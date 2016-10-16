/*
Copyright (c) 2007-2009 Kristofer Karlsson <kristofer.karlsson@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */
package se.krka.kahlua.stdlib;

import se.krka.kahlua.vm.*;

public final class StringLib implements JavaFunction {

	private static final int SUB = 0;
	private static final int CHAR = 1;
	private static final int BYTE = 2;
	private static final int LOWER = 3;
	private static final int UPPER = 4;
	private static final int REVERSE = 5;
	private static final int FORMAT = 6;
	private static final int FIND = 7;
	private static final int MATCH = 8;
	private static final int GSUB = 9;

	private static final int NUM_FUNCTIONS = 10;

	private static final boolean[] SPECIALS = new boolean[256];
	static {
		String s = "^$*+?.([%-";
		for (int i = 0; i < s.length(); i++) {
			SPECIALS[(int) s.charAt(i)] = true;
		}
	}
	
	private static final int LUA_MAXCAPTURES = 32;
	private static final char L_ESC = '%';
	private static final int CAP_UNFINISHED = ( -1 );
	private static final int CAP_POSITION = ( -2 );

	private static final String[] names;
	private static final StringLib[] functions;

	// NOTE: String.class won't work in J2ME - so this is used as a workaround
	private static final Class STRING_CLASS = "".getClass();
	
	static {
		names = new String[NUM_FUNCTIONS];
		names[SUB] = "sub";
		names[CHAR] = "char";
		names[BYTE] = "byte";
		names[LOWER] = "lower";
		names[UPPER] = "upper";
		names[REVERSE] = "reverse";
		names[FORMAT] = "format";
		names[FIND] = "find";
		names[MATCH] = "match";
		names[GSUB] = "gsub";

		functions = new StringLib[NUM_FUNCTIONS];
		for (int i = 0; i < NUM_FUNCTIONS; i++) {
			functions[i] = new StringLib(i);
		}
	}

	/** @exclude */
	private final int methodId;
	public StringLib(int index) {
		this.methodId = index;
	}

	public static void register(Platform platform, KahluaTable env) {
		KahluaTable string = platform.newTable();
		for (int i = 0; i < NUM_FUNCTIONS; i++) {
			string.rawset(names[i], functions[i]);
		}

		string.rawset("__index", string);
        KahluaTable metatables = KahluaUtil.getClassMetatables(platform, env);
        metatables.rawset(STRING_CLASS, string);
		env.rawset("string", string);
	}

	public String toString() {
		return names[methodId];
	}

	public int call(LuaCallFrame callFrame, int nArguments)  {
		switch (methodId) {
		case SUB: return sub(callFrame, nArguments);
		case CHAR: return stringChar(callFrame, nArguments);
		case BYTE: return stringByte(callFrame, nArguments);
		case LOWER: return lower(callFrame, nArguments);
		case UPPER: return upper(callFrame, nArguments);
		case REVERSE: return reverse(callFrame, nArguments);
		case FORMAT: return format(callFrame, nArguments);
		case FIND: return findAux(callFrame, true);
		case MATCH: return findAux(callFrame, false);
		case GSUB: return gsub(callFrame, nArguments);
		default: return 0; // Should never happen.
		}
	}

	private long unsigned(long v) {
		if (v < 0L) {
			v += (1L << 32);
		}
		return v;
	}
	
	private int format(LuaCallFrame callFrame, int nArguments) {
		String f = KahluaUtil.getStringArg(callFrame, 1, names[FORMAT]);

		int len = f.length();
		int argc = 2;
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < len; i++) {
			char c = f.charAt(i);
			if (c == '%') {
				i++;
				KahluaUtil.luaAssert(i < len, "incomplete option to 'format'");
				c = f.charAt(i);
				if (c == '%') {
					result.append('%');
				} else {
					// Detect flags
					boolean repr = false;
					boolean zeroPadding = false;
					boolean leftJustify = false;
					boolean showPlus = false;
					boolean spaceForSign = false;
					flagLoop: while (true) {
						switch (c) {
						case '-':
							leftJustify = true;
							break;
						case '+':
							showPlus = true;
							break;
						case ' ':
							spaceForSign = true;
							break;
						case '#':
							repr = true;
							break;
						case '0':
							zeroPadding = true;
							break;
						default:
							break flagLoop;
						}
						i++;
						KahluaUtil.luaAssert(i < len, "incomplete option to 'format'");
						c = f.charAt(i);
					}

					// Detect width
					int width = 0;
					while (c >= '0' && c <= '9') {
						width = 10 * width + c - '0';
						i++;
						KahluaUtil.luaAssert(i < len, "incomplete option to 'format'");
						c = f.charAt(i);
					}
					
					// Detect precision
					int precision = 0;
					boolean hasPrecision = false;
					if (c == '.') {
						hasPrecision = true;
						i++;
						KahluaUtil.luaAssert(i < len, "incomplete option to 'format'");
						c = f.charAt(i);

						while (c >= '0' && c <= '9') {
							precision = 10 * precision + c - '0';
							i++;
							KahluaUtil.luaAssert(i < len, "incomplete option to 'format'");
							c = f.charAt(i);
						}
					}
					
					if (leftJustify) {
						zeroPadding = false;
					}
					
					// This will be overriden to space for the appropiate specifiers

					// Pass 1: set up various variables needed for each specifier
					// This simplifies the second pass by being able to combine several specifiers.
					
					int base = 10;
					boolean upperCase = false;
					int defaultPrecision = 6; // This is the default for all float numerics
					String basePrepend = "";
					switch (c) {
					// Simple character
					case 'c':
						zeroPadding = false;
						break;
					// change base
					case 'o': 
						base = 8;
						defaultPrecision = 1;
						basePrepend = "0";
						break;
					case 'x':
						base = 16;
						defaultPrecision = 1;
						basePrepend = "0x";
						break;
					case 'X':
						base = 16;
						defaultPrecision = 1;
						upperCase = true;
						basePrepend = "0X";
						break;
					// unsigned integer and signed integer
					case 'u':
						defaultPrecision = 1;
						break;
					case 'd':
					case 'i':
						defaultPrecision = 1;
						break;
					case 'e':
						break;
					case 'E':
						upperCase = true;
						break;
					case 'g':
						break;
					case 'G':
						upperCase = true;
						break;
					case 'f':
						break;
					case 's':
						zeroPadding = false;
						break;
					case 'q':
						// %q neither needs nor supports width
						width = 0;
						break;
					default:
						throw new RuntimeException("invalid option '%" + c +
						"' to 'format'");
					}
					
					// Set precision
					if (!hasPrecision) {
						precision = defaultPrecision;
					}

					if (hasPrecision && base != 10) {
						zeroPadding = false;
					}
					char padCharacter = zeroPadding ? '0' : ' ';

					// extend the string by "width" characters, and delete a subsection of them later to get the correct padding width
					int resultStartLength = result.length();
					if (!leftJustify) {
						extend(result, width, padCharacter);
					}
					
					// Detect specifier and compute result
					switch (c) {
					case 'c':
						result.append((char)(getDoubleArg(callFrame, argc)).shortValue());
						break;
					case 'o':
					case 'x':
					case 'X':
					case 'u': {
						long vLong = getDoubleArg(callFrame, argc).longValue();
						vLong = unsigned(vLong);

						if (repr) {
							if (base == 8) {
								int digits = 0;
								long vLong2 = vLong;
								while (vLong2 > 0) {
									vLong2 /= 8;
									digits++;
								}
								if (precision <= digits) {
									result.append(basePrepend);
								}
							} else if (base == 16) {
								if (vLong != 0) {
									result.append(basePrepend);
								}
							}
						}
						
						if (vLong != 0 || precision > 0) {
							stringBufferAppend(result, vLong, base, false, precision);
						}
						break;
					}
					case 'd':
					case 'i': {
						Double v = getDoubleArg(callFrame, argc);
						long vLong = v.longValue();
						if (vLong < 0) {
							result.append('-');
							vLong = -vLong;
						} else if (showPlus) {
							result.append('+');
						} else if (spaceForSign) {
							result.append(' ');
						}
						if (vLong != 0 || precision > 0) {
							stringBufferAppend(result, vLong, base, false, precision);
						}
						break;
					}
					case 'e':
					case 'E':
					case 'f': {
						Double v = getDoubleArg(callFrame, argc);
						boolean isNaN = v.isInfinite() || v.isNaN();
						
						double vDouble = v.doubleValue();
						if (KahluaUtil.isNegative(vDouble)) {
							if (!isNaN) {
								result.append('-');
							}
							vDouble = -vDouble;
						} else if (showPlus) {
							result.append('+');
						} else if (spaceForSign) {
							result.append(' ');
						}
						if (isNaN) {
							result.append(KahluaUtil.numberToString(v));
						} else {
							if (c == 'f') {
								appendPrecisionNumber(result, vDouble, precision, repr);
							} else {
								appendScientificNumber(result, vDouble, precision, repr, false);
							}
						}
						break;
					}
					case 'g':
					case 'G':
					{
						// Precision is significant digits for %g
						if (precision <= 0) {
							precision = 1;
						}
						
						// first round to correct significant digits (precision),
						// then check which formatting to be used.
						Double v = getDoubleArg(callFrame, argc);
						boolean isNaN = v.isInfinite() || v.isNaN();
						double vDouble = v.doubleValue();
						if (KahluaUtil.isNegative(vDouble)) {
							if (!isNaN) {
								result.append('-');
							}
							vDouble = -vDouble;
						} else if (showPlus) {
							result.append('+');
						} else if (spaceForSign) {
							result.append(' ');
						}
						if (isNaN) {
							result.append(KahluaUtil.numberToString(v));
						} else {
							double x = roundToSignificantNumbers(vDouble, precision);

							/*
							 * Choose %f version if:
							 *     |v| >= 10^(-4)
							 * AND
							 *     |v| < 10^(precision)
							 *     
							 * otherwise, choose %e
							 */ 
							if (x == 0 || (x >= 1e-4 && x < (double) KahluaUtil.ipow(10, precision))) {
								int iPartSize;
								if (x == 0) {
									iPartSize = 1;
								} else if (Math.floor(x) == 0) {
									iPartSize = 0;
								} else {
									double longValue = x;
									iPartSize = 1;
									while (longValue >= 10.0) {
										longValue /= 10.0;
										iPartSize++;
									}
								}
								// format with %f, with precision significant numbers
								appendSignificantNumber(result, x, precision - iPartSize, repr);								
							} else {
								// format with %e, with precision significant numbers, i.e. precision -1 digits
								// but skip trailing zeros unless repr
								appendScientificNumber(result, x, precision - 1, repr, true);
							}
						}
						break;
					}
					case 's': {
						String s = getStringArg(callFrame, argc);
						int n = s.length();
						if (hasPrecision) {
							n = Math.min(precision, s.length());
						}
						append(result, s, 0, n);
						break;
					}
					case 'q':
						String q = getStringArg(callFrame, argc);
						result.append('"');
						for (int j = 0; j < q.length(); j++) {
							char d = q.charAt(j);
							switch (d) {
							case '\\': result.append("\\"); break;
							case '\n': result.append("\\\n"); break;
							case '\r': result.append("\\r"); break;
							case '"': result.append("\\\""); break;
							default: result.append(d);
							}
						}
						result.append('"');
						break;
					default:
						throw new RuntimeException("Internal error");
					}
					if (leftJustify) {
						int currentResultLength = result.length();
						int d = width - (currentResultLength - resultStartLength);
						if (d > 0) {
							extend(result, d, ' ');
						}
					} else {
						int currentResultLength = result.length();
						int d = currentResultLength - resultStartLength - width;
						d = Math.min(d, width);
						if (d > 0) {
							result.delete(resultStartLength, resultStartLength + d);
						}
						if (zeroPadding) {
							int signPos = resultStartLength + (width - d);
							char ch = result.charAt(signPos);
							if (ch == '+' || ch == '-' || ch == ' ') {
								result.setCharAt(signPos, '0');
								result.setCharAt(resultStartLength, ch);
							}
						}
					}
					if (upperCase) {
						stringBufferUpperCase(result, resultStartLength);
					}
					argc++;
				}
			} else {
				result.append(c);
			}
		}
		callFrame.push(result.toString());
		return 1;
	}

	private void append(StringBuffer buffer, String s, int start, int end) {
		for (int i = start; i < end; i++) {
			buffer.append(s.charAt(i));
		}
	}

	private void extend(StringBuffer buffer, int extraWidth, char padCharacter) {
		int preLength = buffer.length();		
		buffer.setLength(preLength + extraWidth);
		for (int i = extraWidth - 1; i >= 0; i--) {
			buffer.setCharAt(preLength + i, padCharacter);
		}
	}

	private void stringBufferUpperCase(StringBuffer buffer, int start) {
		int length = buffer.length();
		for (int i = start; i < length; i++) {
			char c = buffer.charAt(i);
			if (c >= 'a' && c <= 'z') {
				buffer.setCharAt(i, (char) (c - 32));
			}
		}
	}
	
	private static final char[] digits = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
	/**
	 * Precondition: value >= 0
	 * Precondition: 2 <= base <= 16 
	 * @param sb the stringbuffer to append to
	 * @param value the value to append
	 * @param base the base to use when formatting (typically 8, 10 or 16)
	 * @param printZero
	 * @param minDigits
	 */
	private static void stringBufferAppend(StringBuffer sb, double value, int base, boolean printZero, int minDigits) {
		int startPos = sb.length();
		while (value > 0 || minDigits > 0) {
			double newValue = Math.floor(value / base);
			sb.append(digits[(int) (value - (newValue * base))]);
			value = newValue;
			minDigits--;
		}
		int endPos = sb.length() - 1;
		if (startPos > endPos && printZero) {
			sb.append('0');
		} else {
			// Note that the digits are in reverse order now, so we need to correct it.
			// We can't use StringBuffer.reverse because that reverses the entire string
			
			int swapCount = (1 + endPos - startPos) / 2;
			for (int i = swapCount - 1; i >= 0; i--) {
				int leftPos = startPos + i;
				int rightPos = endPos - i;
				char left = sb.charAt(leftPos);
				char right = sb.charAt(rightPos);
				sb.setCharAt(leftPos, right);
				sb.setCharAt(rightPos, left);
			}
		}
	}
	
	/**
	 * Only works with non-negative numbers
	 * @param buffer
	 * @param number
	 * @param precision
	 * @param requirePeriod
	 */
	private void appendPrecisionNumber(StringBuffer buffer, double number, int precision, boolean requirePeriod) {
		number = roundToPrecision(number, precision);
		double iPart = Math.floor(number);
		double fPart = number - iPart;
		
		for (int i = 0; i < precision; i++) {
			fPart *= 10.0;
		}
		fPart = KahluaUtil.round(iPart + fPart) - iPart;
			
		stringBufferAppend(buffer, iPart, 10, true, 0);
		
		if (requirePeriod || precision > 0) {
			buffer.append('.');
		}
		
		stringBufferAppend(buffer, fPart, 10, false, precision);
	}

	/**
	 * Only works with non-negative numbers
	 * @param buffer
	 * @param number
	 * @param significantDecimals
	 * @param includeTrailingZeros
	 */
	private void appendSignificantNumber(StringBuffer buffer, double number, int significantDecimals, boolean includeTrailingZeros) {
		double iPart = Math.floor(number);
		
		stringBufferAppend(buffer, iPart, 10, true, 0);
		
		double fPart = roundToSignificantNumbers(number - iPart, significantDecimals);
		
		boolean hasNotStarted = iPart == 0 && fPart != 0;
		int zeroPaddingBefore = 0;
		int scanLength = significantDecimals;
		for (int i = 0; i < scanLength; i++) {
			fPart *= 10.0;
			if (Math.floor(fPart) == 0 && fPart != 0) {
				zeroPaddingBefore++;
				if (hasNotStarted) {
					scanLength++;
				}
			}
		}
		fPart = KahluaUtil.round(fPart);

		if (!includeTrailingZeros) {
			while (fPart > 0 && (fPart % 10) == 0) {
				fPart /= 10;
				significantDecimals--;
			}
		}
		
		buffer.append('.');
		int periodPos = buffer.length();
		extend(buffer, zeroPaddingBefore, '0');
		int prePos = buffer.length();
		stringBufferAppend(buffer, fPart, 10, false, 0);
		int postPos = buffer.length();

		int len = postPos - prePos;
		if (includeTrailingZeros && len < significantDecimals) {
			int padRightSize = significantDecimals - len - zeroPaddingBefore;
			extend(buffer, padRightSize, '0');
		}
		
		if (!includeTrailingZeros && periodPos == buffer.length()) {
			buffer.delete(periodPos - 1, buffer.length());
		}
	}

	private void appendScientificNumber(StringBuffer buffer, double x, int precision, boolean repr, boolean useSignificantNumbers) {
		int exponent = 0;
		
		// Run two passes to handle cases such as %.2e with the value 95.
		for (int i = 0; i < 2; i++) {
			if (x >= 1.0) {
				while (x >= 10.0) {
					x /= 10.0;
					exponent++;
				}
			} else {
				while (x > 0 && x < 1.0) {
					x *= 10.0;
					exponent--;
				}
			}
			x = roundToPrecision(x, precision);
		}
		int absExponent = Math.abs(exponent);
		char expSign;
		if (exponent >= 0) {
			expSign = '+';
		} else {
			expSign = '-';
		}
		if (useSignificantNumbers) {
			appendSignificantNumber(buffer, x, precision, repr);
		} else {
			appendPrecisionNumber(buffer, x, precision, repr);
		}
		buffer.append('e');
		buffer.append(expSign);
		stringBufferAppend(buffer, absExponent, 10, true, 2);
	}

	private String getStringArg(LuaCallFrame callFrame, int argc) {
		return getStringArg(callFrame, argc, names[FORMAT]);
	}
	
	private String getStringArg(LuaCallFrame callFrame, int argc, String funcname) {
		return KahluaUtil.getStringArg(callFrame, argc, funcname);
	}
	
	private Double getDoubleArg(LuaCallFrame callFrame, int argc) {
		return getDoubleArg(callFrame, argc, names[FORMAT]);
	}

	private Double getDoubleArg(LuaCallFrame callFrame, int argc, String name) {
		return KahluaUtil.getNumberArg(callFrame, argc, name);
	}

	private int lower(LuaCallFrame callFrame, int nArguments) {
		KahluaUtil.luaAssert(nArguments >= 1, "not enough arguments");
		String s = getStringArg(callFrame,1,names[LOWER]);
		
		callFrame.push(s.toLowerCase());
		return 1;
	}

	private int upper(LuaCallFrame callFrame, int nArguments) {
		KahluaUtil.luaAssert(nArguments >= 1, "not enough arguments");
		String s = getStringArg(callFrame,1,names[UPPER]);

		callFrame.push(s.toUpperCase());
		return 1;
	}

	private int reverse(LuaCallFrame callFrame, int nArguments) {
		KahluaUtil.luaAssert(nArguments >= 1, "not enough arguments");
		String s = getStringArg(callFrame, 1, names[REVERSE]);
		s = new StringBuffer(s).reverse().toString();
		callFrame.push(s);
		return 1;
	}

	private int stringByte(LuaCallFrame callFrame, int nArguments) {
		KahluaUtil.luaAssert(nArguments >= 1, "not enough arguments");
		String s = getStringArg(callFrame, 1, names[BYTE]);

		int i = nullDefault(1, KahluaUtil.getOptionalNumberArg(callFrame, 2));
		int j = nullDefault(i, KahluaUtil.getOptionalNumberArg(callFrame, 3));

		int len = s.length();
		if (i < 0) {
			i += len + 1;
		}
		if (i <= 0) {
			i = 1;
		}
		if (j < 0) {
			j += len + 1;
		} else if (j > len) {
			j = len;
		}
		int nReturns = 1 +j - i;

		if (nReturns <= 0) {
			return 0;
		}
		callFrame.setTop(nReturns);
		int offset = i - 1;
		for (int i2 = 0; i2 < nReturns; i2++) {
			char c = s.charAt(offset + i2);
			callFrame.set(i2, KahluaUtil.toDouble(c));
		}
		return nReturns;
	}

	private int nullDefault(int defaultValue, Double val) {
		if (val == null) {
			return defaultValue;
		}
		return val.intValue();
	}

	private int stringChar(LuaCallFrame callFrame, int nArguments) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < nArguments; i++) {
			int num = getDoubleArg(callFrame, 1, names[CHAR]).intValue();
			sb.append((char) num);
		}
		return callFrame.push(sb.toString());
	}

	private int sub(LuaCallFrame callFrame, int nArguments) {
		String s = getStringArg(callFrame, 1, names[SUB]);
		double start = getDoubleArg(callFrame, 2, names[SUB]).doubleValue();
		double end = -1;
		if (nArguments >= 3) {
			end = getDoubleArg(callFrame, 3, names[SUB]).doubleValue();
		}
		String res;
		int istart = (int) start;
		int iend = (int) end;

		int len = s.length();
		if (istart < 0) {
			istart = Math.max(len + istart + 1, 1);
		} else if (istart == 0) {
			istart = 1;
		}


		if (iend < 0) {
			iend = Math.max(0, iend + len + 1);
		} else if (iend > len) {
			iend = len;
		}

		if (istart > iend) {
			return callFrame.push("");
		}
		res = s.substring(istart - 1, iend);

		return callFrame.push(res);
	}

    /**
     * Rounds to keep <em>precision</em> decimals. Rounds towards even numbers.
     * @param x the number to round
     * @param precision the precision to round to. A precision of 3 will for instance round 1.65432 to 1.654
     * @return the rounded number
     */
    public static double roundToPrecision(double x, int precision) {
        double roundingOffset = KahluaUtil.ipow(10, precision);
        return KahluaUtil.round(x * roundingOffset) / roundingOffset;
    }

    public static double roundToSignificantNumbers(double x, int precision) {
        if (x == 0) {
            return 0;
        }
        if (x < 0) {
            return -roundToSignificantNumbers(-x, precision);
        }
        double lowerLimit = KahluaUtil.ipow(10, precision - 1);
        double upperLimit = lowerLimit * 10.0;
        double multiplier = 1.0;
        while (multiplier * x < lowerLimit) {
            multiplier *= 10.0;
        }
        while (multiplier * x >= upperLimit) {
            multiplier /= 10.0;
        }
        return KahluaUtil.round(x * multiplier) / multiplier;
    }

    /* Pattern Matching
      * Original code that this was adapted from is copyright (c) 2008 groundspeak, inc.
      */

	/** @exclude */
	public static class MatchState {
		public final LuaCallFrame callFrame;
		public final StringPointer src_init;  /* init of source string */
		public final int endIndex; /* end (`\0') of source string */
		public final Capture[] capture;

		public MatchState(LuaCallFrame callFrame, StringPointer srcInit, int endIndex) {
			this.callFrame = callFrame;
			this.src_init = srcInit;
			this.endIndex = endIndex;
			capture = new Capture[LUA_MAXCAPTURES];
			for (int i = 0; i < LUA_MAXCAPTURES; i++) {
				capture[i] = new Capture();
			}
		}


		public int level;  /* total number of captures (finished or unfinished) */

		/** @exclude */
		public static class Capture {
			public StringPointer init;
			public int len;
		}

		public Object getCapture(int i) {
			if (i >= level) {
				return null;
			}
			if (capture[i].len == CAP_POSITION) {
				return new Double(src_init.length() - capture[i].init.length() + 1);
			} else {
				return capture[i].init.getStringSubString(capture[i].len);
			}
		}
	}

	/** @exclude */
	public static class StringPointer {
		private final String string;
		private int index = 0;

		public StringPointer(String original) {
			this.string = original;
		}

		public StringPointer(String original, int index) {
			this.string = original;
			this.index = index;
		}

		public StringPointer getClone() {
			return new StringPointer(string, index);
		}

		public int getIndex () {
			return index;
		}

		public void setIndex (int ind) {
			index = ind;
		}

		public String getString() {
			if (index == 0) {
				return string;
			}
			return string.substring(index);
		}

		public int getStringLength() {
			return getStringLength(0);
		}

		public int getStringLength(int i) {
			return string.length() - (index + i);
		}

		public String getStringSubString(int len) {
			return string.substring(index, index + len);
		}

		public char getChar() {
			return getChar(0);
		}

		public char getChar(int strIndex) {
			int i = index + strIndex;
			if (i >= string.length()) {
				return '\0';
			} else {
				return string.charAt(i);
			}
		}

		public int length() {
			return string.length () - index;
		}

		public int postIncrStringI(int num) {
			int oldIndex = index;
			index += num;
			return oldIndex;
		}

		public int preIncrStringI(int num) {
			index += num;
			return index;
		}

		public char postIncrString (int num) {
			char c = getChar();
			index += num;
			return c;
		}

		public int compareTo(StringPointer cmp, int len) {
			for (int i = 0; i < len; i++) {
				int val = getChar(i) - cmp.getChar(i);
				if (val != 0) {
					return val;
				}
			}
			return 0;
		}
	}

	private static Object push_onecapture ( MatchState ms, int i, StringPointer s, StringPointer e ) {
		if (i >= ms.level) {
			if ( i == 0 ) { // ms->level == 0, too
				String res = s.string.substring(s.index, e.index);
				ms.callFrame.push(res);
				return res;
			} else {
				throw new RuntimeException("invalid capture index");
			}
		} else {
			int l = ms.capture[i].len;
			if (l == CAP_UNFINISHED) {
				throw new RuntimeException("unfinished capture");
			} else if (l == CAP_POSITION) {
				Double res = new Double(ms.src_init.length() - ms.capture[i].init.length() + 1);
				ms.callFrame.push(res);
				return res;
			} else {
				int index = ms.capture[i].init.index;
				String res = ms.capture[i].init.string.substring(index, index+l);
				ms.callFrame.push(res);
				return res;
			}
		}
	}

	private static int push_captures ( MatchState ms, StringPointer s, StringPointer e ) {
		int nlevels = ( ms.level == 0 && s != null ) ? 1 : ms.level;
		KahluaUtil.luaAssert(nlevels <= LUA_MAXCAPTURES, "too many captures");
		for (int i = 0; i < nlevels; i++) {
			push_onecapture (ms, i, s, e);
		}
		return nlevels;  // number of strings pushed
	}

	private static boolean noSpecialChars(String pattern) {
		for (int i = 0; i < pattern.length(); i++) {
			char c = pattern.charAt(i);
			if (c < 256 && SPECIALS[c]) {
				return false;
			}
		}
		return true;
	}

	private static int findAux (LuaCallFrame callFrame, boolean find ) {
		String f = find ? names[FIND] : names[MATCH];
		String source = KahluaUtil.getStringArg(callFrame, 1, f);
		String pattern = KahluaUtil.getStringArg(callFrame, 2, f);
		Double i = KahluaUtil.getOptionalNumberArg(callFrame, 3);
		boolean plain = KahluaUtil.boolEval(KahluaUtil.getOptionalArg(callFrame, 4));
		int init = (i == null ? 0 : i.intValue() - 1);

		if ( init < 0 ) {
			// negative numbers count back from the end of the string.
			init += source.length();
			if ( init < 0 ) {
				init = 0; // if we are still negative, just start at the beginning.
			}
		} else if ( init > source.length() ) {
			init = source.length();
		}

		if ( find && ( plain || noSpecialChars(pattern) ) ) { // explicit plain request or no special characters?
			// do a plain search
			int pos = source.indexOf(pattern, init);
			if ( pos > -1 ) {
				return callFrame.push(KahluaUtil.toDouble(pos + 1), KahluaUtil.toDouble(pos + pattern.length()));
			}
		} else {
			StringPointer s = new StringPointer(source);
			StringPointer p = new StringPointer(pattern);

			boolean anchor = false;
			if (p.getChar() == '^') {
				anchor = true;
				p.postIncrString(1);
			}
			StringPointer s1 = s.getClone();
			s1.postIncrString(init);

			MatchState ms = new MatchState(callFrame, s.getClone(), s.getStringLength());

			do {
				StringPointer res;
				ms.level = 0;
				if ((res = match(ms, s1, p)) != null) {
					if (find) {
						return callFrame.push(new Double(s.length () - s1.length () + 1), new Double(s.length () - res.length ())) +
						push_captures ( ms, null, null );
					} else {
						return push_captures ( ms, s1, res );
					}
				}

			} while ( s1.postIncrStringI ( 1 ) < ms.endIndex && !anchor );
		}
		return callFrame.pushNil();  // not found
	}

	private static StringPointer startCapture ( MatchState ms, StringPointer s, StringPointer p, int what ) {
		StringPointer res;
		int level = ms.level;
		KahluaUtil.luaAssert(level < LUA_MAXCAPTURES, "too many captures");

		ms.capture[level].init = s.getClone();
		ms.capture[level].init.setIndex ( s.getIndex () );
		ms.capture[level].len = what;
		ms.level = level + 1;
		if ( ( res = match ( ms, s, p ) ) == null ) /* match failed? */ {
			ms.level --;  /* undo capture */
		}
		return res;
	}

	private static int captureToClose ( MatchState ms ) {
		int level = ms.level;
		for ( level --; level >= 0; level -- ) {
			if ( ms.capture[level].len == CAP_UNFINISHED ) {
				return level;
			}
		}
		throw new RuntimeException("invalid pattern capture");
	}

	private static StringPointer endCapture ( MatchState ms, StringPointer s, StringPointer p ) {
		int l = captureToClose ( ms );
		StringPointer res;
		ms.capture[l].len = ms.capture[l].init.length () - s.length ();  /* close capture */
		if ( ( res = match ( ms, s, p ) ) == null ) /* match failed? */ {
			ms.capture[l].len = CAP_UNFINISHED;  /* undo capture */
		}
		return res;
	}

	private static int checkCapture ( MatchState ms, int l ) {
		l -= '1'; // convert chars 1-9 to actual ints 1-9
		KahluaUtil.luaAssert(l < 0 || l >= ms.level || ms.capture[l].len == CAP_UNFINISHED,
		"invalid capture index");
		return l;
	}

	private static StringPointer matchCapture ( MatchState ms, StringPointer s, int l ) {
		int len;
		l = checkCapture ( ms, l );
		len = ms.capture[l].len;
		if ( ( ms.endIndex - s.length () ) >= len && ms.capture[l].init.compareTo(s, len) == 0 ) {
			StringPointer sp = s.getClone();
			sp.postIncrString ( len );
			return sp;
		}
		else {
			return null;
		}
	}

	private static StringPointer matchBalance ( MatchState ms, StringPointer ss, StringPointer p ) {

		KahluaUtil.luaAssert(!(p.getChar () == 0 || p.getChar ( 1 ) == 0), "unbalanced pattern");

		StringPointer s = ss.getClone();
		if ( s.getChar () != p.getChar () ) {
			return null;
		} else {
			int b = p.getChar ();
			int e = p.getChar ( 1 );
			int cont = 1;

			while ( s.preIncrStringI ( 1 ) < ms.endIndex ) {
				if ( s.getChar () == e ) {
					if (  -- cont == 0 ) {
						StringPointer sp = s.getClone();
						sp.postIncrString ( 1 );
						return sp;
					}
				} else if ( s.getChar () == b ) {
					cont ++;
				}
			}
		}
		return null;  /* string ends out of balance */
	}

	private static StringPointer classEnd(StringPointer pp) {
		StringPointer p = pp.getClone();
		switch ( p.postIncrString ( 1 ) ) {
		case L_ESC: {
			KahluaUtil.luaAssert(p.getChar () != '\0', "malformed pattern (ends with '%')");
			p.postIncrString ( 1 );
			return p;
		}
		case '[': {
			if ( p.getChar () == '^' ) {
				p.postIncrString ( 1 );
			}
			do { // look for a `]' 
				KahluaUtil.luaAssert(p.getChar () != '\0', "malformed pattern (missing ']')");

				if ( p.postIncrString ( 1 ) == L_ESC && p.getChar () != '\0' ) {
					p.postIncrString ( 1 );  // skip escapes (e.g. `%]')
				}

			} while ( p.getChar () != ']' );

			p.postIncrString ( 1 );
			return p;
		}
		default: {
			return p;
		}
		}
	}

	private static boolean singleMatch ( char c, StringPointer p, StringPointer ep ) {
		switch ( p.getChar () ) {
		case '.':
			return true;  // matches any char
		case L_ESC:
			return matchClass ( p.getChar ( 1 ), c );
		case '[': {
			StringPointer sp = ep.getClone();
			sp.postIncrString ( -1 );
			return matchBracketClass ( c, p, sp );
		}
		default:
			return ( p.getChar () == c );
		}
	}

	private static StringPointer minExpand ( MatchState ms, StringPointer ss, StringPointer p, StringPointer ep ) {
		StringPointer sp = ep.getClone();
		StringPointer s = ss.getClone();

		sp.postIncrString ( 1 );
		while (true) {
			StringPointer res = match ( ms, s, sp );
			if ( res != null ) {
				return res;
			} else if ( s.getIndex () < ms.endIndex && singleMatch ( s.getChar (), p, ep ) ) {
				s.postIncrString ( 1 );  // try with one more repetition 
			} else {
				return null;
			}
		}
	}

	private static StringPointer maxExpand(MatchState ms, StringPointer s, StringPointer p, StringPointer ep) {
		int i = 0;  // counts maximum expand for item
		while (s.getIndex () + i < ms.endIndex && singleMatch(s.getChar(i), p, ep)) {
			i ++;
		}
		// keeps trying to match with the maximum repetitions 
		while (i >= 0) {
			StringPointer sp1 = s.getClone();
			sp1.postIncrString(i);
			StringPointer sp2 = ep.getClone();
			sp2.postIncrString(1);
			StringPointer res = match(ms, sp1, sp2);
			if (res != null) {
				return res;
			}
			i --;  // else didn't match; reduce 1 repetition to try again
		}
		return null;
	}

	private static boolean matchBracketClass(char c, StringPointer pp, StringPointer ecc) {
		StringPointer p = pp.getClone();
		StringPointer ec = ecc.getClone();
		boolean sig = true;
		if (p.getChar(1) == '^') {
			sig = false;
			p.postIncrString(1);  // skip the `^'
		}
		while (p.preIncrStringI(1) < ec.getIndex()) {
			if (p.getChar() == L_ESC) {
				p.postIncrString(1);
				if (matchClass(p.getChar(), c)) {
					return sig;
				}
			} else if ((p.getChar(1) == '-') && (p.getIndex() + 2 < ec.getIndex())) {
				p.postIncrString(2);
				if (p.getChar(-2) <= c && c <= p.getChar()) {
					return sig;
				}
			} else if (p.getChar () == c) {
				return sig;
			}
		}
		return !sig;
	}

	private static StringPointer match(MatchState ms, StringPointer ss, StringPointer pp) {
		StringPointer s = ss.getClone();
		StringPointer p = pp.getClone();
		boolean isContinue = true;
		boolean isDefault = false;
		while (isContinue) {
			isContinue = false;
			isDefault = false;
			switch (p.getChar()) {
			case '(': { // start capture
				StringPointer p1 = p.getClone();
				if (p.getChar(1) == ')') { // position capture?
					p1.postIncrString(2);
					return startCapture(ms, s, p1, CAP_POSITION);
				} else {
					p1.postIncrString(1);
					return startCapture(ms, s, p1, CAP_UNFINISHED);
				}
			}
			case ')': { // end capture 
				StringPointer p1 = p.getClone();
				p1.postIncrString(1);
				return endCapture(ms, s, p1);
			}
			case L_ESC: {
				switch (p.getChar(1)) {
				case 'b': { // balanced string?
					StringPointer p1 = p.getClone();
					p1.postIncrString(2);
					s = matchBalance(ms, s, p1);
					if (s == null) {
						return null;
					}
					p.postIncrString(4);
					isContinue = true;
					continue; // else return match(ms, s, p+4);
				}
				case 'f': { // frontier?
					p.postIncrString (2);
					KahluaUtil.luaAssert(p.getChar() == '[' , "missing '[' after '%%f' in pattern");

					StringPointer ep = classEnd(p);  // points to what is next
					char previous = (s.getIndex() == ms.src_init.getIndex()) ? '\0' : s.getChar(-1);

					StringPointer ep1 = ep.getClone();
					ep1.postIncrString(-1);
					if (matchBracketClass(previous, p, ep1) || !matchBracketClass(s.getChar(), p, ep1)) {
						return null;
					}
					p = ep;
					isContinue = true;
					continue; // else return match(ms, s, ep);
				}
				default: {
					if (Character.isDigit(p.getChar(1))) { // capture results (%0-%9)?
						s = matchCapture(ms, s, p.getChar(1));
						if (s == null) {
							return null;
						}
						p.postIncrString(2);
						isContinue = true;
						continue; // else return match(ms, s, p+2) 
					}
					isDefault = true; // case default 
				}
				}
				break;
			}
			case '\0': {  // end of pattern
				return s;  // match succeeded
			}
			case '$': {
				if (p.getChar(1) == '\0') { // is the `$' the last char in pattern?
					return (s.getIndex() == ms.endIndex) ? s : null;  // check end of string 
				}
			}
			default: { // it is a pattern item
				isDefault = true;
			}
			}

			if (isDefault) { // it is a pattern item
				StringPointer ep = classEnd(p);  // points to what is next
				boolean m = (s.getIndex () < ms.endIndex && singleMatch(s.getChar(), p, ep));
				switch (ep.getChar()) {
				case '?':  { // optional
					StringPointer res;
					StringPointer s1 = s.getClone();
					s1.postIncrString ( 1 );
					StringPointer ep1 = ep.getClone();
					ep1.postIncrString ( 1 );

					if (m && ((res = match(ms, s1, ep1)) != null)) {
						return res;
					}
					p = ep;
					p.postIncrString(1);
					isContinue = true;
					continue; // else return match(ms, s, ep+1);
				}
				case '*': { // 0 or more repetitions 
					return maxExpand(ms, s, p, ep);
				}
				case '+': { // 1 or more repetitions
					StringPointer s1 = s.getClone();
					s1.postIncrString(1);
					return (m ? maxExpand(ms, s1, p, ep) : null);
				}
				case '-': { // 0 or more repetitions (minimum) 
					return minExpand(ms, s, p, ep);
				}
				default: {
					if (!m) {
						return null;
					}
					s.postIncrString(1);

					p = ep;
					isContinue = true;
					continue; // else return match(ms, s+1, ep);
				}
				}
			}
		}
		return null;
	}

	private static boolean matchClass(char classIdentifier, char c) {
		boolean res;
		char lowerClassIdentifier = Character.toLowerCase(classIdentifier);
		switch (lowerClassIdentifier) {
		case 'a': res = Character.isLowerCase(c) || Character.isUpperCase(c); break;
		case 'c': res = isControl(c); break;
		case 'd': res = Character.isDigit(c); break;
		case 'l': res = Character.isLowerCase(c); break;
		case 'p': res = isPunct(c); break;
		case 's': res = isSpace(c); break;
		case 'u': res = Character.isUpperCase(c); break;
		case 'w': res = Character.isLowerCase(c) || Character.isUpperCase(c) || Character.isDigit(c); break;
		case 'x': res = isHex(c); break;
		case 'z': res = (c == 0); break;
		default: return (classIdentifier == c);
		}
		return (lowerClassIdentifier == classIdentifier) == res;
	}

	private static boolean isPunct(char c) {
		return ( c >= 0x21 && c <= 0x2F ) || 
		( c >= 0x3a && c <= 0x40 ) || 
		( c >= 0x5B && c <= 0x60 ) || 
		( c >= 0x7B && c <= 0x7E );
	}

	private static boolean isSpace(char c) {
		return ( c >= 0x09 && c <= 0x0D ) || c == 0x20 ;
	}

	private static boolean isControl(char c) {
		return ( c >= 0x00 && c <= 0x1f ) || c == 0x7f;
	}

	private static boolean isHex(char c) {
		return ( c >= '0' && c <= '9' ) || ( c >= 'a' && c <= 'f' ) || ( c >= 'A' && c <= 'F' );
	}

	private static int gsub(LuaCallFrame cf, int nargs) {
		String srcTemp = KahluaUtil.getStringArg(cf, 1, names[GSUB]);
		String pTemp = KahluaUtil.getStringArg(cf, 2, names[GSUB]);
		Object repl = KahluaUtil.getArg(cf, 3, names[GSUB]);
		{
			String tmp = KahluaUtil.rawTostring(repl);
			if (tmp != null) {
				repl = tmp;
			}
		}			
		Double num = KahluaUtil.getOptionalNumberArg(cf, 4);
		// if i isn't supplied, we want to substitute all occurrences of the pattern
		int maxSubstitutions = (num == null) ? Integer.MAX_VALUE : num.intValue(); 

		StringPointer pattern = new StringPointer (pTemp);
		StringPointer src = new StringPointer (srcTemp);

		boolean anchor = false;
		if (pattern.getChar() == '^') {
			anchor = true;
			pattern.postIncrString ( 1 );
		}

		if (!(repl instanceof Double ||
						repl instanceof String ||
						repl instanceof LuaClosure ||
						repl instanceof JavaFunction ||
						repl instanceof KahluaTable )) {
			KahluaUtil.fail(("string/function/table expected, got " + repl));
		}

		MatchState ms = new MatchState(cf, src.getClone(), src.length());

		int n = 0;
		StringBuffer b = new StringBuffer();
		while (n < maxSubstitutions) {
			ms.level = 0;
			StringPointer e = match(ms, src, pattern);
			if (e != null) {
				n++;
				addValue(ms, repl, b, src, e);
			}

			if (e != null && e.getIndex() > src.getIndex()) { // non empty match?
				src.setIndex (e.getIndex());  // skip it 
			} else if (src.getIndex() < ms.endIndex) {
				b.append(src.postIncrString(1));
			} else {
				break;
			}

			if (anchor) {
				break;
			}
		}
		return cf.push(b.append(src.getString()).toString(), new Double(n));
	}

	private static void addValue(MatchState ms, Object repl, StringBuffer b, StringPointer src, StringPointer e) {
		String replString = KahluaUtil.rawTostring(repl);
		if (replString != null) {
			b.append(addString(ms, replString, src, e));
		} else {
			Object captures = ms.getCapture(0);
			String match;
			if (captures != null) {
				match = KahluaUtil.rawTostring(captures);
			} else {
				match = src.getStringSubString(e.getIndex() - src.getIndex());
			}
			Object res = null;
			if (repl instanceof KahluaTable) {
				res = ((KahluaTable)repl).rawget(match);
			} else {
				res = ms.callFrame.getThread().call(repl, match, null, null);
			}
			if (res == null) {
				res = match;
			}
			b.append(KahluaUtil.rawTostring(res));
		}
	}

	private static String addString(MatchState ms, String repl, StringPointer s, StringPointer e) {
		StringPointer replStr = new StringPointer(repl);
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < repl.length(); i++) {
			char c = replStr.getChar(i);
			if (c != L_ESC) {
				buf.append(c);
			} else {
				i++;  // skip ESC
				c = replStr.getChar(i);
				if (!Character.isDigit(c)) {
					buf.append(c);
				} else if (c == '0') {
					int len = s.getStringLength() - e.length();
					buf.append(s.getStringSubString(len));
				} else {
					Object o = ms.getCapture(c - '1');
					buf.append(KahluaUtil.tostring(o, null));
				}
			}
		}
		return buf.toString();
	}
}

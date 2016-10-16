/*
 * INPUTStatement.java - Implement the INPUT Statement.
 *
 * Copyright (c) 1996 Chuck McManis, All Rights Reserved.
 *
 * Permission to use, copy, modify, and distribute this software
 * and its documentation for NON-COMMERCIAL purposes and without
 * fee is hereby granted provided that this copyright notice
 * appears in all copies.
 *
 * CHUCK MCMANIS MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE
 * SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. CHUCK MCMANIS
 * SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT
 * OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */

package cocoa.basic;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Vector;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * This is the INPUT statement.
 *
 * The syntax of the INPUT statement is :
 *      INPUT [ "prompt"; ] var1, var2, ... varN
 *
 * The variables can be string variables or numeric variables but they
 * cannot be expressions. When reading into a string variable all characters
 * up to the first comma are stored into the string variable, unless the
 * string is quoted with " characters.
 *
 * If insufficient input is provided, the prompt is re-iterated for more data.
 * Syntax errors:
 *      Semi-colon expected after the prompt string.
 *      Malformed INPUT statement.
 * Runtime errors:
 *      Type mismatch.
 *
 */
class INPUTStatement extends Statement {

    /** The prompt is displayed prior to requesting input. */
    String prompt;

    /** This vector holds a list of variables to fill */
    Vector args;

    /**
     * Construct a new INPUT statement object.
     */
    INPUTStatement(LexicalTokenizer lt) throws BASICSyntaxError {
        super(INPUT);
        parse(this, lt);
    }

    /**
     * Execute the INPUT statement. Most of the work is done in fillArgs.
     */
    Statement doit(Program pgm, InputStream in, PrintStream out) throws BASICRuntimeError {
        DataInputStream dis = new DataInputStream(in);
        getMoreData(dis, out, prompt);
        fillArgs(in, out, prompt, pgm, args);
        return (pgm.nextStatement(this));
    }

    /**
     * Reconstruct this statement from its parsed data.
     */
    String unparse() {
        StringBuffer sb = new StringBuffer();
        sb.append("INPUT ");
        if (prompt != null) {
            sb.append("\""+prompt+"\"; ");
        }
        for (int i = 0; i < args.size(); i++) {
            Variable va = (Variable) args.elementAt(i);
            if (i < (args.size() - 1)) {
                sb.append(va.unparse()+", ");
            } else {
                sb.append(va.unparse());
            }
        }
        return sb.toString();
    }

    /**
     * This is our buffer for processing INPUT statement requests.
     */
    private int currentPos= 500;
    private char buffer[] = new char[256];

    void getMoreData(DataInputStream in, PrintStream out, String prompt) throws BASICRuntimeError {
        String x = null;

        if (prompt != null) {
            out.print(prompt);
        } else {
            out.print("?");
        }
        out.print(" ");
        out.flush();

        try {
            x = in.readLine();
        } catch (IOException ioe) {
            throw new BASICRuntimeError(this, "I/O error on input.");
        }
        if (x == null)
            throw new BASICRuntimeError(this, "Out of data for INPUT.");
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = (x.length() > i) ? x.charAt(i) : 0;
        }
        buffer[x.length()] = '\n';
        currentPos = 0;
    }

    /*
     * Read a floating point number from the character buffer array.
     */
    double getNumber(DataInputStream in, PrintStream out, String prompt) throws BASICRuntimeError {
        double m = 0;   // Mantissa
        double f = 0;   // Fractional component
        int oldPos = currentPos; // save our place.
        boolean wasNeg = false;

        if (currentPos >= buffer.length)
            getMoreData(in, out, prompt);

        while (Character.isSpace(buffer[currentPos])) {
            if (buffer[currentPos] == '\n') {
                getMoreData(in, out, prompt);
            }
            currentPos++;
            if (currentPos >= buffer.length)
                getMoreData(in, out, prompt);
        }

        if (buffer[currentPos] == '-') {
            wasNeg = true;
            currentPos++;
        }

        // Look for the integral part.
        while (Character.isDigit(buffer[currentPos])) {
            m = (m*10.0) + (buffer[currentPos++] - '0');
        }

        // Now look for the fractional part.
        if (buffer[currentPos] == '.') {
            currentPos++;
            double t = .1;
            while (Character.isDigit(buffer[currentPos])) {
                f = f + (t * (buffer[currentPos++] - '0'));
                t = t/10.0;
            }
        } else if (currentPos == oldPos) // no number found
            throw new BASICRuntimeError(this, "Number expected.");

        m = (m + f) * ((wasNeg) ? -1 : 1);
        // so it was a number, perhaps we are done with it.
        if ((buffer[currentPos] != 'E') && (buffer[currentPos] != 'e')) {
            return m;
        }

        currentPos++; // skip over the 'e'

        int p = 0;
        double e;
        wasNeg = false;

        // check for negative exponent.
        if (buffer[currentPos] == '-') {
            wasNeg = true;
            currentPos++;
        } else if (buffer[currentPos] == '+') {
            currentPos++;
        }

        while (Character.isDigit(buffer[currentPos])) {
            p = (p * 10) + (buffer[currentPos++] - '0');
        }

        try {
            e = Math.pow(10, (double)p);
        } catch (ArithmeticException zzz) {
            throw new BASICRuntimeError(this, "Illegal numeric constant.");
        }

        if (wasNeg)
            e = 1/e;
        return m * e;
    }

    String getString(DataInputStream in, PrintStream out, String prompt) throws BASICRuntimeError {
        int oldPos = currentPos;
        int sIndex, eIndex;
        StringBuffer sb = new StringBuffer();

        if (currentPos >= buffer.length)
            getMoreData(in, out, prompt);

        while (Character.isSpace(buffer[currentPos])) {
            if (buffer[currentPos] == '\n') {
                getMoreData(in, out, prompt);
            }
            currentPos++;
            if (currentPos >= buffer.length)
                getMoreData(in, out, prompt);
        }

        boolean inQuote = false;
        while (true) {
            switch((int) buffer[currentPos]) {
                case '\n':
                    return (sb.toString()).trim();
                case '"' :
                    if (buffer[currentPos+1] == '"') {
                        currentPos++;
                        sb.append('"');
                    } else if (inQuote) {
                        currentPos++;
                        return sb.toString();
                    } else {
                        inQuote = true;
                    }
                    break;
                case ',' :
                    if (inQuote) {
                        sb.append(',');
                    } else {
                        return (sb.toString()).trim();
                    }
                    break;
                default :
                    sb.append(buffer[currentPos]);
            }
            currentPos++;
            if (currentPos >= buffer.length)
                return sb.toString();
        }
    }

    void fillArgs(InputStream in, PrintStream out, String prompt, Program pgm, Vector v) throws BASICRuntimeError {
        DataInputStream d;

        if (in instanceof DataInputStream)
            d = (DataInputStream) in;
        else
            d = new DataInputStream(in);

        for (int i = 0; i < v.size(); i++) {
            Variable vi = (Variable) v.elementAt(i);
            if (buffer[currentPos] == '\n')
                getMoreData(d, out, "(more)"+((prompt == null) ? "?" : prompt));
            if (!vi.isString()) {
                pgm.setVariable(vi, getNumber(d, out, prompt));
            } else {
                pgm.setVariable(vi, getString(d, out, prompt));
            }
            while (true) {
                if (buffer[currentPos] == ',') {
                    currentPos++;
                    break;
                }

                if (buffer[currentPos] == '\n') {
                    break;
                }

                if (Character.isSpace(buffer[currentPos])) {
                    currentPos++;
                    continue;
                }
                throw new BASICRuntimeError(this, "Comma expected, got '"+buffer[currentPos]+"'.");
            }
        }
    }

    /**
     * Parse INPUT Statement.
     */
    private static void parse(INPUTStatement s, LexicalTokenizer lt) throws BASICSyntaxError {
        Token t;
        boolean needComma = false;
        s.args = new Vector();

        // get optional prompt string.
        t = lt.nextToken();
        if (t.typeNum() == Token.STRING) {
            s.prompt = t.stringValue();
            t = lt.nextToken();
            if (! t.isSymbol(';'))
                throw new BASICSyntaxError("semi-colon expected after prompt string.");
        } else {
            lt.unGetToken();
        }
        while (true) {
            t = lt.nextToken();
            if (t.typeNum() == Token.EOL) {
                return;
            }

            if (needComma) {
                if (! t.isSymbol(',')) {
                    lt.unGetToken();
                    return;
                }
                needComma = false;
                continue;
            }
            if (t.typeNum() == Token.VARIABLE) {
                s.args.addElement(t);
            } else {
                throw new BASICSyntaxError("malformed INPUT statement.");
            }
            needComma = true;
        }
    }
}

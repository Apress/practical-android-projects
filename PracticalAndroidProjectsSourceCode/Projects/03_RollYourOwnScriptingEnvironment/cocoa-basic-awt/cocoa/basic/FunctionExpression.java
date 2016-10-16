/*
 * FunctionExpression.java - this is a built in function call.
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

import java.util.Random;
import java.io.PrintStream;

/**
 * This class implements the mathematical functions for BASIC. The tokenizer
 * will scan the input for one of the strings in <i>functions[]</i> and if
 * it finds it, it returns a token of FUNCTION with its value being that of
 * the index of the string into the array. (which are convientiently mapped
 * into named constants).
 *
 * Parsing of the arguments to to the function are left up to the parse
 * method in this class.
 */
class FunctionExpression extends Expression {

    Expression sVar; // STRING function variable.

    final static String functions[] = {
        "rnd", "int", "sin", "cos", "tan", "atn", "sqr", "max", "min", "abs",
        "left$", "right$", "mid$", "chr$", "len", "val", "spc$", "log", "fre",
        "sgn", "tab", "str$",
    };

    final static int RND = 0;
    final static int INT = 1;
    final static int SIN = 2;
    final static int COS = 3;
    final static int TAN = 4;
    final static int ATN = 5;
    final static int SQR = 6;
    final static int MAX = 7;
    final static int MIN = 8;
    final static int ABS = 9;
    final static int LEFT = 10;
    final static int RIGHT = 11;
    final static int MID = 12;
    final static int CHR = 13;
    final static int LEN = 14;
    final static int VAL = 15;
    final static int SPC = 16;
    final static int LOG = 17;
    final static int FRE = 18; // doesn't really do anything here.
    final static int SGN = 19;
    final static int TAB = 20;
    final static int STR = 21;

    Random r;

    void print(PrintStream p) {
        p.print(functions[oper].toUpperCase());
        p.print("(");
        if (arg1 != null)
            arg1.print(p);
        if (arg2 != null)
            arg2.print(p);
    }

    public String toString() {
        return "FunctionExpression:: '"+unparse()+"'";
    }

    String unparse() {
        StringBuffer sb = new StringBuffer();

        sb.append(functions[oper].toUpperCase());
        sb.append("(");
        if (sVar != null) {
            sb.append(sVar.unparse());
            sb.append(", ");
        }
        if (arg1 != null) {
            sb.append(arg1.unparse());
            sb.append(", ");
        }
        sb.append(arg2.unparse());
        sb.append(")");
        return sb.toString();
    }

    private FunctionExpression(int t, Expression e) {
        super();
        oper = t;
        arg2 = e;
    }

    private FunctionExpression(int t, Expression a1, Expression a2) {
        super();
        arg1 = a1;
        arg2 = a2;
        oper = t;
    }

    double value(Program p) throws BASICRuntimeError {
        try {
            switch (oper) {
                case RND :
                    if (r == null)
                        r = p.getRandom();
                    return (r.nextDouble() * arg2.value(p));
                case INT :
                    return Math.floor(arg2.value(p));
                case SIN :
                    return Math.sin(arg2.value(p));
                case COS :
                    return Math.cos(arg2.value(p));
                case TAN :
                    return Math.tan(arg2.value(p));
                case ATN :
                    return Math.atan(arg2.value(p));
                case SQR :
                    return Math.sqrt(arg2.value(p));
                case MAX :
                    return Math.max(arg1.value(p), arg2.value(p));
                case MIN :
                    return Math.min(arg1.value(p), arg2.value(p));
                case ABS:
                    return Math.abs(arg2.value(p));
                case LEN:
                    String s = arg2.stringValue(p);
                    return (double) s.length();
                case LOG:
                    return Math.log(arg2.value(p));
                case FRE:
                    return 8192.0; // a round number.
                case SGN:
                    double v = arg2.value(p);
                    if (v < 0) {
                        return -1.0;
                    } else if (v > 0) {
                        return 1.0;
                    }
                    return 0.0;
                case VAL:
                    Double dd = null;
                    String zz = (arg2.stringValue(p)).trim();
                    try {
                        dd = Double.valueOf(zz);
                    } catch (NumberFormatException nfe) {
                        throw new BASICRuntimeError("Invalid string for VAL function.");
                    }
                    return dd.doubleValue();
                default :
                    throw new BASICRuntimeError("Unknown or non-numeric function.");
            }
        } catch (Exception e) {
            if (e instanceof BASICRuntimeError)
                throw (BASICRuntimeError) e;
            else
                throw new BASICRuntimeError("Arithmetic Exception.");
        }
    }

    boolean isString() {
        switch (oper) {
            case LEFT:
            case RIGHT:
            case MID:
            case CHR:
            case STR:
            case SPC:
            case TAB:
                return true;
            default:
                return false;
        }
    }

    String stringValue(Program pgm) throws BASICRuntimeError {
        return stringValue(pgm, 0);
    }

    String stringValue(Program pgm, int column) throws BASICRuntimeError {
        String ss = null;
        int len = 0;

        if (sVar != null) {
            ss = sVar.stringValue(pgm);
            len = ss.length();
        }

        /* [pieter][20101125] */ StringBuffer sb;
        /* [pieter][20101125] */ int a;

        switch (oper) {
            /* [pieter][20101125] */ //StringBuffer sb;
            /* [pieter][20101125] */ //int a;

            case LEFT:
                return ss.substring(0, (int) arg2.value(pgm));
            case RIGHT:
                return ss.substring(len - (int) arg2.value(pgm));
            case MID:
                int t = (int) arg1.value(pgm);
                return ss.substring(t-1, (t-1)+(int) arg2.value(pgm));
            case CHR:
                return ""+(char)arg2.value(pgm);
            case STR:
                return ""+arg2.value(pgm);
            case SPC:
                sb = new StringBuffer();
                a = (int) arg2.value(pgm);
                for (int i = 0; i < a; i++) {
                    sb.append(' ');
                }
                return sb.toString();

            case TAB:
                a = (int) arg2.value(pgm);
                sb = new StringBuffer();
                for (int i = column; i < a; i++) {
                    sb.append(' ');
                }
                return sb.toString();

            default:
                return "Function not implemented yet.";
        }
    }

    /**
     * Parse a function argument. This code pulls off the '(' and ')' around the
     * arguments passed to the function and parses them.
     */
    static FunctionExpression parse(int ty, LexicalTokenizer lt) throws BASICSyntaxError {
        FunctionExpression result;
        Expression a;
        Expression b;
        Expression se;
        Token t;

        t = lt.nextToken();
        if (! t.isSymbol('(')) {
            if (ty == RND) {
                lt.unGetToken();
                return new FunctionExpression(ty, new ConstantExpression(1));
            } else if (ty == FRE) {
                lt.unGetToken();
                return new FunctionExpression(ty, new ConstantExpression(0));
            }
            throw new BASICSyntaxError("Missing argument for function.");
        }
        switch (ty) {
            case RND:
            case INT:
            case SIN:
            case COS:
            case TAN:
            case ATN:
            case SQR:
            case ABS:
            case CHR:
            case VAL:
            case STR:
            case SPC:
            case TAB:
            case LOG:
                a = ParseExpression.expression(lt);
                if (a instanceof BooleanExpression) {
                    throw new BASICSyntaxError(functions[ty].toUpperCase()+" function cannot accept boolean expression.");
                }
                if ((ty == VAL) && (! a.isString()))
                    throw new BASICSyntaxError(functions[ty].toUpperCase()+" requires a string valued argument.");
                result = new FunctionExpression(ty, a);
                break;
            case MAX:
            case MIN:
                a = ParseExpression.expression(lt);
                if (a instanceof BooleanExpression) {
                    throw new BASICSyntaxError(functions[ty]+" function cannot accept boolean expression.");
                }
                t = lt.nextToken();
                if (! t.isSymbol(','))
                    throw new BASICSyntaxError(functions[ty]+" function expects two arguments.");
                b = ParseExpression.expression(lt);
                if (b instanceof BooleanExpression) {
                    throw new BASICSyntaxError(functions[ty]+" function cannot accept boolean expression.");
                }
                result = new FunctionExpression(ty, a, b);
                break;
            case LEN:
                a = ParseExpression.expression(lt);
                if (! a.isString()) {
                    throw new BASICSyntaxError(functions[ty]+
                            " function expects a string argumnet.");
                }
                result = new FunctionExpression(ty, a);
                break;
            case LEFT:
            case RIGHT:
                se = ParseExpression.expression(lt);
                if (! se.isString()) {
                    throw new BASICSyntaxError(
                            "Function expects a string expression.");
                }
                t = lt.nextToken();
                if (! t.isSymbol(',')) {
                    throw new BASICSyntaxError(functions[ty]+
                            " function requires two arguments.");
                }
                a = ParseExpression.expression(lt);
                result = new FunctionExpression(ty, a);
                result.sVar = se;
                break;

            case MID:
                se = ParseExpression.expression(lt);
                if (! se.isString()) {
                    throw new BASICSyntaxError(
                            "Function expects a string expression.");
                }
                t = lt.nextToken();
                if (! t.isSymbol(',')) {
                    throw new BASICSyntaxError(functions[ty]+
                            " function requires at least two arguments.");
                }
                a = ParseExpression.expression(lt);
                t = lt.nextToken();
                if (t.isSymbol(')')) {
                    b = new ConstantExpression(1.0);
                    lt.unGetToken();
                } else if (t.isSymbol(',')) {
                    b = ParseExpression.expression(lt);
                } else {
                    throw new BASICSyntaxError(functions[ty]+
                        " unexpected symbol in expression.");
                }
                result = new FunctionExpression(ty, a, b);
                result.sVar = se;
                break;
            default:
                throw new BASICSyntaxError("Unknown function on input.");

        }
        t = lt.nextToken();
        if (! t.isSymbol(')')) {
            throw new BASICSyntaxError("Missing closing parenthesis for function.");
        }
        return result;
    }
}

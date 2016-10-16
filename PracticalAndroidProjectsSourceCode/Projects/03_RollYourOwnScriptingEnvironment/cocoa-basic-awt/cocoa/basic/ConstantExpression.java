/*
 * ConstantExpression.java - An expression which is simply a constant.
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
import java.io.PrintStream;

/**
 * This class implements the simplest possible expression, a constant.
 */
class ConstantExpression extends Expression {
    private double v;
    private String strCons;

    ConstantExpression(double a) {
        super();
        v = a;
    }

    ConstantExpression(String a) {
        super();
        strCons = a;
    }

    void print(PrintStream p) {
        p.print((strCons == null) ? v+" " : strCons);
    }

    double value(Program pgm) throws BASICRuntimeError {
        if (strCons != null)
            return 0;
        return v;
    }

    String unparse() {
        if (strCons != null) {
            return ("\""+strCons+"\"");
        }
        return ""+v;
    }

    String stringValue(Program pgm, int c) throws BASICRuntimeError {
        if (strCons != null)
            return strCons;
        return ""+v;
    }

    String stringValue(Program pgm) throws BASICRuntimeError {
        if (strCons != null)
            return strCons;
        return ""+v;
    }

    boolean isString() {
        return (strCons != null);
    }

    public String toString() {
        if (strCons != null)
            return strCons;
        return v+" ";
    }
}

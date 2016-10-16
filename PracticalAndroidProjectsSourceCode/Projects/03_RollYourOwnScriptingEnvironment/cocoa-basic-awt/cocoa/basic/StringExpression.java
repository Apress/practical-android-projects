/*
 * StringExpression.java - parse and execute the string "expressions".
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

class StringExpression extends Expression {

    StringExpression(int op, Expression a, Expression b) throws BASICSyntaxError {
        super(op, a, b);
    }

    double value(Program pgm) throws BASICRuntimeError {
        switch (oper) {
            case OP_EQ:
                return (arg1.stringValue(pgm).compareTo(arg2.stringValue(pgm)) == 0) ? 1 : 0;
            case OP_NE:
                return (arg1.stringValue(pgm).compareTo(arg2.stringValue(pgm)) != 0) ? 1 : 0;
            case OP_LT:
                return (arg1.stringValue(pgm).compareTo(arg2.stringValue(pgm)) < 0) ? 1 : 0;
            case OP_LE:
                return (arg1.stringValue(pgm).compareTo(arg2.stringValue(pgm)) <= 0) ? 1 : 0;
            case OP_GT:
                return (arg1.stringValue(pgm).compareTo(arg2.stringValue(pgm)) > 0) ? 1 : 0;
            case OP_GE:
                return (arg1.stringValue(pgm).compareTo(arg2.stringValue(pgm)) >= 0) ? 1 : 0;
            default:
                return super.value(pgm);
        }
    }

    String stringValue(Program pgm, int c)  throws BASICRuntimeError {
        switch (oper) {
            case OP_ADD:
                String z = arg1.stringValue(pgm, c);
                int c2 = c + z.length();
                return z + arg2.stringValue(pgm, c2);
            default:
                throw new BASICRuntimeError("Unknown operator in string expression.");
        }
    }

    String stringValue(Program pgm) throws BASICRuntimeError {
        return stringValue(pgm, 0);
    }

    boolean isString() {
        return true;
    }
}
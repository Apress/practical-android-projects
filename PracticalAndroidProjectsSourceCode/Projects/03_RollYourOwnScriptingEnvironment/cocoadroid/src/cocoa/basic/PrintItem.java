/*
 * PrintItem.java - one "thing" to print in a print statement.
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

class PrintItem {
    final static int EXPRESSION = 0;
    final static int STRING_VARIABLE = 1;
    final static int STRING_CONSTANT = 2;
    final static int TAB = 3;
    final static int SEMI = 4;

    private int type;
    private Object thing;

    PrintItem(int t, Object o) {
        type = t;
        thing = o;
    }

    private static final String S_TAB = "\t";
    private static final String S_SEMI = "";

    String value(Program pgm, int c) throws BASICRuntimeError {
        switch (type) {
            case EXPRESSION :
                Expression e = (Expression)thing;
                if (thing instanceof BooleanExpression) {
                    double zz = ((Expression)thing).value(pgm);
                    return (zz == 1) ? "TRUE" : "FALSE";
                }
                if (e.isString()) {
                    return e.stringValue(pgm, c);
                }
                return (" "+e.value(pgm)+" ");
            case STRING_VARIABLE :
                return (pgm.getString((Variable)thing));
            case STRING_CONSTANT :
                return ((String)thing);
            case TAB:
                return (S_TAB);
            case SEMI:
                return(S_SEMI);
            default:
                return "BOGUS PRINTITEM";
        }
    }

    boolean needCR() {
        return ((type != TAB) && (type != SEMI));
    }

    String unparse() {
        switch (type) {
            case EXPRESSION :
                return ((Expression) thing).unparse();
            case STRING_VARIABLE:
                return ((Variable)thing).unparse();
            case STRING_CONSTANT:
                return ("\""+(String)thing+"\"");
            case TAB:
                return (", ");
            case SEMI:
                return ("; ");
        }
        return "BOGUS";
    }

    void print(PrintStream p) {
        switch (type) {
            case EXPRESSION :
                ((Expression) thing).print(p);
                return;
            case STRING_VARIABLE:
                p.print((String)thing);
                return;
            case STRING_CONSTANT:
                p.print("\""+(String)thing+"\"");
                return;
            case TAB:
                p.print(",");
                return;
            case SEMI:
                p.print(";");
                return;
        }
    }
}
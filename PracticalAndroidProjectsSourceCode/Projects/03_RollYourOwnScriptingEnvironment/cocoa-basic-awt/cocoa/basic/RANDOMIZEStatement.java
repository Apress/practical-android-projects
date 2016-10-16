/*
 * RANDOMIZEStatement.java - Implement the RANDOMIZE Statement.
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

/**
 * The RANDOMIZE statement.
 *
 * The RANDOMIZE statement seeds the random number generator. Syntax is:
 *      RANDOMIZE
 */
class RANDOMIZEStatement extends Statement {
    Expression nExpn;
    boolean useTimeOfDay = false;

    RANDOMIZEStatement(LexicalTokenizer lt) throws BASICSyntaxError {
        super(RANDOMIZE);
        Token t = lt.nextToken();
        switch (t.typeNum()) {
            case Token.OPERATOR:
            case Token.CONSTANT:
            case Token.VARIABLE:
                lt.unGetToken();
                nExpn = ParseExpression.expression(lt);
            case Token.KEYWORD:
                if (t.numValue() != TIMER)
                    throw new BASICSyntaxError("Badly formed randomize statement.");
                useTimeOfDay = true;
            default:
                lt.unGetToken();
        }
    }

    Statement doit(Program pgm, InputStream in, PrintStream out) throws BASICRuntimeError {
        if (nExpn != null) {
            pgm.randomize(nExpn.value(pgm));
        } else {
            pgm.randomize((double) System.currentTimeMillis());
        }
        return pgm.nextStatement(this);
    }

    String unparse() {
        if (nExpn != null)
            return "RANDOMIZE "+nExpn.unparse();
        return "RANDOMIZE";
    }

}

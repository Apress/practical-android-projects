/*
 * GOSUBStatement.java - Implement the GOSUB Statement.
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
 * The GOSUB statement.
 *
 * Like the GOTO statement, the GOSUB statement unconditionally tranfers
 * control to a non-linear sequence in the program. However, unlike GOTO
 * the position is remembered on the stack so that executing a RETURN
 * statement will return execution to the statement following the GOSUB.
 * The destination is indicated by a line number.
 *
 * Syntax :
 *      GOSUB line
 *
 * Syntax Errors:
 *      Line number required.
 *
 * Runtime Errors:
 *      Non-existent line number.
 */
class GOSUBStatement extends Statement {

    // This is the line number to transfer control too.
    int lineTarget;

    GOSUBStatement(LexicalTokenizer lt) throws BASICSyntaxError {
        super(GOSUB);

        parse(this, lt);
    }

    Statement doit(Program pgm, InputStream in, PrintStream out) throws BASICRuntimeError {
        Statement s;
        pgm.push(this);
        s = pgm.getStatement(lineTarget);
        if (s != null) {
            return s;
        }
        throw new BASICRuntimeError("GOSUB non-existent line "+lineTarget+".");
    }

    String unparse() {
        return "GOSUB "+lineTarget;
    }

    /**
     * Parse GOSUB Statement.
     */
    private static void parse(GOSUBStatement s, LexicalTokenizer lt) throws BASICSyntaxError {
        Token t = lt.nextToken();
        if (t.typeNum() != Token.CONSTANT) {
            throw new BASICSyntaxError("Line number required after GOSUB.");
        }
        s.lineTarget = (int) t.numValue();
    }

}

/*
 * DATAStatement.java - Implement the DATA Statement.
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

/**
 * The DATA Statement
 *
 * The DATA statement is the source of data for all subsequent READ
 * statements. A DATA statement defines one or more string or numeric
 * constants that are put into a FIFO buffer when the statement is
 * executed. READ statements then pull data out of this buffer and
 * put it into variables. The syntax of the DATA statement is:
 *      DATA    constant1, constant2, ..., constantN
 *
 * Syntax errors:
 *      Bogus value in DATA statement
 */
class DATAStatement extends Statement {

    Vector args;

    DATAStatement(LexicalTokenizer lt) throws BASICSyntaxError {
        super(DATA);

        parse(this, lt);
    }

    Statement doit(Program pgm, InputStream in, PrintStream out) throws BASICRuntimeError {
        for (int i = 0; i < args.size(); i++) {
            pgm.pushData((Token)args.elementAt(i));
        }
        return pgm.nextStatement(this);
    }

    String unparse() {
        StringBuffer sb = new StringBuffer();
        sb.append("DATA ");
        for (int i = 0; i < args.size(); i++) {
            Token t = (Token)args.elementAt(i);
            if (i < (args.size() - 1)) {
                sb.append(t.unparse()+", ");
            } else {
                sb.append(t.unparse());
            }
        }
        return sb.toString();
    }

    /**
     * Parse DATA Statement.
     */
    private static void parse(DATAStatement s, LexicalTokenizer lt) throws BASICSyntaxError {
        Token t;
        s.args = new Vector();

        while (true) {
            t = lt.nextToken();
            if (t.typeNum() == Token.CONSTANT) {
                s.args.addElement(t);
            } else if (t.typeNum() == Token.STRING) {
                s.args.addElement(t);
            } else if (t.isOp(Expression.OP_SUB)) {
                t = lt.nextToken();
                if (t.typeNum() != Token.CONSTANT)
                    throw new BASICSyntaxError("Bogus value in DATA statement.");
                t.negate();
                s.args.addElement(t);
            } else {
                lt.unGetToken();
                return;
            }
            t = lt.nextToken();
            if (t.typeNum() == Token.EOL) {
                return;
            } else if (! t.isSymbol(',')) {
                lt.unGetToken();
                return;
            }
        }
    }


}

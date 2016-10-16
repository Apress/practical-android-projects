/*
 * ONStatement.java - Implement the ON Statement.
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
import cocoa.util.RedBlackTree;
import java.util.Vector;

/**
 * The ON Statement
 *
 * There are two types of ON statements, ON - GOTO  and ON - GOSUB. Both
 * statements share a common syntax which is as follows:
 *      ON expr GOTO | GOSUB line1, line2, ..., lineN
 * The value of the expression is used to compute which line to transfer
 * control to. The result is zero based so an expression that resolves to
 * 0 will transfer control to 'line1', a value 1 will tranfer to 'line2' etc.
 * If the expression's value is out of range the next statement will be
 * executed. This is useful as the default case.
 *
 * Syntax errors:
 *      Numeric expression required here.
 *      ON statement requires GOTO or GOSUB.
 *      Line numbers must be separated by commas.
 *
 * Runtime errors:
 *      Illegal or non-existent destination line.
 *
 */

class ONStatement extends Statement {

    // This is the line number to transfer control too.
    Expression nExp;
    Vector args;

    ONStatement(LexicalTokenizer lt) throws BASICSyntaxError {
        super(ON_GOTO);

        parse(this, lt);
    }

    Statement doit(Program pgm, InputStream in, PrintStream out) throws BASICRuntimeError {
        Statement s;

        int select = (int) nExp.value(pgm);
        if ((select < 0) || (select >= args.size())) {
            return pgm.nextStatement(this);
        }
        Token z = (Token) args.elementAt(select);
        s = pgm.getStatement((int) z.numValue());
        if (s == null)
            throw new BASICRuntimeError("ON "+keywords[keyword]+" has illegal line target.");
        if (keyword == ON_GOSUB)
            pgm.push(this);
        return s;
    }

    String unparse() {
        StringBuffer sb = new StringBuffer();
        sb.append("ON ");
        sb.append(nExp.unparse()+" ");
        sb.append(keywords[keyword].toUpperCase()+" ");
        for (int i = 0; i < args.size(); i++) {
            Token t = (Token)(args.elementAt(i));
            if (i < (args.size()-1))
                sb.append(t.unparse()+", ");
            else
                sb.append(t.unparse());
        }
        return sb.toString();
    }

    RedBlackTree getVars() {
        RedBlackTree vv = new RedBlackTree();
        nExp.trace(vv);
        return vv;
    }

    /**
     * Parse ON Statement.
     */
    private static void parse(ONStatement s, LexicalTokenizer lt) throws BASICSyntaxError {
        Token t;

        s.nExp = ParseExpression.expression(lt);
        if (s.nExp.isString() || (s.nExp instanceof BooleanExpression)) {
            throw new BASICSyntaxError("Numeric expression required here.");
        }
        t = lt.nextToken();
        if ((t.typeNum() != Token.KEYWORD) ||
            ((t.numValue() != GOTO) && (t.numValue() != GOSUB))) {
            throw new BASICSyntaxError("On statement needs GOTO or GOSUB.");
        }

        // Check our assumption about the GOTOness of the statement.
        if (t.numValue() == GOSUB)
            s.keyword = ON_GOSUB;

        s.args = new Vector();
        while (true) {
            t = lt.nextToken();
            if (t.typeNum() != Token.CONSTANT) {
                break;
            }
            s.args.addElement(t);
            t = lt.nextToken();
            if (t.typeNum() == Token.EOL)
                break;
            if (! t.isSymbol(',')) {
                throw new BASICSyntaxError("LINE numbers should be separated by commas.");
            }
        }
        lt.unGetToken(); // back up the tokenizer;
        return;
    }
}

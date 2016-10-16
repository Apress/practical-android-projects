/*
 * LETStatement.java - Implement the LET Statement.
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

/**
 * The LET statement.
 *
 * The LET statement is the standard assignment statement in BASIC.
 * Technically its syntax is:
 *      LET var = expression
 * However we allow you to omit the LET or simply use
 *      var = expression.
 *
 * Syntax errors :
 *      missing = in assignment statement.
 *      string assignment needs string expression.
 *      Boolean expression not allowed in LET.
 *      unmatched parenthesis in LET statement.
 */
class LETStatement extends Statement {

    Variable myVar;
    Expression nExp;

    LETStatement(LexicalTokenizer lt) throws BASICSyntaxError {
        super(LET);

        parse(this, lt);
    }

    Statement doit(Program pgm, InputStream in, PrintStream out) throws BASICRuntimeError {
        if (myVar.isString()) {
            pgm.setVariable(myVar, nExp.stringValue(pgm));
        } else {
            pgm.setVariable(myVar, nExp.value(pgm));
        }
        return pgm.nextStatement(this);
    }

    String unparse() {
        StringBuffer sb = new StringBuffer();
        sb.append("LET ");
        sb.append(myVar.unparse());
        sb.append(" = ");
        sb.append(nExp.unparse());
        return sb.toString();
    }

    /**
     * Generate a trace record for the LET statement.
     */
    RedBlackTree getVars() {
        RedBlackTree vv = new RedBlackTree();
        nExp.trace(vv);
        return (vv);
    }


    /**
     * Parse LET Statement.
     */
    private static void parse(LETStatement s, LexicalTokenizer lt) throws BASICSyntaxError {
        Token t = lt.nextToken();
        Variable vi;


        if (t.typeNum() != Token.VARIABLE)
            throw new BASICSyntaxError("variable expected for LET statement.");

        vi = (Variable)t;
        s.myVar = (Variable) t;
        t = lt.nextToken();
        if (! t.isOp(Expression.OP_EQ))
            throw new BASICSyntaxError("missing = in assignment statement.");
        s.nExp = ParseExpression.expression(lt);
        if (s.myVar.isString() && ! s.nExp.isString()) {
            throw new BASICSyntaxError("String assignment needs string expression.");
        }
        if (s.nExp instanceof BooleanExpression) {
            throw new BASICSyntaxError("Boolean expression not allowed in LET.");
        }
        t = lt.nextToken();
        if (t.isSymbol(')'))
            throw new BASICSyntaxError("unmatched parenthesis in LET statement.");
        else
            lt.unGetToken();
        return;
    }
}

/*
 * IFStatement.java - Implement the IF Statement.
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
 * The IF Statement
 *
 * The IF statement provides for conditional change of control in
 * program execution. The IF statement has a boolean expression
 * associated with it that is evaluated at runtime. If the result
 * of that evaluation is a 'true' value, then control is transferred
 * to the statement after the THEN keyword, otherwise control is
 * transferred to the next <i>numbered</i> line. If multiple statements
 * follow the THEN keyword, they are executed in sequence but only
 * if the expression is true. The statement after the THEN keyword
 * may optionally be a line number which is shorthand for 'GOTO line'.
 * The syntax of the IF statement is:
 *          IF boolean THEN statement [ : stmt2 : ... : stmtN ]
 *          IF boolean THEN linenumber
 *
 * Syntax errors:
 *      Boolean expression required.
 *      Missing THEN keyword.
 *      Unparsable statement after THEN keyword.
 *      Illegal line number following THEN.
 *
 * Runtime errors:
 *      Illegal line number following THEN.
 */
class IFStatement extends Statement {

    // This is the line number to transfer control too.
    int lineTarget;
    Expression nExp;
    Statement thenClause;

    IFStatement(LexicalTokenizer lt) throws BASICSyntaxError {
        super(IF);

        parse(this, lt);
    }

    Statement doit(Program pgm, InputStream in, PrintStream out) throws BASICRuntimeError {
        double v;
        Statement s;

        v = nExp.value(pgm);
        if (v == 1.0) {
            if (thenClause != null) {
                return thenClause;
            } else {
                s = pgm.getStatement(lineTarget);
                if (s != null)
                    return s;
                throw new BASICRuntimeError("Illegal line number following THEN.");
            }
        }
        return pgm.nextStatement(this);
    }

    String unparse() {
        StringBuffer sb = new StringBuffer();
        Statement qq;

        sb.append("IF ");
        sb.append(nExp.unparse());
        sb.append(" THEN ");
        if (thenClause == null) {
            sb.append(lineTarget+"");
            return sb.toString();
        }
        for (qq = thenClause; qq != null; qq = qq.nxt) {
            sb.append(qq.unparse());
            if (qq.nxt != null)
                sb.append(" : ");
        }
        return sb.toString();
    }

    RedBlackTree getVars() {
        RedBlackTree vv = new RedBlackTree();
        nExp.trace(vv);
        return vv;
    }

    /**
     * This method verifies that the expression *isn't* boolean. This is
     * perhaps an arbitrary restriction, but part of the spec none-the-less.
     */
    private static void noBool(Expression a) throws BASICSyntaxError {
        if (a instanceof BooleanExpression)
            throw new BASICSyntaxError("Boolean expression not allowed here.");
    }

    /**
     * Parse IF Statement.
     */
    private static void parse(IFStatement s, LexicalTokenizer lt) throws BASICSyntaxError {
        s.nExp = ParseExpression.expression(lt);
        if (!(s.nExp instanceof BooleanExpression))
            throw new BASICSyntaxError("Boolean expression required for IF.");
        Token t = lt.nextToken();
        if (t.isSymbol(')')) {
            throw new BASICSyntaxError("Mismatched parenthesis.");
        } else if ((t.typeNum() != Token.KEYWORD) || (t.numValue() != THEN)) {
            throw new BASICSyntaxError("Missing THEN keyword in IF statement.");
        }
        t = lt.nextToken();
        if (t.typeNum() == Token.CONSTANT) {
            int li = (int) t.numValue();
            if (li <= 0) {
                throw new BASICSyntaxError("Invalid line number following THEN.");
            }
            s.thenClause = null;
            s.lineTarget = (int) t.numValue();
            return;
        }
        s.lineTarget = -1; // Not a line number based IF statement
        lt.unGetToken(); // put back this token.
        s.thenClause = ParseStatement.statement(lt);
        if (s.thenClause == null)
            throw new BASICSyntaxError("Unparsable statement after THEN.");
        return;
    }

    /**
     * Update line number information in this statement. Used to determine the next
     * line to execute.
     */
    public void addLine(int l) {
        line = l;
        if (nxt != null)
            nxt.addLine(l);
        if (thenClause != null)
            thenClause.addLine(l);
    }


}

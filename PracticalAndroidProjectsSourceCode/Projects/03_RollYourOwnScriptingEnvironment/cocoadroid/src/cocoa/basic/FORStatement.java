/*
 * FORStatement.java - Implement the FOR Statement.
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
 * The FOR Statement
 *
 * The FOR statement is the BASIC language ITERATOR construct. The
 * FOR statement defines a control variable that is initialized to
 * an initial value, then the statements after the FOR statement and
 * before a matching NEXT statement are executed. When the NEXT
 * statement is encountered control returns to the FOR statement where
 * the STEP expression is added to the control variable. The result
 * is compared to the ending expression and if the control variable's
 * value has either passed the ending expression's value, or it has
 * moved farther away from the starting positions value, control
 * passes to the statement after the NEXT statement, otherwise execution
 * continues with the statement following the FOR statement. The
 * syntax for the FOR statement is as follows:
 *          FOR var = startExpr TO endExpr [ STEP stepExpr ]
 * If the STEP keyword is omitted the default STEP expression is the
 * constant 1.
 *
 * Syntax Errors:
 *      Missing variable in FOR statement.
 *      Numeric variable required for FOR statement.
 *      Missing = in FOR statement.
 *      Missing TO in FOR statement.
 */
class FORStatement extends Statement {

    // This is the line number to transfer control too.
    int lineTarget;
    Expression nExp;
    Expression eExp;
    Expression sExp;
    Variable myVar;

    FORStatement(LexicalTokenizer lt) throws BASICSyntaxError {
        super(FOR);

        parse(this, lt);
    }

    Statement doit(Program pgm, InputStream in, PrintStream out) throws BASICRuntimeError {
        pgm.setVariable(myVar, nExp.value(pgm));
        pgm.push(this);
        return pgm.nextStatement(this);
    }

    String unparse() {
        StringBuffer sb = new StringBuffer();
        sb.append("FOR ");
        sb.append(myVar.unparse());
        sb.append(" = ");
        sb.append(nExp.unparse());
        sb.append(" TO ");
        sb.append(eExp.unparse());
        if (sExp != null) {
            sb.append(" STEP ");
            sb.append(sExp.unparse());
        }
        return sb.toString();
    }

    /**
     * Collect the variables associated with the execution of this statement.
     */
    RedBlackTree getVars() {
        RedBlackTree vv = new RedBlackTree();
        nExp.trace(vv);
        eExp.trace(vv);
        if (sExp != null)
            sExp.trace(vv);
        return vv;
    }

    private static boolean noBool(Expression e) {
        return (! (e instanceof BooleanExpression));
    }

    /**
     * Parse FOR Statement.
     */
    private static void parse(FORStatement s, LexicalTokenizer lt) throws BASICSyntaxError {
        Token t = lt.nextToken();

        if (t.typeNum() != Token.VARIABLE) {
            throw new BASICSyntaxError("Missing variable in FOR statement");
        }

        s.myVar = (Variable) t;
        if (s.myVar.isString())
            throw new BASICSyntaxError("Numeric variable required for FOR statement.");

        t = lt.nextToken();
        if (! t.isOp(Expression.OP_EQ)) {
            throw new BASICSyntaxError("Missing = in FOR statement");
        }
        s.nExp = ParseExpression.expression(lt);
        noBool(s.nExp);
        t = lt.nextToken();
        if ((t.typeNum() != Token.KEYWORD) || (t.numValue() != TO)) {
            throw new BASICSyntaxError("Missing TO in FOR statement.");
        }
        s.eExp = ParseExpression.expression(lt);
        noBool(s.eExp);
        t = lt.nextToken();
        if ((t.typeNum() != Token.KEYWORD) || (t.numValue() != STEP)) {
            lt.unGetToken();
            s.sExp = new ConstantExpression(1.0);
            return;
        }
        s.sExp = ParseExpression.expression(lt);
        noBool(s.sExp);
        return;
    }

}

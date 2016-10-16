/*
 * Expression.java - Parse and evaluate expressions for BASIC.
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

import cocoa.util.RedBlackTree;

/**
 * This is the base class for BASIC expressions.
 *
 * Expressions are parsed by the class <b>ParseExpression</b> and creates
 * a parse tree using objects of type expression. The subclasses
 * <b>ConstantExpression</b>, <b>VariableExpression</b>, and <b>FunctionExpression</b>
 * hold specific types of indivisable elements. The class <b>BooleanExpression</b> is
 * used to point to boolean expressions. This distinction on booleans allows us to do
 * some syntax checking and statements like the IF statement can verify their expression
 * is a boolean one.
 *
 * See the class ParseExpression for the grammar and precidence rules.
 */

public class Expression {
    Expression arg1, arg2;
    int oper;

    /**
     * These are the valid operator types.
     */

    final static int OP_ADD  = 1;   // Addition '+'
    final static int OP_SUB  = 2;   // Subtraction '-'
    final static int OP_MUL  = 3;   // Multiplication '*'
    final static int OP_DIV  = 4;   // Division '/'
    final static int OP_EXP  = 5;   // Exponentiation '**'
    final static int OP_AND  = 6;   // Bitwise AND '&'
    final static int OP_IOR  = 7;   // Bitwise inclusive OR '|'
    final static int OP_XOR  = 8;   // Bitwise exclusive OR '^'
    final static int OP_NOT  = 9;   // Unary negation '!'
    final static int OP_EQ   = 10;  // Equality '='
    final static int OP_NE   = 11;  // Inequality '<>'
    final static int OP_LT   = 12;  // Less than '<'
    final static int OP_LE   = 13;  // Less than or equal '<='
    final static int OP_GT   = 14;  // Greater than '>'
    final static int OP_GE   = 15;  // Greater than or equal '>='
    final static int OP_BAND = 16;  // Boolean AND '.AND.'
    final static int OP_BIOR = 17;  // Boolean inclusive or '.OR.'
    final static int OP_BXOR = 18;  // Boolean exclusive or '.XOR.'
    final static int OP_BNOT = 19;  // Boolean negation '.NOT.'
    final static int OP_NEG  = 20;  // Unary minus

    final static String opVals[] = {
        "<NULL>", "+", "-", "*", "/", "**", "&", "|", "^", "!", "=", "<>",
        "<", "<=", ">", ">=", ".AND.", ".OR.", ".XOR.", ".NOT.", "-",
    };

    protected Expression() {
    }

    final static String typeError =
        "Expression: cannot combine boolean term with arithmetic term.";

    /**
     * Create a new expression.
     */
    protected Expression(int op, Expression a, Expression b) throws BASICSyntaxError {
        arg1 = a;
        arg2 = b;
        oper = op;
        /*
         * If the operator is a boolean, both arguments must be boolean.
         */
        if (op > OP_GE) {
            if ( (! (arg1 instanceof BooleanExpression)) ||
                 (! (arg2 instanceof BooleanExpression)) )
                throw new BASICSyntaxError(typeError);
        } else {
            if ((arg1 instanceof BooleanExpression) || (arg2 instanceof BooleanExpression))
                throw new BASICSyntaxError(typeError);
        }
    }

    /**
     * Create a unary expression.
     */
    protected Expression(int op, Expression a) throws BASICSyntaxError {
        arg2 = a;
        oper = op;
        if ((oper == OP_BNOT) && (! (arg2 instanceof BooleanExpression)))
            throw new BASICSyntaxError(typeError);
    }

    void print(PrintStream p) {
        p.print("(");
        // unary expressions don't have an arg1.
        if (arg1 != null)
            arg1.print(p);
        p.print(opVals[oper]);
        arg2.print(p);
        p.print(")");
        return;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("(");
        if (arg1 != null)
            sb.append(arg1.toString());
        sb.append(opVals[oper]);
        sb.append(arg2.toString());
        sb.append(")");
        return sb.toString();
    }

    String unparse() {
        StringBuffer sb = new StringBuffer();
        sb.append("(");
        if (arg1 != null) {
            sb.append(arg1.unparse());
            sb.append(" "+opVals[oper]+" ");
        }
        sb.append(arg2.unparse());
        sb.append(")");
        return sb.toString();
    }

    /**
     * Generate a set of trace records for this expression. All of the
     * variables in the expression are added to the tracer vector.
     */
    void trace(RedBlackTree tracer) {
        if (arg1 != null)
            arg1.trace(tracer);
        if (arg2 != null)
            arg2.trace(tracer);
    }


    /**
     * This method evaluates the expression in the context of the
     * passed in program. It throws runtime errors for things like
     * no such variable and divide by zero.
     *
     * Note that for boolean operations the value 1.0 == true and
     * the value 0.0 is equivalent to false.
     */
    double value(Program pgm) throws BASICRuntimeError {
        switch (oper) {
            case OP_ADD :
                return arg1.value(pgm) + arg2.value(pgm);

            case OP_SUB :
                return arg1.value(pgm) - arg2.value(pgm);

            case OP_MUL :
                return arg1.value(pgm) * arg2.value(pgm);

            case OP_DIV :
                if (arg2.value(pgm) == 0) {
                    throw new BASICRuntimeError("divide by zero!");
                }
                return arg1.value(pgm) / arg2.value(pgm);

            case OP_XOR :
                return (double)(((long) arg1.value(pgm)) ^ ((long) arg2.value(pgm)));

            case OP_IOR :
                return (double)(((long) arg1.value(pgm)) | ((long) arg2.value(pgm)));

            case OP_AND :
                return (double)(((long) arg1.value(pgm)) & ((long) arg2.value(pgm)));

            case OP_EXP :
                return (Math.pow(arg1.value(pgm), arg2.value(pgm)));

            case OP_EQ :
                return (arg1.value(pgm) == arg2.value(pgm)) ? 1.0 : 0.0;

            case OP_NE :
                return (arg1.value(pgm) != arg2.value(pgm)) ? 1.0 : 0.0;

            case OP_LT :
                return (arg1.value(pgm) < arg2.value(pgm)) ? 1.0 : 0.0;

            case OP_LE :
                return (arg1.value(pgm) <= arg2.value(pgm)) ? 1.0 : 0.0;

            case OP_GT :
                return (arg1.value(pgm) > arg2.value(pgm)) ? 1.0 : 0.0;

            case OP_GE :
                return (arg1.value(pgm) >= arg2.value(pgm)) ? 1.0 : 0.0;

            case OP_BAND :
                return ((arg1.value(pgm) == 1.0) && (arg2.value(pgm) == 1.0)) ? 1.0 : 0.0;

            case OP_BIOR :
                return ((arg1.value(pgm) == 1.0) || (arg2.value(pgm) == 1.0)) ? 1.0 : 0.0;

            case OP_BXOR :
                return ((arg1.value(pgm) == 1.0) ^ (arg2.value(pgm) == 1.0)) ? 1.0 : 0.0;

            case OP_BNOT :
                return (arg2.value(pgm) == 1.0) ? 0.0 : 1.0;

            case OP_NOT :
                return (double)(~((long)(arg2.value(pgm))));

            case OP_NEG :
                return 0 - arg2.value(pgm);

            default:
                throw new BASICRuntimeError("Illegal operator in expression!");

        }
    }

    String stringValue(Program pgm, int c) throws BASICRuntimeError {
        System.out.println("This is not a string expression?");
        throw new BASICRuntimeError("No String representation for this.");
    }

    String stringValue(Program pgm) throws BASICRuntimeError {
        throw new BASICRuntimeError("No String representation for this.");
    }

    boolean isString() { return false; }
}

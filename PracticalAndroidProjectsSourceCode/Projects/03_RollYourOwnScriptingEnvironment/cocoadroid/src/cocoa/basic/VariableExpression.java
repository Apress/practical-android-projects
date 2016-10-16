/*
 * VariableExpression.java - An expression consisting of a variable.
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
 * This class implements an expression that is simply a variable. Or
 * more correctly the value of that variable.
 */
class VariableExpression extends Expression {
    private Variable v;

    VariableExpression(Variable a) {
        super();
        v = a;
    }

    void print(PrintStream p) {
        p.print(v.toString());
    }

    double value(Program pgm) throws BASICRuntimeError {
        if (v.isString())
            return 0;
        return (pgm.getVariable(v));
    }

    String stringValue(Program pgm, int c) throws BASICRuntimeError {
        if (v.isString())
            return pgm.getString(v);
        return (""+pgm.getVariable(v));
    }

    String stringValue(Program pgm) throws BASICRuntimeError {
        if (v.isString())
            return pgm.getString(v);
        return (""+pgm.getVariable(v));
    }

    String unparse() {
        return v.unparse();
    }

    /**
     * Add the value of this variable to the trace record.
     */
    void trace(RedBlackTree tracer) {
        tracer.put(v.name, this);
        if (v.isArray() && (v.numExpn() != 0)) {
            for (int i=0; i < v.numExpn(); i++) {
                (v.expn(i)).trace(tracer);
            }
        }
    }

    boolean isString() {
        return (v.isString());
    }

    public String toString() {
        return v.toString();
    }
}

class Traceable {
    String name;
    String value;

    Traceable(String n, String v) {
        name = n;
        value = v;
    }

    public String toString() {
        return ("  : "+name+" = "+value);
    }
}
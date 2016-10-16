/*
 * Variable.java - A class for representing variables.
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

/**
 * This class is the Variable object. There are effectively four kinds
 * of variables in this version of BASIC, numbers, strings, number arrays
 * and string arrays.
 *
 * The class is a subclass of Token so that we can pass these back from
 * the LexicalTokenizer class and the program parser is easier.
 */
class Variable extends Token {

    // Legal variable sub types
    final static int NUMBER = 0;
    final static int STRING  = 1;
    final static int NUMBER_ARRAY = 2;
    final static int STRING_ARRAY = 4;

    String name;
    int subType;

    /*
     * If the variable is in the symbol table these values are
     * initialized.
     */
    int ndx[];  // array indices.
    int mult[]; // array multipliers
    double nArrayValues[];
    String sArrayValues[];

    /*
     * If this is a variable array reference, these expressions are
     * initialized.
     */
    Expression expns[];

    /**
     * Create a reference to this array.
     */
    Variable(String someName, Expression ee[]) {
        type = VARIABLE;
        if (someName.endsWith("$")) {
            subType = STRING_ARRAY;
        } else {
            subType = NUMBER_ARRAY;
        }
        name = someName;
        expns = ee;
    }

    /**
     * Create a reference or scalar symbol table entry for this variable.
     */
    Variable(String someName) {
        type = VARIABLE;
        if (someName.endsWith("$")) {
            subType = STRING;
        } else {
            subType = NUMBER;
        }
        name = someName;
    }

    /**
     * Return a string represention of this variables "name"
     *
     * The unparse functions are used to reconstruct the source from the
     * parse tree. This can be very useful debugging information and is
     * used in the trace function.
     */
    String unparse() {
        if (! isArray()) {
            return name;
        }

        StringBuffer sb = new StringBuffer();
        sb.append(name);
        sb.append("( ");
        if (ndx != null) {
            for (int i = 0; i < ndx.length; i++) {
                if (i < (ndx.length-1))
                    sb.append(ndx[i]+", ");
                else
                    sb.append(ndx[i]+")");
            }
        } else {
            for (int i = 0; i < expns.length; i++) {
                if (i < (expns.length-1))
                    sb.append(expns[i].unparse()+", ");
                else
                    sb.append(expns[i].unparse()+")");
            }
        }
        return sb.toString();
    }

    /**
     * Create a symbol table entry for this array.
     */
    Variable(String someName, int ii[]) {
        int offset;
        ndx = ii;
        mult = new int[ii.length];
        mult[0] = 1;
        offset = ii[0];
        for (int i = 1; i < ii.length; i++) {
            mult[i] = mult[i-1] * ii[i];
            offset = offset * ii[i];
        }
        name = someName;
        type = VARIABLE;
        if (name.endsWith("$")) {
            sArrayValues = new String[offset];
            subType = STRING_ARRAY;
        } else {
            nArrayValues = new double[offset];
            subType = NUMBER_ARRAY;
        }
    }

    /**
     * This method takes an array of indices and computes a linear
     * offset into the storage array. If either the number of
     * indices are different, or there values exceed the max value
     * here in the symbol table entry, a runtime error is thrown.
     */
    private int computeIndex(int ii[]) throws BASICRuntimeError {
        int offset = 0;
        if ((ndx == null) || (ii.length != ndx.length))
            throw new BASICRuntimeError("Wrong number of indices.");

        for (int i = 0; i < ndx.length; i++) {
            if ((ii[i] < 1) || (ii[i] > ndx[i]))
                throw new BASICRuntimeError("Index out of range.");
            offset = offset +  (ii[i]-1) * mult[i];
        }
        return offset;
    }

    int numIndex() {
        if (! isArray())
            return 0;

        if (ndx != null)
            return ndx.length;
        if (expns != null)
            return expns.length;
        return 0;
    }

    double numValue(int ii[]) throws BASICRuntimeError {
        return nArrayValues[computeIndex(ii)];
    }

    /**
     * Returns value as a string, even if this internally is
     * a number.
     */
    String stringValue(int ii[]) throws BASICRuntimeError {
        if (subType == NUMBER_ARRAY)
            return ""+nArrayValues[computeIndex(ii)];
        return sArrayValues[computeIndex(ii)];
    }

    /**
     * Override token's stringValue() method to return string version
     * of numeric value if necessary.
     */
    String stringValue() {
        if (subType == NUMBER) {
            return ""+nValue;
        }
        return sValue;
    }

    /**
     * Set this variables value in the symbol table.
     */
    void setValue(double v) {
        nValue = v;
    }

    /**
     * Set this variables string value.
     */
    void setValue(String s) {
        sValue = s;
    }

    void setValue(double v, int ii[]) throws BASICRuntimeError {
        int offset = computeIndex(ii);
        if (nArrayValues == null) {
            throw new BASICRuntimeError("ARRAY storage not initialized.");
        }
        nArrayValues[offset] = v;
    }

    void setValue(String v, int ii[]) throws BASICRuntimeError {
        int offset = computeIndex(ii);
        if (sArrayValues == null) {
            throw new BASICRuntimeError("ARRAY storage not initialized.");
        }
        sArrayValues[offset] = v;
    }

    /**
     * Return true if this variable holds a string value.
     */
    boolean isString() {
        return name.endsWith("$");
    }

    boolean isArray() {
        return (subType == NUMBER_ARRAY) || (subType == STRING_ARRAY);
    }

    int numExpn() {
        if (expns == null)
            return 0;
        return expns.length;
    }

    Expression expn(int i) {
        return expns[i];
    }

    /**
     * just print the variable's name.
     */
    public String toString() {
        return ("Variable TOKEN : type = "+subType+", name = "+name);
    }
}

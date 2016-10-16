/*
 * BASICRuntimeError.java
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
 * Thrown by the parser if it can't parse an input line.
 */
public class BASICRuntimeError extends BASICError {
    /**
     * A runtime error with message <i>errorMessage</i>
     */
    public BASICRuntimeError(String errorMessage) {
        super(errorMessage);
    }

    BASICRuntimeError(Statement thisStatement, String errorMessage) {
        super(thisStatement, errorMessage);
    }

    /**
     * Return the runtime error message.
     */
    public String getMsg() { return "Runtime Error: "+super.getMsg(); }
}
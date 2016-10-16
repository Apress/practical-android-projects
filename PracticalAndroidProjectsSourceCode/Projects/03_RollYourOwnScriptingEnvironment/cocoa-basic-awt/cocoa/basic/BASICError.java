/*
 * BASICError.java
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
 * This is the base class for errors thrown by BASIC. If you catch it
 * then you will catch all exceptions possible.
 *
 * @see BASICSyntaxError
 * @see BASICRuntimeError
 */
public class BASICError extends Exception {
    String msg = "None.";
    Statement s = null;

    /** A new runtime error with message <i>errorMessage</i>. */
    public BASICError(String errorMessage) {
        super(errorMessage);
        msg = errorMessage;
    }

    /**
     * A runtime error that occurred in <i>thisStatement</i>.
     */
    BASICError(Statement thisStatement, String errorMessage) {
        super(errorMessage);
        msg = errorMessage;
        s = thisStatement;
    }

    /**
     * Once caught, you can use this method to get a string representation
     * of the error you've caught.
     */
    public String getMsg() {
        if (s != null)
            return (msg+"\n At line :"+s.asString());
        return msg;
    }
}
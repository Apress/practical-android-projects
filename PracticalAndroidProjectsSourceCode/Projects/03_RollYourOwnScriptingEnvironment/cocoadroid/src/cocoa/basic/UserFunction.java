/*
 * UserFunction.java - User Function interface.
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

import java.util.Vector;

/**
 * This interface defines the methods a class needs to implement to
 * support user functions.
 */
public interface UserFunction {
    /**
     * Return a user specified "id" number for a function with this
     * name.
     */
    int validFunction(String keyword);

    /**
     * Return an argument signature for your user function. This is
     * in the form of "[N | S ] [ , [N|S]] [, ...]" where 'N' means
     * number required and 'S' means string required.
     */
    String getSignature(int id);

    /**
     * This is the actual function call from cocoa.basic. It will be called
     * and in 'id' will be passed your function id, and in 'args' will
     * be passed 'Double' and 'String' objects with your parameters.
     * You will be expected to return a String object if your function
     * name ends with $ and a Double object if your function name does
     * not end in $. You can also return a BASICRuntimerError object which
     * will be thrown for you when it is received.
     */
    Object doit(int id, Vector args);
}
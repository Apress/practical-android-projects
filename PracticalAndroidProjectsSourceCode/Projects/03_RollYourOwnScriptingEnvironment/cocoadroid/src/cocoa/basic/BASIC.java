/*
 * BASIC.java -  BASIC Interpreter in Java.
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

import java.io.*;
import cocoa.dlib.*;

public class BASIC
{
    public static void main(String args[])
    {
        char data[] = new char[256];
        /* [pieter][20101125] */ //LexicalTokenizer lt = new LexicalTokenizer(data);
        /* [pieter][20101125] */ //Console con;
        /* [pieter][20101125] */ //ConsoleWindow cw = new ConsoleWindow("Java BASIC 1.0");
        /* [pieter][20101125] */
        ConsoleWindow cw = new ConsoleWindow("Java BASIC 1.x");

        CommandInterpreter ci = new CommandInterpreter(
                cw.DataInputStream(),
                cw.PrintStream());
        try {
            ci.start();
        }
        catch (Exception e) {
            System.out.println("Caught an Exception :");
            e.printStackTrace();
            try {
                System.out.println("Press enter to continue.");
                int c = System.in.read();
            }
            catch (IOException xx) {
                /* pass */
            }
        }
    }
}

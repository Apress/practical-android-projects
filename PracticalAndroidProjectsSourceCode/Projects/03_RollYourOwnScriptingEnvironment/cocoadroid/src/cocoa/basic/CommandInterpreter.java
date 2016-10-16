/*
 * CommandInterpreter.java -  Provide the cocoa.basic command line interface.
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

/**
 * This class is an "interactive" BASIC environment. You can think of it as
 * BASIC debug mode. Using the streams you passed in to create the object, it
 * hosts an interactive session allowing the user to enter BASIC programs, run
 * them, save them, and load them.
 */
public class CommandInterpreter
{
    protected DataInputStream inStream;
    protected PrintStream outStream;

    final static String commands[] = {
            "new", "run", "list", "cat", "del", "resume",
            "bye", "save", "load", "dump", "cont",
    };

    protected final int CMD_NEW = 0;
    final int CMD_RUN = 1;
    final int CMD_LIST = 2;
    final int CMD_CAT = 3;
    final int CMD_DEL = 4;
    final int CMD_RESUME = 5;
    protected final int CMD_BYE = 6;
    final int CMD_SAVE = 7;
    final int CMD_LOAD = 8;
    final int CMD_DUMP = 9;
    final int CMD_CONT = 10;

    /**
     * Create a new command interpreter attached to the passed
     * in streams.
     */
    public CommandInterpreter(InputStream in, OutputStream out)
    {
        if (in instanceof DataInputStream)
        {
            inStream = (DataInputStream) in;
        }
        else
        {
            inStream = new DataInputStream(in);
        }
        if (out instanceof PrintStream)
        {
            outStream = (PrintStream) out;
        }
        else
        {
            outStream = new PrintStream(out);
        }
    }

    /**
     * This method basically dispatches the commands of the command
     * interpreter.
     */
    protected Program processCommand(Program pgm, LexicalTokenizer lt, Token x) {
        Token t;
        Statement s = null;

        switch ((int) x.numValue()) {
            case CMD_RESUME:
                try {
                    pgm.resume(inStream, outStream);
                } catch (BASICRuntimeError e) {
                    outStream.println(e.getMsg());
                }
                return pgm;
            case CMD_CONT:
                try {
                    pgm.cont(inStream, outStream);
                } catch (BASICRuntimeError e) {
                    outStream.println(e.getMsg());
                }
                return pgm;

            case CMD_RUN:
                try {
                    pgm.run(inStream, outStream);
                } catch (BASICRuntimeError e2) {
                    outStream.println(e2.getMsg());
                }
                return pgm;

            case CMD_SAVE:
                t = lt.nextToken();
                if (t.typeNum() != Token.STRING) {
                    outStream.println("File name expected for SAVE Command.");
                    return pgm;
                }
                outStream.println("Saving file...");
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(t.stringValue());
                } catch (IOException except) { return pgm; }
                PrintStream pp = new PrintStream(fos);
                pgm.list(pp);
                pp.flush();
                try {
                    fos.close();
                } catch (IOException except) { return pgm; }
                return pgm;

            case CMD_LOAD:
                t = lt.nextToken();
                if (t.typeNum() != Token.STRING) {
                    outStream.println("File name expected for LOAD command.");
                }
                try {
                    pgm = Program.load(t.stringValue(), outStream);
                    outStream.println("File loaded.");
                } catch (IOException e) {
                    outStream.println("File "+t.stringValue()+" not found.");
                    return pgm;
                } catch (BASICSyntaxError bse) {
                    outStream.println("Syntax error reading file.");
                    outStream.println(bse.getMsg());
                    return pgm;
                }
                return pgm;
            case CMD_DUMP:
                PrintStream zzz = outStream;
                t = lt.nextToken();
                if (t.typeNum() == Token.STRING) {
                    try {
                        zzz = new PrintStream(new FileOutputStream(t.stringValue()));
                    } catch (IOException ii) { }
                }
                pgm.dump(zzz);
                if (zzz != outStream)
                    zzz.close();
                return pgm;

            case CMD_LIST:
                t = lt.nextToken();
                if (t.typeNum() == Token.EOL) {
                    pgm.list(outStream);
                } else if (t.typeNum() == Token.CONSTANT) {
                    int strt = (int) t.numValue();
                    t = lt.nextToken();
                    if (t.typeNum() == Token.EOL) {
                        pgm.list(strt, outStream);
                    } else if (t.isSymbol(',')) {
                        t = lt.nextToken();
                        if (t.typeNum() != Token.CONSTANT) {
                            outStream.println("Illegal parameter to LIST command.");
                            outStream.println(lt.showError());
                            return pgm;
                        }
                        int e = (int) t.numValue();
                        pgm.list(strt, e, outStream);
                    } else {
                        outStream.println("Syntax error in LIST command.");
                        outStream.println(lt.showError());
                    }
                } else {
                    outStream.println("Syntax error in LIST command.");
                    outStream.println(lt.showError());
                }
                return pgm;
        }
        outStream.println("Command not implemented.");
        return pgm;
    }

    protected char[] data = new char[256];

    /**
     * Starts the interactive session. When running the user should see the
     * "Ready." prompt. The session ends when the user types the <code>bye</code>
     * command.
     */
    public void start() {
        LexicalTokenizer lt = new LexicalTokenizer(data);
        Program pgm = new Program();
        DataInputStream dis = inStream;
        String lineData;

        outStream.println(
                "JavaBASIC Version 1.0 Copyright (C) 1996 Chuck McManis. All Rights Reserved.");

        while (true) {
            Statement s = null;
            try {
                lineData = dis.readLine();
            } catch (IOException ioe) {
                outStream.println("Caught an IO exception reading the input stream!");
                return; }

            // exit on eof of the input stream
            if (lineData == null)
                return;

            // ignore blank lines.
            if (lineData.length() == 0)
                continue;

            lt.reset(lineData);

            if (! lt.hasMoreTokens())
                continue;

            Token t = lt.nextToken();
            switch (t.typeNum()) {
                /*
                 * Process one of the command interpreter's commands.
                 */
                case Token.COMMAND :
                    if (t.numValue() == CMD_BYE)
                        return;
                    else if (t.numValue() == CMD_NEW) {
                        pgm = new Program( );
                        System.gc();
                        break;
                    } else {
                        pgm = processCommand(pgm, lt, t);
                    }
                    outStream.println("Ready.\n");
                    break;

                /*
                 * Process an initial number, it can be a new statement line
                 * or it may be an implicit delete command.
                 */
                case Token.CONSTANT :
                    Token peek = lt.nextToken();
                    if (peek.typeNum() == Token.EOL) {
                        pgm.del((int) t.numValue());
                        break;
                    } else {
                        lt.unGetToken();
                    }
                    try {
                        s = ParseStatement.statement(lt);
                        s.addText(lineData);
                        s.addLine((int) t.numValue());
                        pgm.add((int) t.numValue(), s);
                    } catch (BASICSyntaxError e) {
                        outStream.println("Syntax Error : "+e.getMsg());
                        outStream.println(lt.showError());
                        continue;
                    }
                    break;

                /*
                 * If initially it is a variable or a statement keyword then it
                 * must be an 'immediate' line.
                 */
                case Token.VARIABLE :
                case Token.KEYWORD : // immediate mode
                case Token.SYMBOL :
                    lt.unGetToken();
                    try {
                        s = ParseStatement.statement(lt);
                        do {
                            s = s.execute(pgm, inStream, outStream);
                        } while (s != null);

                    } catch (BASICSyntaxError e) {
                        outStream.println("Syntax Error : "+e.getMsg());
                        outStream.println(lt.showError());
                        continue;
                    } catch (BASICRuntimeError er) {
                        outStream.println("RUNTIME ERROR.");
                        outStream.println(er.getMsg());
                    }
                    break;

                /*
                 * Blank lines are ignored.
                 */
                case Token.EOL :
                    break;

                /*
                 * Anything else is an error.
                 */
                default:
                    outStream.println("Error, command not recognized.");
                    break;
            }
        }
    }
}

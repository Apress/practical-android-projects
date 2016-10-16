package com.pietergreyling.android.cocoadroid;

import cocoa.basic.*;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 */
public class CocoaDroidCommandInterpreter extends CommandInterpreter
{
    /**
     * Create a new command interpreter attached to the passed
     * in streams.
     */
    public CocoaDroidCommandInterpreter(InputStream in, OutputStream out)
    {
        super(in, out);
    }

    public void eval()
    {
        LexicalTokenizer lt = new LexicalTokenizer(data);
        Program pgm = new Program();
        DataInputStream dis = inStream;
        String lineData;

        while (true) {
            Statement s = null;
            try {
                lineData = dis.readLine();
            }
            catch (IOException ioe) {
                outStream.println("Caught an IO exception reading the input stream!");
                return;
            }

            // exit on eof of the input stream
            if (lineData == null)
                return;

            // ignore blank lines.
            if (lineData.length() == 0)
                continue;

            lt.reset(lineData);

            if (!lt.hasMoreTokens())
                continue;

            Token t = lt.nextToken();
            switch (t.typeNum()) {
                /*
                 * Process one of the command interpreter's commands.
                 */
                case Token.COMMAND:
                    if (t.numValue() == CMD_BYE)
                        return;
                    else if (t.numValue() == CMD_NEW) {
                        pgm = new Program();
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
                case Token.CONSTANT:
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
                    }
                    catch (BASICSyntaxError e) {
                        outStream.println("Syntax Error : " + e.getMsg());
                        outStream.println(lt.showError());
                        continue;
                    }
                    break;

                /*
                 * If initially it is a variable or a statement keyword then it
                 * must be an 'immediate' line.
                 */
                case Token.VARIABLE:
                case Token.KEYWORD: // immediate mode
                case Token.SYMBOL:
                    lt.unGetToken();
                    try {
                        s = ParseStatement.statement(lt);
                        do {
                            s = s.execute(pgm, inStream, outStream);
                        } while (s != null);

                    }
                    catch (BASICSyntaxError e) {
                        outStream.println("Syntax Error : " + e.getMsg());
                        outStream.println(lt.showError());
                        continue;
                    }
                    catch (BASICRuntimeError er) {
                        outStream.println("RUNTIME ERROR.");
                        outStream.println(er.getMsg());
                    }
                    break;

                /*
                 * Blank lines are ignored.
                 */
                case Token.EOL:
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

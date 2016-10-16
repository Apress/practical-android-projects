/*
 * ParseStatment.java - Parse valid BASIC statements.
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
import java.io.InputStream;

class ParseStatement extends Statement {

    private ParseStatement(int x) {
        super(x);
    }

    final static String extraError = "extra input beyond statement end";

    /**
     * Here we implement the abstract methods of Statement, they all generate errors
     * since ParseStatement isn't a "real" statement.
     */
    Statement doit(Program pgm, InputStream in, PrintStream out) throws BASICRuntimeError {
        throw new BASICRuntimeError("Attempt to execute a statement parser object.");
    }

    String unparse() {
        return "THE PARSESTATEMENT OBJECT, NOT A STATEMENT.";
    }

    void trace(Program pgm, PrintStream ps) {
        ps.println("ParseStatement");
    }

    /**
     * Given a 'full' tokenizer buffer, return us a parsed statement.
     */
    static Statement statement(LexicalTokenizer lt) throws BASICSyntaxError {
        Statement s = doParse(lt);
        // System.out.println("UNPARSE = '"+s.unparse()+"'");
        return s;
    }

    /**
     * This method returns a parsed statemnet, it throws an exception if an
     * error occurred.
     */
    static Statement doParse(LexicalTokenizer lt) throws BASICSyntaxError {
        Statement s;
        Token   t;

        t = lt.nextToken();
        if (t.typeNum() == Token.SYMBOL) {
            switch ((int) t.numValue()) {
                case '?':
                    s = new PRINTStatement(lt);
                    t = lt.nextToken();
                    if ((t == null) || (t.typeNum() == Token.EOL))
                        return s;

                    if (t.isSymbol(':')) {
                        s.nxt = statement(lt);
                        return s;
                    }
                    throw new BASICSyntaxError(extraError);
                case '\'':
                    s = new REMStatement(lt);
                    return s;
                default :
                    throw new BASICSyntaxError("Illegal statement symbol start?");
            }
        }

        if (t.typeNum() == Token.KEYWORD) {
            switch ((int) t.numValue()) {
                case TRON:
                    s = new TRONStatement(lt);
                    t = lt.nextToken();
                    if (t.isSymbol(':')) {
                        s.nxt = statement(lt);
                    } else if (t.typeNum() != Token.EOL)
                        throw new BASICSyntaxError(extraError);

                    return s;

                case TROFF:
                    s = new TROFFStatement(lt);
                    t = lt.nextToken();
                    if (t.isSymbol(':')) {
                        s.nxt = statement(lt);
                    } else if (t.typeNum() != Token.EOL)
                        throw new BASICSyntaxError(extraError);

                    return s;

                case END:
                    s = new ENDStatement(lt);
                    t = lt.nextToken();
                    if (t.typeNum() != Token.EOL) {
                        throw new BASICSyntaxError(extraError);
                    }
                    return s;

                case RANDOMIZE:
                    s = new RANDOMIZEStatement(lt);
                    t = lt.nextToken();
                    if (t.isSymbol(':'))
                        s.nxt = statement(lt);
                    else if (t.typeNum() != Token.EOL)
                        throw new BASICSyntaxError(extraError);
                    return s;

                case STOP:
                    s = new STOPStatement(lt);
                    t = lt.nextToken();
                    if (t.isSymbol(':')) {
                        s.nxt = statement(lt);
                    } else if (t.typeNum() != Token.EOL)
                        throw new BASICSyntaxError(extraError);

                    return s;


                case DIM:
                    s = new DIMStatement(lt);
                    t = lt.nextToken();
                    if (t.isSymbol(':'))
                        s.nxt = statement(lt);
                    else if (t.typeNum() != Token.EOL)
                        throw new BASICSyntaxError(extraError);
                    return s;

                case GOTO:
                    s = new GOTOStatement(lt);
                    t = lt.nextToken();
                    if (t.typeNum() != Token.EOL)
                        throw new BASICSyntaxError(extraError);
                    return s;

                case GOSUB:
                    s = new GOSUBStatement(lt);
                    t = lt.nextToken();
                    if (t.isSymbol(':'))
                        s.nxt = statement(lt);
                    else if (t.typeNum() != Token.EOL)
                        throw new BASICSyntaxError(extraError);

                    return s;

                case RETURN:
                    s = new RETURNStatement(lt);
                    t = lt.nextToken();
                    if (t.typeNum() != Token.EOL)
                        throw new BASICSyntaxError(extraError);

                    return s;

                case PRINT:
                    s = new PRINTStatement(lt);
                    t = lt.nextToken();
                    if ((t == null) || (t.typeNum() == Token.EOL))
                        return s;

                    if (t.isSymbol(':')) {
                        s.nxt = statement(lt);
                        return s;
                    }
                    throw new BASICSyntaxError(extraError);

                case IF:
                    s = new IFStatement(lt);
                    t = lt.nextToken();
                    if ((t != null) && (t.typeNum() != Token.EOL))
                        throw new BASICSyntaxError(extraError);
                    return s;

                case DATA:
                    s = new DATAStatement(lt);
                    t = lt.nextToken();
                    if ((t == null) || (t.typeNum() == Token.EOL))
                        return s;

                    if (t.isSymbol(':')) {
                        s.nxt = statement(lt);
                        return s;
                    }
                    throw new BASICSyntaxError(extraError);

                case RESTORE:
                    s = new RESTOREStatement(lt);
                    t = lt.nextToken();
                    if (t.isSymbol(':'))
                        s.nxt = statement(lt);
                    else if (t.typeNum() != Token.EOL)
                        throw new BASICSyntaxError(extraError);

                    return s;

                case READ:
                    s = new READStatement(lt);
                    t = lt.nextToken();
                    if ((t == null) || (t.typeNum() == Token.EOL))
                        return s;
                    else if (t.isSymbol(':')) {
                        s.nxt = statement(lt);
                        return s;
                    }
                    throw new BASICSyntaxError(extraError);

                case ON:
                    s = new ONStatement(lt);
                    t = lt.nextToken();
                    if (t.isSymbol(':'))
                        s.nxt = statement(lt);
                    else if (t.typeNum() != Token.EOL)
                        throw new BASICSyntaxError(extraError);

                    return s;

                case REM:
                    s = new REMStatement(lt);
                    return s;

                case FOR:
                    s = new FORStatement(lt);
                    t = lt.nextToken();
                    if (t.isSymbol(':'))
                        s.nxt = statement(lt);
                    else if (t.typeNum() != Token.EOL)
                        throw new BASICSyntaxError(extraError);

                    return s;

                case NEXT:
                    s = new NEXTStatement(lt);
                    t = lt.nextToken();
                    if (t.isSymbol(':'))
                        s.nxt = statement(lt);
                    else if (t.typeNum() != Token.EOL)
                        throw new BASICSyntaxError(extraError);
                    return s;

                case LET:
                    s = new LETStatement(lt);
                    t = lt.nextToken();
                    if (t.typeNum() == Token.EOL)
                        return s;
                    else if (t.isSymbol(':')) {
                        s.nxt = statement(lt);
                        return s;
                    } else if (t.isSymbol(')')) {
                        throw new BASICSyntaxError("Mismatched parenthesis in LET statement.");
                    }
                    throw new BASICSyntaxError("Unexpected text following LET statement.");

                case INPUT :
                    s = new INPUTStatement(lt);
                    t = lt.nextToken();
                    if ((t == null) || (t.typeNum() == Token.EOL))
                        return s;
                    if (t.isSymbol(':')) {
                        s.nxt = statement(lt);
                        return s;
                    }
                    throw new BASICSyntaxError(extraError);

                default:
                    throw new BASICSyntaxError("Invalid keyword");
            }
        } else if (t.typeNum() == Token.VARIABLE) {
            lt.unGetToken();
            s = new LETStatement(lt);
            t = lt.nextToken();
            if ((t == null) || (t.typeNum() == Token.EOL)) {
                return s;
            } else if (t.isSymbol(':')) {
                s.nxt = statement(lt);
                return s;
            }
            throw new BASICSyntaxError(extraError);

        }
        throw new BASICSyntaxError("Unrecognized statement");
    }
}
/*
 * Program.java - One BASIC program, ready to roll.
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
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.Random;
import cocoa.util.RedBlackTree;
import java.util.Stack;
import java.util.Vector;

/**
 * This class instantiates a BASIC program. A valid program is one that is
 * parsed and ready to run. You can run it by invoking the run() method.
 * The standard input and output of the running cocoa.basic program can either
 * be passed into the <b>run</b> method, or they can be presumed to be the
 * in and out streams referenced by the <b>System</b> class.
 *
 * This class uses Red-Black trees to hold the parsed statements and the
 * symbol table.
 *
 * @author      Chuck McManis
 * @version     1.1
 * @see         CommandInterpreter
 *
 */
public class Program implements Runnable
{
    // this tree holds all of the statements.
    private RedBlackTree stmts = new RedBlackTree(new NumberCompare());

    // this tree holds all of the variables.
    private RedBlackTree vars = new RedBlackTree();

    private Stack stmtStack = new Stack();
    Vector dataStore = new Vector();
    int dataPtr = 0;
    Random r = new Random(0);

    String myName;

    boolean traceState = false;
    PrintStream traceFile = null;

    void trace(boolean a)
    {
        traceState = a;
    }

    void trace(boolean a, String f) {
        if (traceFile == null) {
            try {
                traceFile = new PrintStream(new FileOutputStream(f));
            } catch (IOException e) {
                System.out.println("Couldn't open trace file.");
                traceFile = null;
            }
        }
        trace(a);
    }

    Random getRandom() {
        return r;
    }

    void randomize(double seed) {
        r = new Random((long) seed);
    }

    void randomize() {
        r = new Random(); // uses the clock
    }

    /**
     * There are two ways to create a new program object, you can load one from
     * an already open stream or you can pass in a file name and load one from
     * the file system.
     */
    public static Program load(InputStream source, PrintStream out) throws IOException, BASICSyntaxError {
        DataInputStream dis = null;
        dis = new DataInputStream(new BufferedInputStream(source));
        char data[] = new char[256];
        LexicalTokenizer lt = new LexicalTokenizer(data);
        String lineData;
        Statement s;
        Token t;
        Program result = new Program();

        while (true) {
            // read a line of our BASIC program.
            lineData = dis.readLine();

            // if EOF simply return.
            if (lineData == null)
                return result;

            // if the line was blank, ignore it.
            if (lineData.length() == 0)
                continue;

            lt.reset(lineData);
            t = lt.nextToken();
            if (t.typeNum() != Token.CONSTANT) {
                throw new BASICSyntaxError("Line failed to start with a line number.");
            }

            try {
                s = ParseStatement.statement(lt);
            } catch (BASICSyntaxError bse) {
                out.println("Syntax error: "+bse.getMsg());
                out.println(lt.showError());
                throw bse;
            }
            s.addText(lineData);
            s.addLine((int) t.numValue());
            result.add((int) t.numValue(), s);
        }
    }

    /**
     * Load the specified file and parse the cocoa.basic statements it contains.
     * @throws IOException when the filename cannot be located or opened.
     * @throws BASICSyntaxError when the file does not contain a properly formed
     *          BASIC program.
     */
    public static Program load(String source, PrintStream out) throws IOException, BASICSyntaxError {
        // XXX this needs to use the SourceManager class //
        FileInputStream fis = new FileInputStream(source);
        Program r = null;
        try {
            r = load(fis, out);
        } catch (BASICSyntaxError e) {
            fis.close();
            throw e;
        }
        fis.close();
        return r;
    }

    /**
     * Write the cocoa.basic program out to the passed output stream. Conceptually
     * this is identical to doing a list operation.
     */
    public void save(OutputStream out) throws IOException {
        PrintStream p = new PrintStream(out);
        list(p);
    }

    /**
     * Write the program out to the file named in <i>output</i>.
     */
    public void save(String output) throws IOException {
        FileOutputStream fos = new FileOutputStream(output);
        save(fos);
        fos.flush();
        fos.close();
    }

    /**
     * This method starts this program running in its own thread.
     */
    void start() {
        Thread t = new Thread(this);
        if (myName != null)
            t.setName(myName + " execution.");
        t.start();
    }

    /**
     * Add a statement to the current program. Statements are indexed
     * by line number. If the add fails for some reason this method
     * returns false.
     */
    public boolean add(int line, Statement s) {
        Integer ln = new Integer(line);
        Object z = stmts.put(ln, s);
        return true;
    }

    /**
     * Delete a statement from the current program. Statements are
     * indexed by line numbers. If the statement specified didn't
     * exist, then this method returns false.
     */
    public boolean del(int line) {
        if (stmts.remove(new Integer(line)) == null)
            return false;
        return true;
    }

    /**
     * Compute the indices based on the expressions in the variable
     * object.
     */
    private int[] getIndices(Variable v) throws BASICRuntimeError {
        int result[] = new int[v.numExpn()];

        for (int i=0; i < result.length; i++) {
            result[i] = (int) v.expn(i).value(this);
        }
        return result;
    }

    /**
     * Return the numeric value of a variable in the symbol table.
     * @throws BASICRuntimeError if the variable isn't defined.
     *
     */
    double getVariable(Variable v) throws BASICRuntimeError {
        Variable vi = (Variable) vars.get(v.name);
        if (vi == null) {
            throw new BASICRuntimeError("Undefined variable '"+v.name+"'");
        }
        if (! vi.isArray())
            return vi.numValue();
        int ii[] = getIndices(v);
        return vi.numValue(ii);
    }

    /**
     * Return the contents of the string variable named <i>name</i>. If
     * the variable has not yet been declared (ie used) this method throws
     * a BASICRuntime error.
     */
    String getString(Variable v) throws BASICRuntimeError {
        Variable vi = (Variable) vars.get(v.name);
        if (vi == null)
            throw new BASICRuntimeError("Variable "+v.name+" has not been initialized.");
        if (! v.isArray())
            return vi.stringValue();
        int ii[] = getIndices(v);
        return vi.stringValue(ii);
    }

    /**
     * Set the numeric variable <i>name</i> to have value <i>value</i>. If
     * this is the first time we have seen the variable, create a place for
     * it in the symbol table.
     */
    void setVariable(Variable v, double value) throws BASICRuntimeError {
        Variable vi = (Variable) vars.get(v.name);
        if (vi == null) {
            if (v.isArray())
                throw new BASICRuntimeError("Array must be declared in a DIM statement");
            vi = new Variable(v.name);
            vars.put(v.name, vi);
        }
        if (! vi.isArray()) {
            vi.setValue(value);
            return;
        }
        int ii[] = getIndices(v);
        vi.setValue(value, ii);
    }

    void setRandom(long seed) {
    }

    /**
     * Set the string variable named <i>name</i> to have the value <i>value</i>.
     * If this is the first use of the variable it is created.
     */
    void setVariable(Variable v, String value) throws BASICRuntimeError {
        Variable vi = (Variable) vars.get(v.name);
        if (vi == null) {
            if (v.isArray())
                throw new BASICRuntimeError("Array must be declared in a DIM statement");
            vi = new Variable(v.name);
            vars.put(v.name, vi);
        }
        if (! vi.isArray()) {
            vi.setValue(value);
            return;
        }
        int ii[] = getIndices(v);
        vi.setValue(value, ii);
    }

    /**
     * This method is used by the DIM statement to DECLARE arrays. Given
     * the nature of arrays we force them to be declared before they can
     * be used. This is common to most BASIC implementations.
     */
    void declareArray(Variable v) throws BASICRuntimeError {
        Variable vi;
        int ii[] = getIndices(v);
        vi = new Variable(v.name, ii);
        Variable xx = (Variable) vars.put(v.name, vi);
    }

    /**
     * Compute and return the next program statement to be executed.
     * The policy is, if the current statement has another statement hanging
     * off its <i>nxt</i> pointer use that one, otherwise use the next one
     * in the program numerically.
     */
    Statement nextStatement(Statement s) {
        if (s == null) {
            return null;
        } else if (s.nxt != null) {
            return s.nxt;
        }
        return ((Statement) stmts.next(new Integer(s.line)));
    }


    /**
     * Return the statment whose line number is <i>line</i>
     */
    Statement getStatement(int line) {
        Statement s = (Statement) stmts.get(new Integer(line));
        return s;
    }

    /**
     * List program lines from <i>start</i> to <i>end</i> out to the
     * PrintStream <i>p</i>. Note that due to a bug in the Windows
     * implementation of PrintStream this method is forced to append
     * a <CR> to the file.
     */
    void list(int start, int end, PrintStream p) {
        for (Enumeration e = stmts.elements(); e.hasMoreElements(); ) {
            Statement s = (Statement) e.nextElement();
            if ((s.lineNo() >= start) && (s.lineNo() <= end)) {
                p.print(s.asString());
                p.print("\r");
                p.println(); // for Windows clients
            }
        }
    }

    /**
     * Dump the symbol table
     */
    void dump(PrintStream p) {
        for (Enumeration e = vars.elements(); e.hasMoreElements(); ) {
            Variable v = (Variable) e.nextElement();
            p.println(v.unparse()+" = "+(v.isString() ? "\""+v.stringValue()+"\"" : ""+v.numValue()));
        }
    }

    /**
     * This is the first variation on list, it simply list from the starting
     * line to the the end of the program.
     */
    void list(int start, PrintStream p) {
        list(start, Integer.MAX_VALUE, p);
    }

    /**
     * This second variation on list will list the entire program to the passed
     * PrintStream object.
     */
    void list(PrintStream p) {
        list(0, p);
    }

    /**
     * This final variant of the list method will list the program on System.out.
     */
    void list() {
        list(System.out);
    }

    /**
     * Run the program and use the passed in streams as its input and output streams.
     *
     * Prior to running the program the statement stack is cleared, and the data fifo
     * is also cleared. Thus re-running a stopped program will always work correctly.
     *
     * @throws BASICRuntimeError if an error occurs while running.
     */
    public void run(InputStream in, OutputStream out) throws BASICRuntimeError {
        PrintStream pout;
        Enumeration e = stmts.elements();
        stmtStack = new Stack();    // assume no stacked statements ...
        dataStore = new Vector();   // ...  and no data to be read.
        dataPtr = 0;
        Statement s;

        vars = new RedBlackTree();

        // if the program isn't yet valid.
        if (! e.hasMoreElements())
            return;

        if (out instanceof PrintStream) {
            pout = (PrintStream) out;
        } else {
            pout = new PrintStream(out);
        }

        /* First we load all of the data statements */
        while (e.hasMoreElements()) {
            s = (Statement) e.nextElement();
            if (s.keyword == Statement.DATA) {
                s.execute(this, in, pout);
            }
        }

        e = stmts.elements();
        s = (Statement) e.nextElement();
        do {
            int yyy;

            /* While running we skip Data statements. */
            try {
                yyy = in.available();
            } catch (IOException ez) {
                yyy = 0;
            }
            if (yyy != 0) {
                pout.println("Stopped at :"+s);
                push(s);
                break;
            }
            if (s.keyword != Statement.DATA) {
                if (traceState) {
                    s.trace(this, (traceFile != null) ? traceFile : pout);
                }

                s = s.execute(this, in, pout);
            } else
                s = nextStatement(s);
        } while (s != null);
    }

    /**
     * This package private version of run() is used by the command interpreter
     * to run a "single" statement in the context of this program. Single is
     * in quotes because if the statement has additional statements chained off
     * its next pointer, these will be run as well. Further if one of them is
     * a GOTO or GOSUB or IF and they cause a tranfer to a numbered statement then
     * exectution will start at that statement. This can be useful for debugging
     * but unpredictable since not all variables will be declared if their assignment
     * statements have not yet been executed.
     *
     * Unlike its sibling method above, it does NOT clear the statement stack or
     * data FIFO. This is so the command interpreter can debug stopped programs
     * using the immediate execution feature.
     */
    void run(Statement s, InputStream in, OutputStream out) throws BASICRuntimeError {
        // if the program isn't yet valid.
        PrintStream pout;
        Enumeration e = stmts.elements();
        if (! e.hasMoreElements())
            return;

        if (out instanceof PrintStream) {
            pout = (PrintStream) out;
        } else {
            pout = new PrintStream(out);
        }

        do {
            s = s.execute(this, in, pout);
        } while (s != null);
    }

    /**
     * This final version of run is used to implement the <b>Runnable</b> interface.
     * It will run the program using System.in and System.out as the standard I/O
     * streams for the program and it does *NOT* throw BASICRuntimeError. Instead
     * it catches it and prints an error message to standard out.
     */
    public void run() {
        try {
            run(System.in, System.out);
        } catch (BASICRuntimeError e) {
            System.out.println("Error Running program: "+e.getMsg());
        }
    }

    /**
     * This method resumes a program that has been stopped. If the program
     * wasn't really stopped it throws a BASICRuntimeError.
     *
     * @throws BASICRuntimeError - Program wasn't in a stopped state.
     */
    void resume(InputStream in, PrintStream pout) throws BASICRuntimeError {
        Statement s;

        s = pop();
        if ((s == null) || (s.keyword != Statement.STOP)) {
            throw new BASICRuntimeError("This program was not previously stopped.");
        }
        s = nextStatement(s);
        do {
            s = s.execute(this, in, pout);
        } while (s != null);
    }

    void cont(InputStream in, PrintStream pout) throws BASICRuntimeError {
        Statement s;
        int yyy;

        s = pop();
        do {
            /* While running we skip Data statements. */
            try {
                yyy = in.available();
            } catch (IOException e) {
                yyy = 0;
            }
            if (yyy != 0) {
                pout.println("Stopped at :"+s);
                push(s);
                break;
            }
            if (s.keyword != Statement.DATA) {
                if (traceState) {
                    s.trace(this, (traceFile != null) ? traceFile : pout);
                }

                s = s.execute(this, in, pout);
            } else
                s = nextStatement(s);
        } while (s != null);
    }


    /*
     * These methods deal with pushing and popping statements from the statement
     * stack, and data items from the data stack.
     */

    /**
     * Push this statement on the stack (one of FOR, GOSUB, or STOP)
     */
    void push(Statement s) {
        stmtStack.push(s);
    }

    /**
     * Pop the next statement off the stack, return NULL if the stack is
     * empty.
     */
    Statement pop() {
        if (stmtStack.isEmpty())
            return null;
        return (Statement) stmtStack.pop();
    }

    /**
     * Add a token to the data FIFO.
     */
    void pushData(Token t) {
        dataStore.addElement(t);
    }

    /**
     * Get the next token in the FIFO, return null if the
     * FIFO is empty.
     */
    Token popData() {
        if (dataPtr > (dataStore.size() - 1))
            return null;
        return (Token) dataStore.elementAt(dataPtr++);
    }

    /**
     * Reset the data FIFO back to the beginning.
     */
     void resetData() {
        dataPtr = 0;
     }
}
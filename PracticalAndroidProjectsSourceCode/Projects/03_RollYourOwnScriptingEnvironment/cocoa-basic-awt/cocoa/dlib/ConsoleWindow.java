package cocoa.dlib;
/*
$Id: ConsoleWindow.java 1.2 1996/09/10 02:03:30 ddyer Exp $
$Log: ConsoleWindow.java $
Revision 1.2  1996/09/10 02:03:30  ddyer
added list

Revision 1.1  1996/09/07 13:06:00  ddyer
Initial revision

*/

import java.io.*;
import java.awt.*;

/**
This class implements a simple console window, conceptually a replacement for
reading/writing to System.out and System.in;  The advantage of this over System.xx
is that it works with JDB, the contents of the window can be saved, and *major feature*
the typein and typeout can be edited.

The intended use is as a programmer's shell for messages and simple command
processing while developing programs.  See the "main" method in this file
for an example how to use it.
 
@author Dave Dyer <ddyer@netcom.com>
@version 1.01, August 1996

*/

/* version 1.01 adds an explicit "descroy" at the exits */
public class ConsoleWindow extends Frame
{
    private KeyboardBuffer area;
    private DataInputStream input;
    private PrintStream output;
    String fonts[];
    String styles[] = {"Plain", "Bold", "Italic"};
    String sizes[] = {"8", "9", "10", "12", "14", "16", "18", "24"};
    String clipboard;
    String saveFile = null;
    String fname;
    int fstyle;
    int fsize;

    MenuItem saveItem = new MenuItem("Save");
    CheckboxMenuItem fontItems[];
    CheckboxMenuItem styleItems[];
    CheckboxMenuItem sizeItems[];

    public ConsoleWindow(String title) {

 // Initialize
	  area = new KeyboardBuffer(30,80);
    fonts = getToolkit().getFontList();
    MenuBar menuBar = new MenuBar();

    fname = "Courier";
    fstyle = Font.PLAIN;
    fsize = 12;

  // Create file menu

    Menu fileMenu = new Menu("File");
    fileMenu.add(new MenuItem("New"));
    fileMenu.add(new MenuItem("Open..."));
    fileMenu.add(saveItem);
    fileMenu.add(new MenuItem("Save as..."));
    fileMenu.add(new MenuItem("-"));
    fileMenu.add(new MenuItem("Exit"));
    menuBar.add(fileMenu);

  // Create edit menu

    Menu editMenu = new Menu("Edit");
    editMenu.add(new MenuItem("Cut"));
    editMenu.add(new MenuItem("Copy"));
    editMenu.add(new MenuItem("Paste"));
    editMenu.add(new MenuItem("Select all"));
    editMenu.add(new MenuItem("-"));
//    editMenu.add(new MenuItem("Find..."));
    menuBar.add(editMenu);

  // Create font menu

    Menu fontMenu = new Menu("Font");
    fontItems = new CheckboxMenuItem[fonts.length];

    for (int i = 0; i < fonts.length; i++) {
	fontItems[i] = new CheckboxMenuItem(fonts[i]);
	fontMenu.add(fontItems[i]);
	
	if (fonts[i].equals(fname))
	  fontItems[i].setState(true);
    }

    menuBar.add(fontMenu);

  // Create style menu

    Menu styleMenu = new Menu("Style");
    styleItems = new CheckboxMenuItem[styles.length];

    for (int i = 0; i < styles.length; i++) {
	styleItems[i] = new CheckboxMenuItem(styles[i]);
	styleMenu.add(styleItems[i]);
	
	if (i == fstyle) 
	  styleItems[i].setState(true);
    }

    menuBar.add(styleMenu);
    
 // Create size menu

    Menu sizeMenu = new Menu("Size");
    sizeItems = new CheckboxMenuItem[sizes.length];

    for (int i = 0; i < sizes.length; i++) {
      sizeItems[i] = new CheckboxMenuItem(sizes[i]);	
      sizeMenu.add(sizeItems[i]);

	if (sizes[i].equals(new Integer(fsize).toString()))
	  sizeItems[i].setState(true);
    }
 
    menuBar.add(sizeMenu);

    setMenuBar(menuBar);
    saveItem.disable();

   // Show window

    area.setFont(new Font(fname, fstyle, fsize));
    setTitle(title);
    add("Center", area);

    pack();
    show();
   }

// Read file

  public String readFile(String fl) {
    String text = new String();

    try {
      FileInputStream fs = new FileInputStream(fl);
      DataInputStream ds = new DataInputStream(fs);
      String str = new String();

      while (str != null) {
  	  str = ds.readLine();

	  if (str != null)
	    text = text + str + "\n";
      }
    } catch (Exception err) {
      System.out.println("Cannot open file.");
    }

    return text;
  }

// Write file

  public void writeFile(String fl, String txt) {
    try {
      FileOutputStream fs = new FileOutputStream(fl);
      DataOutputStream ds = new DataOutputStream(fs);
	String ls = System.getProperty("line.separator");

	for (int i = 0; i < txt.length(); i++) {
  	  char ch = txt.charAt(i);

	  switch (ch) {
	    case '\n':
	      ds.writeBytes(ls);
		break;
		
	    default:
		ds.write(ch);
 	  }
      }
    } catch (Exception err) {
      System.out.println("Cannot save file.");
    }
  }

// Handle system event

  public boolean handleEvent(Event evt) {
    if (evt.id == Event.WINDOW_DESTROY && evt.target == this) 
      {this.dispose();
       System.exit(0);
       }

    return super.handleEvent(evt);
  }

// Handle component events

  public boolean action(Event evt, Object obj) {
    String label = (String) obj;
    String file = null;
  
  // Handle file menu

    if (label.equals("New")) {
      area.setText("");
	setTitle("Text Edit");
      saveItem.disable();

	return true;
    }

    if (label.equals("Open...")) {
      FileDialog dialog = new FileDialog(this, "Open...", 
	  FileDialog.LOAD);

      dialog.show();
      file = dialog.getFile();

      if (file != null) {
	  setTitle(file + " - Text Edit");
	
	  saveFile = file;
	  area.setText(readFile(file));
	  saveItem.enable();
      }

	return true;
    }

    if (label.equals("Save")) {
      writeFile(saveFile, area.getText());
    }

    if (label.equals("Save as...")) {
      FileDialog dialog = new FileDialog(this, "Save as...", 
	  FileDialog.SAVE);
      
	dialog.show();
      file = dialog.getFile();

      if (file != null) {
	  setTitle(file + " - Text Edit");
	  saveFile = file;
	  writeFile(file, area.getText());
      }

	return true;
    }

    if (label.equals("Exit")) {
      {this.dispose();
       System.exit(0);
       }
     	return true;
    }

  // Handle edit menu

    if (label.equals("Cut")) {
      clipboard = area.getSelectedText();
      
	area.replaceText("", area.getSelectionStart(), 
        area.getSelectionEnd());

	return true;
    }

    if (label.equals("Copy")) {
      clipboard = area.getSelectedText();
	return true;
    }

    if (label.equals("Paste")) {
      int start = area.getSelectionStart();
      int end = area.getSelectionEnd();

      if (start == end) 
        area.insertText(clipboard, start);
      else
        area.replaceText(clipboard, start, end);

	return true;
    }

    if (label.equals("Select all"))
      area.selectAll();

//    if (label.equals("Find...")) {
//	FindDialog dialog = new FindDialog(this, label, true);
//	dialog.resize(200, 70);
//	dialog.show();
//	
//	return true;
//    }

  // Handle font menu

    for (int i = 0; i < fonts.length; i++) {
      if (label.equals(fonts[i])) {
	  for (int j = 0; j < fonts.length; j++) 
	    if (i != j)
	      fontItems[j].setState(false);

	  fname = label;
	  area.setFont(new Font(fname, fstyle, fsize));
		
	  return true;
	}
    }

  // Handle style menu

    for (int i = 0; i < styles.length; i++) {
      if (label.equals(styles[i])) {
 	  for (int j = 0; j < styles.length; j++) 
	    if (i != j)
	      styleItems[j].setState(false);
		
 	  fstyle = i;
	  area.setFont(new Font(fname, fstyle, fsize));
	  
	  return true;
      }
    }

  // Handle size menu

    for (int i = 0; i < sizes.length; i++) {
      if (label.equals(sizes[i])) {
	  for (int j = 0; j < sizes.length; j++) 
	    if (i != j)
	      sizeItems[j].setState(false);
		
 	  fsize = new Integer(label).intValue();
	  area.setFont(new Font(fname, fstyle, fsize));

	  return true;
      }
    }
    
    return false; 
  }

 public KeyboardBuffer getTextArea() {
    return area;
  }
 /* get a PrintSteam for this window.  Printed output will appear *above*
 the line that is currently being typed. 
 */
 public PrintStream PrintStream() 
  { if(output==null) { output = area.PrintStream(); }
    return(output); 
  }
 /** get a DataInputStream corresponding to the typein area if this window.
  Note that the input from the DataInputStream will be line buffered, so no
  input is available from incomplete lines.
  */
 public DataInputStream DataInputStream()
  { if(input==null) { input = area.DataInputStream();}
   return(input); 
  }
/**
<pre>
public static void main (String args[]) throws IOException
{ String name = args.length >= 1 ? args[0] : "Console";
  ConsoleWindow console = new ConsoleWindow(name);
  PrintStream out = console.PrintStream();
  DataInputStream in = console.DataInputStream();
  out.println("ready");
 while (true)
  {
   out.print("> ");
   {String str = in.readLine();
    if(str.compareTo("die")==0) 
     {out.println("Dying");
      System.out.println("Dying (default output)");
      throw new Error("this is a fatal error");
      }
    out.println("Typed Line: " + str);
  }};
 }
</pre>
*/
/* [pieter][20101125] */ /***
public static void main (String args[]) throws IOException
{ String name = args.length >= 1 ? args[0] : "Console";
  ConsoleWindow console = new ConsoleWindow(name);
  PrintStream out = console.PrintStream();
  DataInputStream in = console.DataInputStream();
  out.println("ready");
 while (true)
  {
   out.print("> ");
   {String str = in.readLine();
    if(str.compareTo("die")==0) 
     {out.println("Dying");
      System.out.println("Dying (default output)");
      throw new Error("this is a fatal error");
      }
    out.println("Typed Line: " + str);
  }};
 }
***/ /* [pieter][20101125] */
}
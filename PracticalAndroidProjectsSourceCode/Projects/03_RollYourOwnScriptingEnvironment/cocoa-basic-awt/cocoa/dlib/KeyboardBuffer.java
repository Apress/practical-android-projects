package cocoa.dlib;

/*
$Id: KeyboardBuffer.java 1.2 1996/09/10 02:03:30 ddyer Exp $
$Log: KeyboardBuffer.java $
Revision 1.2  1996/09/10 02:03:30  ddyer
added list

Revision 1.1  1996/09/07 13:06:00  ddyer
Initial revision

*/

import java.awt.*;
import java.io.*;

/**

This is a subclass of TextArea which keeps track of the
location of the cursor, and distinguishes text typed
below the cursor from text printed or edited above the
cursor.  It is intended as a helper class for ConsoleWindow

@see ConsoleWindow
@author Dave Dyer <ddyer@netcom.com>
@version 1.0, August 1996
*/
class windowOutputStream extends OutputStream
{	KeyboardBuffer window;
  byte buffer[] = new byte[128];
  int idx = 0;
  public void flush() 
  { if(idx>0) 
   { int n = idx;
     idx=0;
     write(buffer,0,n); 
   }}
  void safeflush()
  { flush();
  }
	windowOutputStream (KeyboardBuffer window) {this.window = window;}
	public void write(int b)
	 {buffer[idx++]=(byte)b; 
	  if(idx>=buffer.length) {flush();}
	  }
	public void write(byte b[]) {flush(); window.write(b,0,b.length);}
	public void write(byte b[],int from,int len) 
	 {flush();
	  window.write(b,from,len);
	  }
}

class windowInputStream extends InputStream
{
    KeyboardBuffer window;

    windowInputStream(KeyboardBuffer window)
    {
        this.window = window;
    }

    public int available()
    {
        window.outputstream.safeflush();
        return (window.available());
    }

    public int read()
    {
        window.outputstream.safeflush();
        return (window.read());
    }
    //public int read(byte ar[]) {return(window.read(ar));}
    //public int read(byte ar[],int from,int to) { return(window.read(ar,from,to));}

}

public class KeyboardBuffer extends TextArea
{
    private Thread suspendedthread;        // the guy we zapped
    private windowInputStream inputstream = new windowInputStream(this);
    windowOutputStream outputstream = new windowOutputStream(this);
    private int marked_position = 0;
    private char temp[] = new char[1];
    private boolean charisready = false;
    private boolean keyisdown = false;    // a key is down and has not come up yet
    private int keyselend;                        // end of the selection when the key wend down
    private int keyisdownat;
    private boolean linemode = true;    // true if we don't admit input is there

    // until there is an eol character
    /* constructors */
    public KeyboardBuffer(String name)
    {
        super(name);
    }

    public KeyboardBuffer(int rows, int columns)
    {
        super(rows, columns);
    }

    public KeyboardBuffer(String name, int rows, int cols)
    {
        super(name, rows, cols);
    }

    /* get the raw input and output streams */
    OutputStream OutputStream()
    {
        return (outputstream);
    }

    InputStream InputStream()
    {
        return (inputstream);
    }

    /* get the formatted input and output streams */
    PrintStream PrintStream()
    {
        return (new PrintStream(outputstream, true));
    }

    DataInputStream DataInputStream()
    {
        return (new DataInputStream(inputstream));
    }

public void set_linemode(boolean t_f)
{ linemode = t_f;
  set_charisready();
}
public boolean linemode_p() { return(linemode); }

/* after any change in state, decide if we think a character is ready 
   for the consumer */
private synchronized void set_charisready()
  { String text = getText();
    int len = text.length();
    charisready = false;
    if (!keyisdown && (len>marked_position))
     { if(!linemode) {charisready = true; }
       else
       {for(int i=marked_position;
     			 i<len;
     			 i++) 
     			 {char ch = text.charAt(i);
            if((ch == '\r') || (ch =='\n')) 
             { charisready = true; break; 
             }
           }
     }}
//	System.out.println("Mark " + marked_position + " last " + (int)text.charAt(len-1));
    if(charisready) 
    {Thread s=suspendedthread;
     suspendedthread=null;
     if(s!=null) 
     {s.resume();
      /* System.out.println("Thread " + s + " resumed"); */ 
     }
    }
  }

private void AddToMarkedPosition(int dif)
	{
		marked_position += dif;
		set_charisready(); 
	}
	
private synchronized void NoticeNewText(int oldlen,int selend)
  {int newlen = getText().length();
	 if((newlen!=oldlen)  && (selend<marked_position))
		 {AddToMarkedPosition(newlen-oldlen);
		 }
  }

/* keyup and keydown ought to come in pairs, but we try not to depend on it. */
public boolean keyDown(Event e,int keynum)
	{
	  keyisdown=true; charisready=false;
	  keyisdownat = getText().length();
		keyselend = getSelectionEnd();		//remember these until the key comes back up
		return(super.keyDown(e,keynum));
	}
	
public boolean keyUp(Event e,int keynum)
	{ int len = keyisdown ? keyisdownat : getText().length();
	  int sel = keyisdown ? keyselend : getSelectionEnd();
		NoticeNewText(len,sel);
		keyisdown=false;
		set_charisready(); 
	return(super.keyUp(e,keynum));
}
	
public void insertText(String str,int pos)
	{
		if((str!=null) && (str.length()>0))
		{charisready=false;
		super.insertText(str,pos);
		if(pos<marked_position) 
		 {
		   AddToMarkedPosition(str.length());
		 }
			else {set_charisready();}; 
    }
	}

public void insertTextBeforeMark(String str)
	{
		if((str!=null) && (str.length()>0))
		{charisready=false;
		 boolean oldkd = keyisdown;
		 keyisdown=true; charisready=false;
		 super.insertText(str,marked_position);
		 marked_position+=str.length();
		 keyisdown=oldkd;
		 set_charisready();
    }
	}
	
public void appendText(String str)
{
		charisready=false;
		super.appendText(str);
		set_charisready(); 
}

public void replaceText(String str,int start,int end)
	{
		charisready=false;
		super.replaceText(str,start,end);
		if(marked_position>=end)
		 {
		 AddToMarkedPosition(str.length()-(end-start));
		 }
		else {set_charisready();}; 

	}

public void setText(String str)
	{
		charisready=false;
		super.setText(str);
		marked_position = getText().length();
		set_charisready(); 

	}

    
	private char readChar() 
	{ char value = (char)0;
	 if(charisready)
	 { String str = getText();
    int newlen = str.length();
    if((newlen>marked_position)) 
     {value = str.charAt(marked_position++);
     } 
    }
  return(value);
  }
  private char waitChar()
  { char ch;
  	while((ch=readChar())==(char)0) 
  	{ suspendedthread=Thread.currentThread();
  	  //System.out.println("Going to suspend thread" + suspendedthread);
  	  suspendedthread.suspend(); 
  	  //System.out.println("thread" + suspendedthread + " running again");
  	  }
  	//System.out.println("WaitChar returns " + (int)ch );
  	return(ch);
  }
  
  
  /* stream methods */
  public int available() 
  { /* here's a tricky bit: there are at least three threads involved here,
    the system thread actually maintaining the buffer, the console thread 
    running the window, and the client thread reading/writing the streams.
    Rather than let the client know what's really going on, we only tell him
    what we want him to know, so he doesn't accidentally notice the buffer
    in some transient state */
    return(charisready ? getText().length()-marked_position : 0);
  }
  public int read() { char ch=waitChar(); return((int)ch);}
  public void write(int v) 
   { char ch =(char)v;
     /* although this looks circuitous, this should actually be cons free */
     Character chr = new Character(ch);
     insertTextBeforeMark(chr.toString());
   }
  public void write(byte data[],int from,int len)
  {
    insertTextBeforeMark(new String(data,0,from,len));
  }   

  }
package com.example.myandroid;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MyAndroidSdkAppActivity2 extends Activity
{
    /** TAG for debug logging purposes - used as a filter in DDMS */
    private static final String TAG = "MyAndroidSdkAppActivity2";
    
    /** our message text file - used to store arbitrary bits of text */
    private static final String MESSAGEFILE = "messagefile.txt";
    
    /** handles to our static controls in the XML layout */
    private Button       cmdSilly      = null;
    private Button       cmdMaker      = null;
    private Button       cmdSave       = null;
    private EditText     txtMessage    = null;
    private CharSequence message_def   = null;
    private CharSequence message       = null;
    private String       label_toasts  = null;
    private String       label_alerts  = null;
    private String       label_notify  = null;
    
    /** handles to our dynamic controls created programmatically */
    private ViewGroup    vwgMainLayout = null;
    private static final int MAKE_MAX  = 3;
    private int          countMade     = 0;
    private Button       cmdMade       = null;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "in [onCreate()]...");
        
        super.onCreate(savedInstanceState);
        // set the main layout
        setContentView(R.layout.main);       
        // also get a programmable handle to the main layout
        vwgMainLayout  = (ViewGroup)findViewById(R.id.layout_main);
        initialize(); // our init method
    }

    public void initialize()
    {
        Log.d(TAG, "in [initialize()]...");
        
        /** get string values from the default string table */
        message_def  = getString(R.string.default_message);
        label_toasts = getString(R.string.label_toasts);
        label_alerts = getString(R.string.label_alerts);
        label_notify = getString(R.string.label_notify);
        
        cmdSilly = (Button)findViewById(R.id.cmd_silly_exit_button);
        cmdSilly.setOnClickListener(
            new Button.OnClickListener() {
                public void onClick (View v){
                    Log.d(TAG, v.toString() + ": Leaving activity...");
                    Runtime.getRuntime().exit(0);
                }
            }
        );       
        
        cmdSave = (Button)findViewById(R.id.cmd_save_message);
        cmdSave.setOnClickListener(
            new Button.OnClickListener() {
                public void onClick (View v) {
                    Log.d(TAG, v.toString() + ": Saving message...");
                    message = txtMessage.getText();
                    Log.d(TAG, "message: [" + message + "]");
                    Log.d(TAG, "file: [" + MESSAGEFILE + "]");
                    writeMessageFile(message);
                    makeToast("[" + message + "] is now saved");
                }
            }
        );
        
        txtMessage = (EditText)findViewById(R.id.txt_message);
        txtMessage.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                boolean ret = false; // we are not consuming the event by default
                if (keyCode == KeyEvent.KEYCODE_MENU) { // ignore menu key
                    Log.d(TAG, v.toString() + ": User pressed the MENU key");
                }
                else {
                    Log.d(TAG, v.toString() + ": User worked in the message");
                    message = txtMessage.getText();               
                    setButtonsEnabled();
                }
                return ret;
            }
        });        
        
        cmdMaker = (Button)findViewById(R.id.cmd_make_button);
        cmdMaker.setOnClickListener(
            new Button.OnClickListener() {
                public void onClick (View v) {
                    Log.d(TAG, v.toString() + ": Making a button...");
                    makeWideButton( "I was made at runtime");
                }
            }
        ); 
        
        Log.d(TAG, "reading file: [" + MESSAGEFILE + "]");
        message = readMessageFile();
        txtMessage.setText(message);
        Log.d(TAG, "retrieved: [" + message + "]");
        setButtonsEnabled();
        if (0 == message.length()) { // only show default message if empty
            txtMessage.setText(message_def);
        }
    }
    
    public void setButtonsEnabled()
    {
        if (0 == message.length()) {
            Log.d(TAG, "message is EMPTY");
            cmdMaker.setEnabled(false);
            cmdSave.setEnabled(false);            
        }
        else {
            Log.d(TAG, "message is: [" + message + "]");
            cmdMaker.setEnabled(true);
            cmdSave.setEnabled(true);
        }
    }
    
    /** 
     * class to demonstrate tagging an Android View instance with user data
     * */
    public class MyButtonTagData {
        public Integer myUserId = 0;
        public CharSequence myUserData = "--empty--";
        public MyButtonTagData(Integer id, CharSequence data) {
            this.myUserId   = id;
            this.myUserData = data;
        }
    }
    
    public void makeWideButton(CharSequence label) 
    {
        countMade++;
        if (MAKE_MAX >= countMade) {
            cmdMade = new Button(this);
            cmdMade.setText("[" + countMade + "] " + label);
            cmdMade.setTag( /** attach our structure instance to the control */
                new MyButtonTagData(new Integer(countMade), label_notify)
            );
            cmdMade.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick (View v) {
                        MyButtonTagData tagdata = (MyButtonTagData)v.getTag();
                        Integer tag = tagdata.myUserId;
                        switch (tag.intValue()) {
                            case 1:
                                ((Button)v).setText(label_alerts);
                                Log.d(TAG, v.toString() + ": button ONE...");
                                showOkAlertDialog(tag + " - " + message); 
                                break;
                            case 2:  
                                ((Button)v).setText(label_toasts);
                                Log.d(TAG, v.toString() + ": button TWO...");
                                makeToast(tag + " - " + message); 
                                break;
                            default: 
                                ((Button)v).setText(tagdata.myUserData);
                                Log.d(TAG, v.toString() + ": button DEFAULT...");
                                showNotification(tag + " - " + message); 
                                break;
                        }                        
                    }
                }
            );       
            LayoutParams parms = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            vwgMainLayout.addView(cmdMade, parms);       
        }
    }

    public void makeToast(CharSequence message) 
    {
        Toast.makeText( 
            this,
            message, 
            Toast.LENGTH_SHORT).show();
    }

    public void showOkAlertDialog(CharSequence message) 
    {
        new AlertDialog.Builder(this)
          .setMessage(message)
          .setPositiveButton("OK", null)
          .show();
    }

    public void showNotification(CharSequence message) 
    {
        final int notifyRef = 1;
        final int notifyIcon = R.drawable.icon;
        final long notifyWhen = System.currentTimeMillis();
        final String notifyService = Context.NOTIFICATION_SERVICE;
        
        NotificationManager notifyManager = (NotificationManager)
            getSystemService(notifyService);
        
        Notification notification = new Notification(
            notifyIcon, message, notifyWhen);
        
        Context context = getApplicationContext();
        CharSequence notifyTitle = message;
        CharSequence notifyText = "You saved this message.";
        
        Intent notifyIntent = new Intent(
            this, MyAndroidSdkAppActivity2.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
            this, 0, notifyIntent, 0);
        notification.setLatestEventInfo(
            context, notifyTitle, notifyText, contentIntent);
        
        notifyManager.notify(notifyRef, notification);
    }

    /**
     * application specific wrapper to read a message that might be in a file
     * */
    public String readMessageFile() 
    {
        String ret_str = "";
        try {
            ret_str = stringFromPrivateApplicationFile(MESSAGEFILE);
        }
        catch (Throwable t) {
            makeToast("Message read failed: " + t.toString());
        }    
        return ret_str;
    }

    /**
     * application specific wrapper to write a message to a file
     * */
    public void writeMessageFile(CharSequence message) 
    {
        try {
            stringToPrivateApplicationFile(MESSAGEFILE, message.toString()); 
        }
        catch (Throwable t) {
            makeToast("Message write failed: " + t.toString());
        }        
    }
    
    /**
     * general method to read a string from a private application file
     * */
    public String stringFromPrivateApplicationFile(String name)
        throws java.lang.Throwable
    {
        String ret_str = "";
        try {
            InputStream is = openFileInput(name);
            if (null != is) {
                InputStreamReader   tmp_isr = new InputStreamReader(is);
                BufferedReader      tmp_rdr = new BufferedReader(tmp_isr);
                String              tmp_str = "";
                StringBuilder       tmp_buf = new StringBuilder();
                while ( (tmp_str = tmp_rdr.readLine()) != null) {
                    tmp_buf.append(tmp_str);
                }
                is.close();
                ret_str = tmp_buf.toString();
            }
        }
        catch (java.io.FileNotFoundException e) {
            /** file has not been created - log this */
            Log.e(TAG, "File not found: " + e.toString(), e);
        }
        catch (Throwable t) {
            Log.e(TAG, "File read failed: " + t.toString(), t);
            throw t; /** other unexpected exception - rethrow it */
        }
        return ret_str;
    }

    /**
     * general method to write a string to a private application file
     * */
    public void stringToPrivateApplicationFile(String name, String data) 
        throws java.lang.Throwable
    {
        try {
            OutputStreamWriter tmp_osw = new OutputStreamWriter(
                    openFileOutput(name, Context.MODE_PRIVATE));
            tmp_osw.write(data);
            tmp_osw.close();
        }
        catch (Throwable t) {
            Log.e(TAG, "File write failed: " + t.toString(), t);
            throw t; /** other unexpected exception - rethrow it */
        }
    }
    
    /**
     * how to make a menu - implement onCreateOptionsMenu()
     * */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        Log.d(TAG, "in [onCreateOptionsMenu()]...");
        
        // always first delegate to the base class in case of system menus
        super.onCreateOptionsMenu(menu);

        /** our 1st demo menu grouping - menu sub-item titles should be 
         * read from the strings table rather than embedded in app code */
        final int mnu_grp1 = 1;
        menu.add(mnu_grp1, 1, 1, "My Menu Item 1-1");
        menu.add(mnu_grp1, 2, 2, "My Menu Item 1-2");
        
        // our 2nd demo menu grouping
        final int mnu_grp2 = 2;
        menu.add(mnu_grp2, 3, 3,"My Menu Item 2-1");
        menu.add(mnu_grp2, 4, 4,"My Menu Item 2-2");
        
        return true; // true for a visible menu, false for an invisible one
    }

    /**
     * how to respond to a menu - implement onOptionsItemSelected()
     * */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        final int mnu_id = item.getItemId();
        Log.d(TAG, "Menu Item: ID [" + mnu_id + "] selected");
        switch(mnu_id) {
            case 1: // our own items
            case 2:
            case 3:
            case 4:
                makeToast("Menu [" + mnu_id + "] " + message); 
                return true; // true when we have handled al our own items
            default: // not our items
                Log.d(TAG, "Menu Item: UNKNOWN ID selected");
                return super.onOptionsItemSelected(item); // pass item id up
        }
    }

}

package com.pietergreyling.android.kahluadroid;

import static com.pietergreyling.android.common.CommonAndroidCodeLibrary.makeToast;
import static com.pietergreyling.android.common.CommonAndroidCodeLibrary.showOkAlertDialog;
import static com.pietergreyling.android.common.CommonAndroidCodeLibrary.showNotification;
import static com.pietergreyling.android.common.CommonAndroidCodeLibrary.stringFromAssetFile;
import static com.pietergreyling.android.common.CommonAndroidCodeLibrary.stringFromPrivateApplicationFile;
import static com.pietergreyling.android.common.CommonAndroidCodeLibrary.stringToPrivateApplicationFile;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import se.krka.kahlua.converter.KahluaConverterManager;
import se.krka.kahlua.integration.LuaCaller;
import se.krka.kahlua.integration.LuaReturn;
import se.krka.kahlua.integration.annotations.LuaMethod;
import se.krka.kahlua.integration.expose.LuaJavaClassExposer;
import se.krka.kahlua.j2se.J2SEPlatform;
import se.krka.kahlua.luaj.compiler.LuaCompiler;
import se.krka.kahlua.vm.KahluaTable;
import se.krka.kahlua.vm.KahluaThread;
import se.krka.kahlua.vm.KahluaUtil;
import se.krka.kahlua.vm.LuaClosure;
import se.krka.kahlua.vm.Platform;
import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class KahluaDroid extends Activity
{
    protected static final String          TAG      = "KahluaDroid";
    protected String                       APP_TITLE;
    protected String                       STARTUP_SCRIPT_CODE;

    /**
     * Kahlua reference variables
     */
    protected final Platform               _platform;
    protected final KahluaTable            _env;
    protected final KahluaConverterManager _manager;
    protected final LuaJavaClassExposer    _exposer;
    protected final LuaCaller              _caller;
    protected final KahluaThread           _thread;

    /**
     * GUI reference variables
     */
    protected EditText                     _txtInput;
    protected TextView                     _txtOutput;
    protected Button                       _cmdRunAsync;
    protected Button                       _cmdRunSync;
    protected Button                       _cmdClear;
    protected final StringBuffer           _buffer  = new StringBuffer();

    public KahluaDroid()
    {
        _platform = new J2SEPlatform();
        _env = _platform.newEnvironment();
        _manager = new KahluaConverterManager();
        KahluaTable java = _platform.newTable();
        _env.rawset("Java", java);
        _exposer = new LuaJavaClassExposer(_manager, _platform, _env, java);
        _exposer.exposeGlobalFunctions(this);
        _caller = new LuaCaller(_manager);
        _thread = new KahluaThread(new PrintStream(new OutputStream()
        {
            @Override
            public void write(int i) throws IOException
            {
                _buffer.append(Character.toString((char) i));
            }
        }), _platform, _env);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreate(): ...");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initialize();
        runStartupScript();
    }

    /**
     * Sets up Activity user interface controls and resources.
     */
    protected void initialize()
    {
        APP_TITLE = getString(R.string.app_desc) + " [v."
                + getString(R.string.app_version) + "]";
        setTitle(APP_TITLE);
        _txtInput = (EditText) findViewById(R.id.edittext_input);
        _txtInput.setTextSize(TextSize.NORMAL);
        _txtInput.setTypeface(Typeface.MONOSPACE);

        _cmdRunAsync = (Button) findViewById(R.id.button_run_async);
        _cmdRunAsync.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                executeAsync();
            }
        });

        _cmdRunSync = (Button) findViewById(R.id.button_run_sync);
        _cmdRunSync.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                executeSync();
            }
        });

        _cmdClear = (Button) findViewById(R.id.button_clear);
        _cmdClear.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                _txtInput.getText().clear();
            }
        });

        _txtOutput = (TextView) findViewById(R.id.textview_output);
        _txtOutput.setTextSize(TextSize.NORMAL);
        _txtOutput.setTypeface(Typeface.MONOSPACE);
        _txtOutput.setTextColor(Color.GREEN);
        _txtOutput.setBackgroundColor(Color.DKGRAY);
    }
    
    protected void runStartupScript() 
    {
        try 
        {
            STARTUP_SCRIPT_CODE = readStartupScript();
            Log.d(TAG, 
                    "runStartupScript(): STARTUP_SCRIPT_CODE:\n" + 
                    STARTUP_SCRIPT_CODE);
            executeSync(STARTUP_SCRIPT_CODE);
        }
        catch (Throwable t) 
        {
            Log.e(TAG, "runStartupScript(): FAILED!", t);
            STARTUP_SCRIPT_CODE = "";
        }
    }

    private class KahluaAsyncTask extends AsyncTask<String, Void, Void>
    {
        @Override
        protected void onPreExecute()
        {
            _cmdRunAsync.setEnabled(false);
            _cmdRunAsync.setText(getString(R.string.button_run_async_text_busy));
            //_txtInput.getText().clear(); /* leave input code to play with, fix etc. */
            flush();
        }

        @Override
        protected Void doInBackground(String... strings)
        {
            // flush();
            String source = strings[0];
            try
            {
                LuaClosure closure = LuaCompiler.loadstring(source, null, _env);
                LuaReturn result = _caller.protectedCall(_thread, closure);
                if (result.isSuccess())
                {
                    for (Object o : result)
                    {
                        _buffer.append(KahluaUtil.tostring(o, _thread) + "\n");
                    }
                }
                else
                {
                    _buffer.append(result.getErrorString() + "\n");
                    _buffer.append(result.getLuaStackTrace() + "\n");
                }
            }
            catch (Exception e)
            {
                _buffer.append(e.getMessage() + "\n");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            flush();
            _cmdRunAsync.setText(getString(R.string.button_run_async_text_wait));
            _cmdRunAsync.setEnabled(true);
        }

        private void flush()
        {
            // output.append(_buffer.toString());
            // prepend _buffer to output
            String oldoutput = (_txtOutput.getText()).toString();
            String newoutput = _buffer.toString() + oldoutput;
            _txtOutput.setText(newoutput);
            _buffer.setLength(0);
        }
    };

    protected void executeAsync()
    {
        // prepend source to output
        final String source = _txtInput.getText().toString();
        Log.d(TAG, "executeAsync(): " + source);
        // _txtOutput.append("> " + source + "\n");
        String oldoutput = (_txtOutput.getText()).toString();
        String newoutput = ("> " + source + "\n") + oldoutput;
        _txtOutput.setText(newoutput);
        // run the code asynchronously
        KahluaAsyncTask task = new KahluaAsyncTask();
        task.execute(source);
    }

    protected void executeSync()
    {
        // prepend source to output
        final String source = _txtInput.getText().toString();
        Log.d(TAG, "executeSync(): " + source);
        // _txtOutput.append("> " + source + "\n");
        String oldoutput = (_txtOutput.getText()).toString();
        String newoutput = ("> " + source + "\n") + oldoutput;
        _txtOutput.setText(newoutput);
        _cmdRunSync.setText(getString(R.string.button_run_sync_text_busy));
        executeSync(source);
    }

    protected void executeSync(String source)
    {
        try
        {
            LuaClosure closure = LuaCompiler.loadstring(source, null, _env);
            LuaReturn result = _caller.protectedCall(_thread, closure);
            if (result.isSuccess())
            {
                for (Object o : result)
                {
                    _buffer.append(KahluaUtil.tostring(o, _thread) + "\n");
                }
            }
            else
            {
                _buffer.append(result.getErrorString() + "\n");
                _buffer.append(result.getLuaStackTrace() + "\n");
            }
        }
        catch (Exception e)
        {
            _buffer.append(e.getMessage() + "\n");
        }
        finally {
            flushSync();
            _cmdRunSync.setText(getString(R.string.button_run_sync_text_wait));
        }        
    }

    private void flushSync()
    {
        // output.append(_buffer.toString());
        // prepend _buffer to output
        String oldoutput = (_txtOutput.getText()).toString();
        String newoutput = _buffer.toString() + oldoutput;
        _txtOutput.setText(newoutput);
        _buffer.setLength(0);
    }

    protected class TextSize
    {
        protected static final int SMALL  = 14;
        protected static final int NORMAL = 16;
        protected static final int LARGE  = 18;
    }

    /**
     * Implement our application menu using an XML menu layout and the ADK MenuInflater.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // always first delegate to the base class in case of system menus
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.kahluadroid_main_menu, menu);
        // true for a visible menu, false for an invisible one
        return true;
    }

    /**
     * Respond to our application menu events.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        final int mnu_id = item.getItemId();
        switch (mnu_id) {
            case R.id.menu_itm_snippets_load:
                loadSnippetsAssetFile();
                return true;
            case R.id.menu_itm_startup_script_load:
                loadStartupScript();
                return true;
            case R.id.menu_itm_startup_script_save:
                saveStartupScript();
                return true;
            default: // not our items
                return super.onOptionsItemSelected(item); // pass item id up
        }
    }

    /**
     * Loads the example snippets from the samples asset file.
     * Note that we provide illustrative exception alerts which might or
     * might not be a wise thing for end-user applications in general.
     */
    private void loadSnippetsAssetFile()
    {
        String buffer = "";
        try {
            buffer = stringFromAssetFile(this,
                    getString(R.string.file_name_snippets));
            _txtInput.setText(buffer);
        }
        catch (Throwable t) {
            Log.e(TAG, "loadSnippetsAssetFile(): LOAD FAILED!", t);
            showOkAlertDialog(this, t.toString(), "Load Kahlua Snippets");
        }
    }
    
    /**
     * Reads work previously saved to the startup script lua file.
     * Note that we provide illustrative exception alerts which might or
     * might not be a wise thing for end-user applications in general.
     */
    protected void loadStartupScript() 
    {
        String buffer = readStartupScript();
        //if(buffer.)
        _txtInput.setText(buffer);    
    }
    
    protected String readStartupScript()
    {
        String buffer = "";
        try {
            buffer = stringFromPrivateApplicationFile(this,
                    getString(R.string.file_name_startup_script));
            return buffer;
        }
        catch (Throwable t) {
            Log.e(TAG, "readStartupScript(): NO STARTUP SCRIPT!", t);
            //showOkAlertDialog(this, t.toString(), "Read Startup Script");
            return "";
        }
    }    

    /**
     * Writes work to be saved to the startup script lua file.
     * Note that we provide illustrative exception alerts which might or
     * might not be a wise thing for end-user applications in general.
     */
    protected void saveStartupScript()
    {
        String code = _txtInput.getText().toString();
        try {
            stringToPrivateApplicationFile(this,
                    getString(R.string.file_name_startup_script), code);
            makeToast(this, "Startup Script Saved");
        }
        catch (Throwable t) {
            Log.e(TAG, "saveStartupScript(): SAVE FAILED!", t);
            showOkAlertDialog(this, t.toString(), "Save Startup Script");
        }
    }

/***************************************************************************
     * Lua Methods start here
     * 
     */

    @LuaMethod(global = true)
    public void sleep(double seconds)
    {
        try
        {
            Thread.sleep((long) (seconds * 1000));
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }
    }

    @LuaMethod(global = true)
    public void lua_setvar(CharSequence varname, CharSequence value)
    {
        _env.rawset(varname, value);
    }

    @LuaMethod(global = true)
    public String lua_getvar(CharSequence varname)
    {
        String value = (String)_env.rawget(varname);
        return value;
    }

    @LuaMethod(global = true)
    public void app_settextsize()
    {
        Double size = (Double)_env.rawget("text_size");
        app_settextsize(size);
    }

    @LuaMethod(global = true)
    public void app_settextsize(Double size)
    {
        switch (size.intValue()) {
            case 1:
                _txtInput.setTextSize(TextSize.SMALL);
                _txtOutput.setTextSize(TextSize.SMALL);
                break;
            case 2:
                _txtInput.setTextSize(TextSize.NORMAL);
                _txtOutput.setTextSize(TextSize.NORMAL);
                break;
            case 3:
                _txtInput.setTextSize(TextSize.LARGE);
                _txtOutput.setTextSize(TextSize.LARGE);
                break;
            default:
                _txtInput.setTextSize(TextSize.NORMAL);
                _txtOutput.setTextSize(TextSize.NORMAL);
        }        
    }

    @LuaMethod(global = true)
    public void app_settextcolor()
    {
        Double color = (Double)_env.rawget("text_color");
        app_settextcolor(color);
    }

    @LuaMethod(global = true)
    public void app_settextcolor(Double color)
    {
        switch (color.intValue()) {
            case 1:
                _txtOutput.setTextColor(Color.BLACK);
                _txtOutput.setBackgroundColor(Color.WHITE);
                break;
            case 2:
                _txtOutput.setTextColor(Color.GREEN);
                _txtOutput.setBackgroundColor(Color.DKGRAY);
                break;
            case 3:
                _txtOutput.setTextColor(Color.LTGRAY);
                _txtOutput.setBackgroundColor(Color.BLUE);
                break;
            default:
                _txtOutput.setTextColor(Color.GREEN);
                _txtOutput.setBackgroundColor(Color.DKGRAY);
        }        
    }

    @LuaMethod(global = true)
    public void android_alert(CharSequence message)
    {
        new AlertDialog.Builder(this)
            .setTitle(getString(R.string.app_name))
            .setMessage(message)
            .setPositiveButton("OK", null).show();
    }

    @LuaMethod(global = true)
    public void android_alert(CharSequence message, CharSequence title)
    {
        new AlertDialog.Builder(this).setTitle(title).setMessage(message)
                .setPositiveButton("OK", null).show();
    }

    @LuaMethod(global = true)
    public void android_toast(CharSequence message)
    {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @LuaMethod(global = true)    
    public void android_notify(
            CharSequence title, 
            CharSequence tickerText, 
            CharSequence message) 
    {
        showNotification(
                getApplicationContext(),
                KahluaDroid.class,
                tickerText,
                title,
                message,
                R.drawable.icon_practical_andy_blue);
    }
    
    @LuaMethod(global = true)
    public String android_version()
    {
        String full_version = 
            String.format("[v:%s.%s][sdk: %s][codename: %s]", 
                    VERSION.RELEASE,
                    VERSION.INCREMENTAL,
                    VERSION.SDK_INT,
                    VERSION.CODENAME);
        return full_version;
    }
    
    @LuaMethod(global = true)
    public String android_release()
    {
        String release = String.format("%s", VERSION.RELEASE);
        return release;
    }

    @LuaMethod(global = true)
    public String android_sdk()
    {
        String sdk_level = String.format("%s", VERSION.SDK_INT);
        return sdk_level;
    }
}

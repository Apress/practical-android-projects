package com.pietergreyling.android.cocoadroid;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.*;
import android.widget.*;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import com.pietergreyling.android.R;

import java.io.*;
import java.util.ArrayList;

import static com.pietergreyling.android.common.CommonAndroidCodeLibrary.*;

public class CocoaDroidActivity extends Activity implements View.OnClickListener
{
    protected static final String TAG = "CocoaDroidActivity";

    protected EditText _txtInput = null;
    protected EditText _txtOutput = null;
    protected ImageButton _cmdEnter = null;
    protected Button _cmdLoadScratch = null;
    protected Button _cmdSaveScratch = null;
    protected Button _cmdClear = null;
    protected ListView _outputListView = null;
    OutputStringArrayAdapter _outputArrayAdapter = null;
    ArrayList<String> _outputArrayList = new ArrayList<String>();
    // The input and output streams that form the communications
    // channels with the Cocoa-BASIC interpreter
    protected ByteArrayInputStream _inputStream = null;
    protected ByteArrayOutputStream _outputStream = null;
    // The embedded Cocoa-BASIC interpreter instance reference
    protected CocoaDroidCommandInterpreter _commandInterpreter = null;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreate(): ...");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initialize();
    }

    /**
     * Sets up Activity user interface controls and resources.
     */
    protected void initialize()
    {
        // set a custom title from the strings table
        setTitle(getString(R.string.app_desc));

        // get a handle on and configure the input and text fields
        _txtInput = (EditText) findViewById(R.id.txt_input);
        _txtInput.setTextSize(TextSize.NORMAL);
        _txtInput.setTypeface(Typeface.MONOSPACE);
        _txtOutput = (EditText) findViewById(R.id.txt_output);
        _txtOutput.setTextSize(TextSize.NORMAL);
        _txtOutput.setTypeface(Typeface.MONOSPACE);
        _txtOutput.setTextColor(Color.GREEN);
        _txtOutput.setBackgroundColor(Color.DKGRAY);

        // get a handle on the enter command button and its event handler
        _cmdEnter = (ImageButton) findViewById(R.id.cmd_enter);
        _cmdEnter.setOnClickListener(this);

        // get a handle on the scratchpad buttons and event handling
        _cmdLoadScratch = (Button) findViewById(R.id.cmd_load_scratch);
        _cmdLoadScratch.setOnClickListener(this);
        _cmdSaveScratch = (Button) findViewById(R.id.cmd_save_scratch);
        _cmdSaveScratch.setOnClickListener(this);

        // button for clearing buffers
        _cmdClear = (Button) findViewById(R.id.cmd_clear);
        _cmdClear.setOnClickListener(this);

        // set up and get a handle on the output list view using an array adapter
        _outputListView = (ListView) findViewById(R.id.lst_output);
        _outputArrayAdapter = new OutputStringArrayAdapter(this, _outputArrayList);
        _outputListView.setAdapter(_outputArrayAdapter);

        // show the startup about banner
        showAbout();

        // and let the interpreter show a little sample
        String print_hello = "print \">> ready...\"";
        evalCodeStringSync(print_hello);
        _txtInput.setText("");
    }

    /**
     * Start up our script engine with a copyright notice.
     * This also demonstrates the general principle of reusing the BASIC interpreter
     * by passing commands into the input stream and letting it do the work.
     */
    protected void showAbout()
    {
        // ask the BASIC interpreter to print the startup banner
        String aboutCommand =
                "PRINT \"" + getString(R.string.app_copy_cocoabasic) + "\"\n";
        aboutCommand = aboutCommand +
                "PRINT \"" + getString(R.string.app_copy_cocoadroid) + "\"\n";
        // also ask it to print a little usage message
        aboutCommand = aboutCommand +
                "PRINT \"" + getString(R.string.app_usage_01) + "\"";
        // now submit the work using the synchronous evaluation
        evalCodeStringSync(aboutCommand);
        _txtInput.setText("");
    }

    /**
     * Write code evaluation output to the result text view and roll the array list
     * with the stack of previous output results.
     *
     * @param result
     */
    protected void writeOutput(String result)
    {
        if (0 == result.length() || "".equals(result.trim())) {
            result = "-- null or empty result --";
        }
        Log.d(TAG, "writeOutput(): " + result);
        // always add previous result to index 0; it is the top of the list
        _outputArrayList.add(0, _txtOutput.getText().toString());
        _outputArrayAdapter.notifyDataSetChanged();
        _txtOutput.setText(result); // to the scratch output area
    }

    /**
     * Clear the input/output buffers with Clear button.
     */
    protected void clearBuffers()
    {
        // always add previous buffer to index 0; it is the top of the list
        _outputArrayList.add(0, _txtOutput.getText().toString());
        _outputArrayAdapter.notifyDataSetChanged();
        _txtInput.setText("");
        _txtOutput.setText("");
    }

    /**
     * Centralized onClick listener for all views, particularly buttons.
     *
     * @param v
     */
    public void onClick(View v)
    {
        Log.d(TAG, "onClick(): ".concat(v.toString()));
        String codeString = _txtInput.getText().toString();
        switch (v.getId()) {
            case R.id.cmd_enter:
                new EvalCodeStringAsyncTask().execute(codeString);
                break;
            case R.id.cmd_load_scratch:
                loadScratchFiles();
                break;
            case R.id.cmd_save_scratch:
                saveScratchFiles();
                break;
            case R.id.cmd_clear:
                clearBuffers();
                break;
            default:
                // do nothing
                break;
        }
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
        inflater.inflate(R.menu.cocoadroid_main_menu, menu);
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
            case R.id.menu_itm_work_load:
                loadWorkFile();
                return true;
            case R.id.menu_itm_work_save:
                saveWorkFile();
                return true;
            case R.id.menu_itm_samples_load:
                loadSamplesAssetFile();
                return true;
            case R.id.menu_itm_app_about:
                showAbout();
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
    private void loadSamplesAssetFile()
    {
        String buffer = "";
        try {
            buffer = stringFromAssetFile(this,
                    getString(R.string.file_name_samples));
            _txtInput.setText(buffer);
        }
        catch (Throwable t) {
            Log.e(TAG, "loadSamplesAssetFile(): LOAD FAILED!", t);
            showOkAlertDialog(this,
                    String.format("%s\n%s",
                            getString(R.string.exception_on_samples_file_load),
                            t.toString()),
                    getString(R.string.title_samples_file_load));
        }
    }

    /**
     * Reads work previously saved to the scratch files.
     * Note that we provide illustrative exception alerts which might or
     * might not be a wise thing for end-user applications in general.
     */
    protected void loadScratchFiles()
    {
        String scratch_input = "";
        String scratch_output = "";
        try {
            scratch_input = stringFromPrivateApplicationFile(this,
                    getString(R.string.file_name_scratch_input));
            scratch_output = stringFromPrivateApplicationFile(this,
                    getString(R.string.file_name_scratch_output));
            _txtInput.setText(scratch_input);
            _txtOutput.setText(scratch_output);
        }
        catch (Throwable t) {
            Log.e(TAG, "loadScratchFiles(): LOAD FAILED!", t);
            showOkAlertDialog(this,
                    String.format("%s\n%s",
                            getString(R.string.exception_on_scratch_files_load),
                            t.toString()),
                    getString(R.string.title_scratch_files_load));
        }
    }

    /**
     * Writes work to be saved to the scratch files.
     * Note that we provide illustrative exception alerts which might or
     * might not be a wise thing for end-user applications in general.
     */
    protected void saveScratchFiles()
    {
        String scratch_input = _txtInput.getText().toString();
        String scratch_output = _txtOutput.getText().toString();
        try {
            stringToPrivateApplicationFile(this,
                    getString(R.string.file_name_scratch_input), scratch_input);
            stringToPrivateApplicationFile(this,
                    getString(R.string.file_name_scratch_output), scratch_output);
            makeToast(this, "Scratch Files saved");
        }
        catch (Throwable t) {
            Log.e(TAG, "saveScratchFiles(): SAVE FAILED!", t);
            showOkAlertDialog(this,
                    String.format("%s\n%s",
                            getString(R.string.exception_on_scratch_files_save),
                            t.toString()),
                    getString(R.string.title_scratch_files_save));
        }
    }

    /**
     * Reads work previously saved to the work file.
     * Note that we provide illustrative exception alerts which might or
     * might not be a wise thing for end-user applications in general.
     */
    protected void loadWorkFile()
    {
        String buffer = "";
        try {
            buffer = stringFromPrivateApplicationFile(this,
                    getString(R.string.file_name_work));
            _txtInput.setText(buffer);
        }
        catch (Throwable t) {
            Log.e(TAG, "loadWorkFile(): LOAD FAILED!", t);
            showOkAlertDialog(this,
                    String.format("%s\n%s",
                            getString(R.string.exception_on_work_file_load),
                            t.toString()),
                    getString(R.string.title_work_file_load));
        }
    }

    /**
     * Writes work to be saved to the work file.
     * Note that we provide illustrative exception alerts which might or
     * might not be a wise thing for end-user applications in general.
     */
    protected void saveWorkFile()
    {
        String work = _txtInput.getText().toString();
        try {
            stringToPrivateApplicationFile(this,
                    getString(R.string.file_name_work), work);
            makeToast(this, "Work File saved");
        }
        catch (Throwable t) {
            Log.e(TAG, "saveWorkFile(): SAVE FAILED!", t);
            showOkAlertDialog(this,
                    String.format("%s\n%s",
                            getString(R.string.exception_on_work_file_save),
                            t.toString()),
                    getString(R.string.title_work_file_save));
        }
    }

    /**
     * Interpret and execute (evaluate) the given code fragment.
     * This version of evalCodeString is reserved by convention for internally
     * invoking non-user initiated interpreter code evaluation, i.e., from code.
     * It is not invoked by the EvalCodeStringAsyncTask whereas the companion
     * evalCodeString() method is.
     *
     * @param codeString
     * @return The result of the evaluation drawn off the interpreter output stream.
     */
    protected String evalCodeStringSync(String codeString)
    {
        Log.d(TAG, "evalCodeStringSync(): " + codeString);
        // invoke eval bypassing use of an EvalCodeStringAsyncTask instance
        String result = evalCodeString(codeString);
        if (0 == result.length() || "".equals(result.trim())) {
            result = "-- null or empty result --";
        }
        writeOutput(result);
        // also place on input area since the user might not have entered this
        // the method might have been initiated by code and not by the Enter button
        _txtInput.setText(codeString);
        return result;
    }

    /**
     * Interpret and execute (evaluate) the given code fragment.
     * It is invoked by the EvalCodeStringAsyncTask.
     *
     * @param codeString
     * @return The result of the evaluation drawn off the interpreter output stream.
     */
    protected String evalCodeString(String codeString)
    {
        Log.d(TAG, "evalCodeString(): " + codeString);

        String result = null;

        // set up and direct the input and output streams
        try {
            _inputStream = inputStreamFromString(codeString);
            _outputStream = new ByteArrayOutputStream();

            // fire up the command interpreter to evaluate the source code buffer
            _commandInterpreter = new CocoaDroidCommandInterpreter(_inputStream, _outputStream);
            try {
                _commandInterpreter.eval();
                // extract the resulting text output from the stream
                result = stringFromOutputStream(_outputStream);
            }
            catch (Throwable t) {
                Log.e(TAG, String.format("evalCodeString(): UNSUPPORTED OPERATION!\n[\n%s\n]\n%s",
                        codeString, t.toString()), t);
                result = ("UNSUPPORTED OPERATION!\n[\n" + codeString + "\n]\n" + t.toString());
            }
        }
        catch (Throwable t) {
            Log.e(TAG, String.format("evalCodeString(): UNSUPPORTED OPERATION!\n[\n%s\n]\n%s",
                    codeString, t.toString()), t);
            result = ("UNSUPPORTED OPERATION!\n[\n" + codeString + "\n]\n" + t.toString());
        }

        return result;
    }

    /**
     * Apply the percentile progress value passed into this
     * method by performing some useful application operation.
     *
     * @param progressPercent
     */
    protected void setProgressPercent(Integer progressPercent)
    {
        Log.d(TAG, "setProgressPercent(): " + progressPercent.toString());
    }

    /**
     * Handle program code interpretation as asynchronous operations.
     * More on Android threading here:
     * http://developer.android.com/resources/articles/painless-threading.html
     * http://developer.android.com/reference/android/os/AsyncTask.html
     * android.os.AsyncTask<Params, Progress, Result>
     */
    protected class EvalCodeStringAsyncTask extends AsyncTask<String, Integer, String>
    {
        protected String doInBackground(String... codeString)
        {
            String result = "";
            Log.d(TAG, "doInBackground() [code]: \n" + codeString[0]);
            result = evalCodeString(codeString[0]);
            Log.d(TAG, "doInBackground() [eval]: \n" + result);
            publishProgress((int) (100)); // just to demonstrate how
            return result;
        }

        /**
         * We leave this here for the sake of completeness.
         * Progress update is not implemented.
         *
         * @param progress
         */
        @Override
        protected void onProgressUpdate(Integer... progress)
        {
            setProgressPercent(progress[0]);
        }

        /**
         * Update the GUI output work result edit field.
         *
         * @param result
         */
        @Override
        protected void onPostExecute(String result)
        {
            writeOutput(result);
        }
    }

    /**
     * Custom String ArrayAdapter class that allows us to manipulate the row colors etc.
     */
    protected class OutputStringArrayAdapter extends ArrayAdapter<String>
    {
        OutputStringArrayAdapter(Context context, ArrayList<String> stringArrayList)
        {
            super(context, android.R.layout.simple_list_item_1, stringArrayList);
        }

        public View getView(int position, View convertView, ViewGroup parent)
        {
            TextView txt = new TextView(this.getContext());
            txt.setTextColor(Color.GREEN);
            txt.setTextSize(TextSize.SMALL);
            txt.setText(this.getItem(position));
            return txt;
        }
    }

    protected class TextSize
    {
        protected static final int SMALL = 14;
        protected static final int NORMAL = 16;
        protected static final int LARGE = 18;
    }

}



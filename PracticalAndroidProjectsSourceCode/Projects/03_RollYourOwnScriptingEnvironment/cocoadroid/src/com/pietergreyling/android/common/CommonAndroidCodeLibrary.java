package com.pietergreyling.android.common;

import java.io.*;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.widget.Toast;

/**
 * A class called CommonAndroidCodeLibrary but I prefer CACL ("Cackle") for a bunch
 * of conveniently lumped together functions (since they are mostly statics anyway).
 */
public class CommonAndroidCodeLibrary
{
    protected static final String _encoding;
    protected static final String _newline;

    static {
        _encoding = System.getProperty("file.encoding");
        _newline = System.getProperty("line.separator");
    }

    /**
     * Wraps a String into an InputStream and returns the InputStream.
     *
     * @param from
     * @return
     * @throws java.io.IOException
     */
    public static ByteArrayInputStream inputStreamFromString(String from) throws IOException
    {
        return (new ByteArrayInputStream(from.getBytes(_encoding)));
    }

    /**
     * Reads the stream contents of an InputStream into a String and returns the String.
     *
     * @param from
     * @return
     * @throws IOException
     */
    public static String stringFromInputStream(InputStream from)
            throws IOException
    {
        return stringFromInputStream(from, 8192);
    }

    public static String stringFromInputStream(InputStream from, int buffSize)
            throws IOException
    {
        ByteArrayOutputStream into = new ByteArrayOutputStream();
        byte[] buf = new byte[buffSize];
        for (int n; 0 < (n = from.read(buf));) {
            into.write(buf, 0, n);
        }
        into.close();
        return (new String(into.toByteArray(), _encoding));
    }

    /**
     * Reads the stream contents of an OutputStream into a String and returns the String.
     *
     * @param from
     * @return
     * @throws IOException
     */
    public static String stringFromOutputStream(ByteArrayOutputStream from) throws IOException
    {
        return from.toString(_encoding);
    }

    /**
     * Reads the contents of a Raw Resource File into a String and returns the String.
     *
     * @param context
     * @param id
     * @return
     * @throws IOException
     */
    public static String stringFromRawResourceFile(Context context, int id) throws IOException
    {
        Resources r = context.getResources();
        InputStream is = r.openRawResource(id);
        String result = stringFromInputStream(is);
        is.close();
        return result;
    }

    /**
     * Reads the contents of an Asset File into a String and returns the String.
     *
     * @param context
     * @param filename
     * @return
     * @throws IOException
     */
    public static String stringFromAssetFile(Context context, String filename) throws IOException
    {
        AssetManager am = context.getAssets();
        InputStream is = am.open(filename);
        String result = stringFromInputStream(is);
        is.close();
        return result;
    }

    /**
     * Reads the contents of a private application file into a String and returns the String.
     *
     * @param context
     * @param name
     * @return
     * @throws java.lang.Throwable
     */
    public static String stringFromPrivateApplicationFile(Context context, String name)
            throws java.lang.Throwable
    {
        String ret_str = "";
        InputStream is = context.openFileInput(name);
        InputStreamReader tmp_isr = new InputStreamReader(is);
        BufferedReader tmp_rdr = new BufferedReader(tmp_isr);
        String tmp_str = "";
        StringBuilder tmp_buf = new StringBuilder();
        while ((tmp_str = tmp_rdr.readLine()) != null) {
            tmp_buf.append(tmp_str);
            tmp_buf.append(_newline); // readLine drops newlines, put them back
        }
        is.close();
        ret_str = tmp_buf.toString();
        return ret_str;
    }

    /**
     * Writes a string to a private application file.
     *
     * @param context
     * @param name
     * @param data
     * @throws java.lang.Throwable
     */
    public static void stringToPrivateApplicationFile(Context context, String name, String data)
            throws java.lang.Throwable
    {
        OutputStreamWriter tmp_osw = new OutputStreamWriter(
                context.openFileOutput(name, Context.MODE_PRIVATE));
        tmp_osw.write(data);
        tmp_osw.close();
    }

    public static void makeToast(Context context, CharSequence message)
    {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void showOkAlertDialog(Context context, CharSequence message)
    {
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    public static void showOkAlertDialog(Context context, CharSequence message, CharSequence title)
    {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    /**
     * Bare bones but functional system notification mechanism all wrapped-up in one function.
     *
     * @param context
     * @param myClass
     * @param tickerText
     * @param contentTitle
     * @param contentText
     * @param notifyIconId
     */
    public void showNotification(
            Context context,
            Class myClass,
            CharSequence tickerText,
            CharSequence contentTitle,
            CharSequence contentText,
            int notifyIconId)
    {
        final int notifyRef = 1;
        final long notifyWhen = System.currentTimeMillis();
        final String notifyService = Context.NOTIFICATION_SERVICE;

        NotificationManager notifyManager = (NotificationManager)
                context.getSystemService(notifyService);

        Notification notification = new Notification(
                notifyIconId, tickerText, notifyWhen);

        Intent notifyIntent = new Intent(context, myClass);
        PendingIntent contentIntent = PendingIntent.getActivity(
                context, 0, notifyIntent, 0);
        notification.setLatestEventInfo(
                context, contentTitle, contentText, contentIntent);

        notifyManager.notify(notifyRef, notification);
    }

}

package org.ljordan.android.chapter06;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.webkit.WebView;

public class HtmlOnAndroid extends Activity {

	private final static String KEY_HIGH_SCORE = "KEY_HIGH_SCORE";

	private WebView webView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		webView = new WebView(this);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.loadUrl("file:///android_asset/index.html");
		webView.addJavascriptInterface(new JavaScriptInterface(), "android");
		webView.setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);

		setContentView(webView);
	}

	final class JavaScriptInterface {
		JavaScriptInterface() {
		}

		public int getScreenWidth() {
			return webView.getWidth();
		}

		public int getScreenHeight() {
			// Removing 5 pixels to prevent vertical scrolling.
			return webView.getHeight() - 5;
		}

		public int getHighScore() {
			SharedPreferences preferences = getPreferences(MODE_WORLD_WRITEABLE);
			return preferences.getInt(KEY_HIGH_SCORE, 0);
		}

		public void setHighScore(int value) {
			SharedPreferences preferences = getPreferences(MODE_PRIVATE);
			Editor editor = preferences.edit();
			editor.putInt(KEY_HIGH_SCORE, value);
			editor.commit();
		}

	}
}
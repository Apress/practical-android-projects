package org.ljordan.tweetface;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.http.AccessToken;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

public class TweetFace extends Activity implements View.OnClickListener {

	private static final String APP = "TweetFace";

	private final static String URL_STATUSES_USER_TIMELINE = "http://api.twitter.com/1/statuses/user_timeline.json";
	private final static String URL_OAUTH_REQUEST_TOKEN = "https://api.twitter.com/oauth/request_token";
	private final static String URL_OAUTH_ACCESS_TOKEN = "http://twitter.com/oauth/access_token";
	private final static String URL_OAUTH_AUTHORIZE = "http://twitter.com/oauth/authorize";
	public final static String URL_CALLBACK = "tweetface://twitter";

	private final static String CONSUMER_KEY = "69VrkKoURS6qNU2ErIjoPA";
	private final static String CONSUMER_SECRET = "CezmSUG3LdcOW5E6ncYyozWPZyC3tSNfZFcdiuXU";
	public final static String FB_APPLICATION_ID = "158406107535204";

	private final static int DIALOG_CONFIRM_TWEET = 10;
	private final static int DIALOG_CONFIRM_WALL = 20;

	private TextView tweetView;
	private TextView statusView;

	private Button loginTwitterButton;
	private Button loginFacebookButton;
	private Button replyOnTwitterButton;
	private Button facebookWallButton;

	private JSONObject tweet = null;
	private HttpClient client = new DefaultHttpClient();

	private OAuthProvider provider = new CommonsHttpOAuthProvider(
			URL_OAUTH_REQUEST_TOKEN, URL_OAUTH_ACCESS_TOKEN,
			URL_OAUTH_AUTHORIZE);
	private CommonsHttpOAuthConsumer consumer = new CommonsHttpOAuthConsumer(
			CONSUMER_KEY, CONSUMER_SECRET);

	private Facebook facebook = new Facebook(FB_APPLICATION_ID);
	private AuthorizeListener authorizeListener = new AuthorizeListener();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		tweetView = (TextView) findViewById(R.id.tweetView);
		statusView = (TextView) findViewById(R.id.statusView);

		loginTwitterButton = (Button) findViewById(R.id.loginTwitterButton);
		loginFacebookButton = (Button) findViewById(R.id.loginFacebookButton);
		replyOnTwitterButton = (Button) findViewById(R.id.replyOnTwitterButton);
		facebookWallButton = (Button) findViewById(R.id.facebookWallButton);

		loginTwitterButton.setOnClickListener(this);
		loginFacebookButton.setOnClickListener(this);
		replyOnTwitterButton.setOnClickListener(this);
		facebookWallButton.setOnClickListener(this);

		new ReadTweet().execute("lucasljordan");
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		Uri uri = intent.getData();
		if (uri != null) {
			String uriString = uri.toString();
			if (uriString.startsWith(URL_CALLBACK)) {
				try {
					String verifier = uri
							.getQueryParameter(OAuth.OAUTH_VERIFIER);
					provider.retrieveAccessToken(consumer, verifier);
					statusView.setText("Authenticated with Twitter!");
					replyOnTwitterButton.setEnabled(true);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}

			}
		} else {
			//probably the first time Activity is loaded.
		}
	}

	public JSONObject readStatus(String screenName)
			throws ClientProtocolException, IOException, JSONException {
		StringBuilder fullUrl = new StringBuilder(URL_STATUSES_USER_TIMELINE);
		fullUrl.append("?screen_name=");
		fullUrl.append(screenName);

		HttpGet get = new HttpGet(fullUrl.toString());
		HttpResponse response = client.execute(get);

		int statusCode = response.getStatusLine().getStatusCode();

		if (statusCode == 200) {
			HttpEntity entity = response.getEntity();
			String json = EntityUtils.toString(entity);
			JSONArray bunchOfTweets = new JSONArray(json);
			JSONObject mostRecentTweet = bunchOfTweets.getJSONObject(0);
			return mostRecentTweet;
		} else {
			String reason = response.getStatusLine().getReasonPhrase();
			throw new RuntimeException("Trouble reading status(code="
					+ statusCode + "):" + reason);
		}
	}

	@Override
	public void onClick(View v) {
		if (v == loginTwitterButton) {
			loginTwitter();
		} else if (v == loginFacebookButton) {
			facebook.authorize(this, new String[] { "publish_stream" },
					authorizeListener);
		} else if (v == replyOnTwitterButton) {
			showDialog(DIALOG_CONFIRM_TWEET);
		} else if (v == facebookWallButton) {
			showDialog(DIALOG_CONFIRM_WALL);
		}
		//unknown button.
	}

	private void loginTwitter() {
		try {
			String authUrl = provider.retrieveRequestToken(consumer,
					URL_CALLBACK);
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl)));
		} catch (Exception e) {
			Log.e(APP, e.getMessage());
			throw new RuntimeException(e);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == DIALOG_CONFIRM_TWEET) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Do you want to create a tweet?")
					.setCancelable(false)
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									statusView.setText("Creating tweet...");
									new UpdateStatus().execute("Not Used");
								}
							})
					.setNegativeButton("No",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});
			return builder.create();
		} else if (id == DIALOG_CONFIRM_WALL) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Do you want to write on your wall?")
					.setCancelable(false)
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									statusView.setText("writing to wall...");
									facebookWallButton.setEnabled(false);
									new PostOnWall().execute("not used");
								}
							})
					.setNegativeButton("No",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									facebookWallButton.setEnabled(true);
									dialog.cancel();
								}
							});
			return builder.create();
		} else {
			return null;
		}
	}

	public void updateStatus() {
		try {
			Configuration conf = new ConfigurationBuilder()
					.setOAuthConsumerKey(consumer.getConsumerKey())
					.setOAuthConsumerSecret(consumer.getConsumerSecret())
					.build();

			AccessToken accessToken = new AccessToken(consumer.getToken(),
					consumer.getTokenSecret());
			Twitter twitter = new TwitterFactory(conf)
					.getOAuthAuthorizedInstance(accessToken);

			String tweetText = "@lucasljordan is trying to count up to: "
					+ System.currentTimeMillis();

			// finally, we can update twitter.
			twitter.updateStatus(tweetText);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void postOnWall() throws FileNotFoundException,
			MalformedURLException, IOException {
		Bundle bundle = new Bundle();
		bundle.putString("message",
				"Working through the examples for the book Practical Android Projects.");
		bundle.putString("link",
				"http://www.facebook.com/apps/application.php?id=158406107535204");

		facebook.request("me/feed", bundle, "POST");
	}

	private class PostOnWall extends AsyncTask<String, Integer, String> {
		@Override
		protected String doInBackground(String... notUsed) {
			try {
				postOnWall();
				return "Posted to you wall.";
			} catch (Exception e) {
				Log.w("TweetFace", e);
				return "error reading posting to wall";
			}
		}

		protected void onPostExecute(final String result) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					facebookWallButton.setEnabled(true);
					statusView.setText(result);
				}
			});
		}
	}

	private class ReadTweet extends AsyncTask<String, Integer, String> {
		@Override
		protected String doInBackground(String... screenNames) {
			try {
				tweet = readStatus(screenNames[0]);
				return tweet.getString("text");
			} catch (Exception e) {
				Log.w("TweetFace", e);
				return "error reading tweet";
			}
		}

		protected void onPostExecute(final String result) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					tweetView.setText(result);
				}
			});
		}
	}

	private class UpdateStatus extends AsyncTask<String, Integer, String> {
		@Override
		protected String doInBackground(String... notUsed) {
			try {
				updateStatus();
				return "Tweet Created!";
			} catch (Exception e) {
				Log.w("TweetFace", e);
				return "Error creating tweet.";
			}
		}

		protected void onPostExecute(final String result) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					statusView.setText(result);
				}
			});
		}
	}

	private class AuthorizeListener implements DialogListener {
		@Override
		public void onComplete(Bundle values) {
			statusView.setText("Authenticated with Facebook!");
			facebookWallButton.setEnabled(true);
		}

		@Override
		public void onFacebookError(FacebookError e) {
			Log.w("TweetFace", e);
			statusView.setText("Trouble With FB, see logs");
		}

		@Override
		public void onError(DialogError e) {
			Log.w("TweetFace", e);
			statusView.setText("Trouble With Dialog, see logs");
		}

		@Override
		public void onCancel() {
			statusView.setText("Did not authenticate.");
		}
	}

}
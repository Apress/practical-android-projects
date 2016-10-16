package org.ljordan.orb_quest;

import java.net.URLEncoder;
import java.util.List;
import java.util.ListIterator;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class ScoreDialog extends Dialog implements
		android.view.View.OnClickListener {

	public final static String PREF_USER_NAME = "PREF_USER_NAME";

	public final static String SERVICE_URL = "http://pap-game-service.appspot.com/add_high_score?highscore=";

	private EditText playerNameEditText;
	private Button yesButton;
	private Button noButton;

	private GameActivity activity;

	public ScoreDialog(GameActivity activity) {
		super(activity);
		this.activity = activity;

		setContentView(R.layout.score_dialog);
		setTitle("High Score");

		playerNameEditText = (EditText) findViewById(R.id.playerNameEditText);
		yesButton = (Button) findViewById(R.id.yesButton);
		noButton = (Button) findViewById(R.id.noButton);

		SharedPreferences settings = getContext().getSharedPreferences(
				HighScoreView.PREFS_ORB_QUEST, 0);
		String unsername = settings.getString(PREF_USER_NAME, "User Name");
		playerNameEditText.setText(unsername);

		yesButton.setOnClickListener(this);
		noButton.setOnClickListener(this);

		LinearLayout rootLayout = (LinearLayout) findViewById(R.id.dialogRoot);

		BitmapDrawable bitmapDrawable = (BitmapDrawable) activity
				.getResources().getDrawable(R.drawable.dialog_graphic);

		ImageView imageView = new ImageView(activity);
		imageView.setImageDrawable(bitmapDrawable);

		rootLayout.addView(imageView);

	}

	@Override
	public void onClick(View view) {
		HighScore newScore = makeHighScore();
		updateLocalHighScore(newScore);

		if (view == yesButton) {
			new ReportScore().execute(newScore);
			dismiss();
			activity.dialogClosed();
		} else {
			dismiss();
			activity.dialogClosed();
		}
	}

	private void updateLocalHighScore(HighScore newScore) {
		try {

			SharedPreferences settings = getContext().getSharedPreferences(
					HighScoreView.PREFS_ORB_QUEST, 0);
			String json = settings.getString(HighScoreView.PREF_HIGH_SCORE,
					HighScore.createDefaultScores());

			JSONArray currentScores = new JSONArray(json);

			List<HighScore> highscores = HighScore.toList(currentScores);
			highscores.add(newScore);

			JSONArray updatedScores = HighScore.toJSONArray(highscores);
			Editor editor = settings.edit();

			editor.putString(HighScoreView.PREF_HIGH_SCORE,
					updatedScores.toString());
			editor.putString(PREF_USER_NAME, newScore.getUsername());

			editor.commit();

		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	private HighScore makeHighScore() {
		HighScore score = new HighScore();
		score.setUsername(playerNameEditText.getEditableText().toString());
		score.setDate(System.currentTimeMillis());
		score.setScore(activity.getScore());

		LocationManager locationManager = (LocationManager) getContext()
				.getSystemService(Context.LOCATION_SERVICE);
		List<String> providers = locationManager.getProviders(true);

		ListIterator<String> li = providers.listIterator();
		while (li.hasNext()) {
			String provider = li.next();
			Location location = locationManager.getLastKnownLocation(provider);
			if (location != null) {
				score.setLatitude(location.getLatitude());
				score.setLongitude(location.getLongitude());
				break;
			}
		}
		score.setGameName("Orb Quest");

		return score;
	}

	private class ReportScore extends AsyncTask<HighScore, Integer, HighScore> {

		@Override
		protected HighScore doInBackground(HighScore... highscores) {
			try {
				DefaultHttpClient client = new DefaultHttpClient();

				StringBuilder fullUrl = new StringBuilder(SERVICE_URL);

				HighScore highScore = highscores[0];
				JSONObject jsonObject = highScore.toJSONObject();
				String jsonStr = jsonObject.toString();

				fullUrl.append(URLEncoder.encode(jsonStr));

				HttpGet get = new HttpGet(fullUrl.toString());
				HttpResponse response = client.execute(get);

				int statusCode = response.getStatusLine().getStatusCode();

				if (statusCode == 200) {
					HttpEntity entity = response.getEntity();
					String json = EntityUtils.toString(entity);
					return new HighScore(new JSONObject(json));
				} else {
					String reason = response.getStatusLine().getReasonPhrase();
					throw new RuntimeException("Trouble adding score(code="
							+ statusCode + "):" + reason);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}

package org.ljordan.gamemanager;

import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

public class UsersOfGameActivity extends Activity implements OnClickListener {

	public HttpClient client = new DefaultHttpClient();

	private TableLayout tableLayout;
	private EditText usernameEditText;
	private EditText gamenameEditText;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.users_of_game);

		tableLayout = (TableLayout) findViewById(R.id.tableLayout);
		Button doGetButton = (Button) findViewById(R.id.doGet);
		doGetButton.setOnClickListener(this);
		usernameEditText = (EditText) findViewById(R.id.usernameTextEdit);
		gamenameEditText = (EditText) findViewById(R.id.gamenameTextEdit);

	}

	@Override
	public void onClick(View v) {
		new GetUsersOfGame().execute();
	}

	private class GetUsersOfGame extends AsyncTask<Integer, Integer, JSONArray> {
		@Override
		protected JSONArray doInBackground(Integer... counts) {
			try {

				String username = usernameEditText.getText().toString();
				String gamename = gamenameEditText.getText().toString();

				StringBuilder fullUrl = new StringBuilder(
						GameManager.SERVICE_URL);

				fullUrl.append("query_high_scores?count=10");
				fullUrl.append("&username=");
				fullUrl.append(URLEncoder.encode(username, "UTF-8"));
				fullUrl.append("&game_name=");
				fullUrl.append(URLEncoder.encode(gamename, "UTF-8"));

				HttpGet get = new HttpGet(fullUrl.toString());
				HttpResponse response = client.execute(get);

				int statusCode = response.getStatusLine().getStatusCode();

				if (statusCode == 200) {
					HttpEntity entity = response.getEntity();
					String json = EntityUtils.toString(entity);
					return new JSONArray(json);
				} else {
					String reason = response.getStatusLine().getReasonPhrase();
					throw new RuntimeException("Trouble getting scores(code="
							+ statusCode + "):" + reason);
				}

			} catch (Exception e) {
				Log.w("TopTenActivity", e);
				throw new RuntimeException(e);
			}
		}

		protected void onPostExecute(final JSONArray result) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					try {
						displayResults(result);
					} catch (JSONException e) {
						Log.w("TopTenActivity", e);
					}
				}
			});
		}
	}

	protected void displayResults(JSONArray result) throws JSONException {
		tableLayout.removeAllViews();

		TableRow row = new TableRow(this);
		row.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));

		TextView userTitleView = new TextView(this);
		userTitleView.setText("Username:");
		userTitleView.setTextSize(18);
		userTitleView.setPadding(10, 2, 100, 2);
		row.addView(userTitleView);

		TextView scoreTitleView = new TextView(this);
		scoreTitleView.setText("Score:");
		scoreTitleView.setTextSize(18);
		row.addView(scoreTitleView);

		for (int i = 0; i < result.length(); i++) {
			HighScore highscore = new HighScore(result.getJSONObject(i));

			row = new TableRow(this);
			row.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
					LayoutParams.FILL_PARENT));

			TextView userView = new TextView(this);
			userView.setText(highscore.getUsername());
			userView.setTextSize(14);
			userView.setPadding(10, 2, 100, 2);
			row.addView(userView);

			TextView scoreView = new TextView(this);
			scoreView.setText("" + highscore.getScore());
			scoreView.setTextSize(14);
			row.addView(scoreView);

			tableLayout.addView(row, new TableLayout.LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

		}
	}

}

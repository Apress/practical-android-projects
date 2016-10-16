package org.ljordan.gamemanager;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.AlertDialog;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class UsersLocationActivity extends MapActivity {

	private MapView mapView;
	public HttpClient client = new DefaultHttpClient();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_location);
		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);

		new GetTopTen().execute(100);
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	private class GetTopTen extends AsyncTask<Integer, Integer, JSONArray> {
		@Override
		protected JSONArray doInBackground(Integer... counts) {
			try {
				StringBuilder fullUrl = new StringBuilder(
						GameManager.SERVICE_URL);

				fullUrl.append("query_high_scores?count=");
				fullUrl.append(counts[0]);

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
		Drawable drawable = this.getResources().getDrawable(
				R.drawable.green_orb);

		HighscoreOverlay highscoreOverlay = new HighscoreOverlay(drawable);

		for (int i = 0; i < result.length(); i++) {
			HighScore highscore = new HighScore(result.getJSONObject(i));

			String username = highscore.getUsername();
			String score = "" + highscore.getScore();
			int latitude = (int) highscore.getLatitude().doubleValue() * 1000000;
			int longitude = (int) highscore.getLongitude().doubleValue() * 1000000;

			List<Overlay> mapOverlays = mapView.getOverlays();

			GeoPoint point = new GeoPoint(latitude, longitude);

			OverlayItem item = new OverlayItem(point, username, "Score: "
					+ score);

			highscoreOverlay.addOverlay(item);
			mapOverlays.add(highscoreOverlay);
		}
	}

	public class HighscoreOverlay extends ItemizedOverlay<OverlayItem> {

		private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();

		public HighscoreOverlay(Drawable defaultMarker) {
			super(boundCenterBottom(defaultMarker));
		}

		public void addOverlay(OverlayItem overlay) {
			mOverlays.add(overlay);
			populate();
		}

		@Override
		protected OverlayItem createItem(int i) {
			return mOverlays.get(i);
		}

		@Override
		public int size() {
			return mOverlays.size();
		}

		@Override
		protected boolean onTap(int index) {
			OverlayItem item = mOverlays.get(index);
			AlertDialog.Builder dialog = new AlertDialog.Builder(
					UsersLocationActivity.this);
			dialog.setTitle(item.getTitle());
			dialog.setMessage(item.getSnippet());
			dialog.show();
			return true;
		}
	}
}

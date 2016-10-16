package org.ljordan.orb_quest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HighScore {

	private Long key;
	private String username;
	private Long score;
	private String gameName;
	private Double longitude;
	private Double latitude;
	private Long date;

	public HighScore() {

	}

	public HighScore(JSONObject jsonObject) throws JSONException {
		if (jsonObject.has("key")) {
			key = jsonObject.getLong("key");
		}
		if (jsonObject.has("username")) {
			username = jsonObject.getString("username");
		}
		if (jsonObject.has("score")) {
			score = jsonObject.getLong("score");
		}
		if (jsonObject.has("gameName")) {
			gameName = jsonObject.getString("gameName");
		}
		if (jsonObject.has("longitude")) {
			longitude = jsonObject.getDouble("longitude");
		}
		if (jsonObject.has("latitude")) {
			latitude = jsonObject.getDouble("latitude");
		}
		if (jsonObject.has("date")) {
			date = jsonObject.getLong("date");
		}
	}

	public JSONObject toJSONObject() throws JSONException {
		JSONObject result = new JSONObject();
		result.put("key", key);
		result.put("username", username);
		result.put("score", score);
		result.put("gameName", gameName);
		result.put("longitude", longitude);
		result.put("latitude", latitude);
		result.put("date", date);

		return result;
	}

	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Long getScore() {
		return score;
	}

	public void setScore(Long score) {
		this.score = score;
	}

	public String getGameName() {
		return gameName;
	}

	public void setGameName(String gameName) {
		this.gameName = gameName;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Long getDate() {
		return date;
	}

	public void setDate(Long date) {
		this.date = date;
	}

	public static String createDefaultScores() {
		try {
			JSONArray result = new JSONArray();
			for (int i = 0; i < 10; i++) {
				HighScore highScore = new HighScore();
				highScore.setUsername("No Score");
				highScore.setScore((long) i);
				result.put(highScore.toJSONObject());
			}

			return result.toString();
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	public static List<HighScore> toList(JSONArray jsonArray) {
		try {
			List<HighScore> result = new ArrayList<HighScore>();
			int length = jsonArray.length();
			for (int i = 0; i < length; i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				HighScore score = new HighScore(jsonObject);
				result.add(score);
			}

			Collections.sort(result, new HighScoreComparator());
			return result;
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	public static JSONArray toJSONArray(List<HighScore> highscores) {
		try {
			JSONArray result = new JSONArray();
			Collections.sort(highscores, new HighScoreComparator());

			while (highscores.size() > 10) {
				highscores.remove(10);
			}

			for (HighScore score : highscores) {
				result.put(score.toJSONObject());
			}

			return result;
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	private static class HighScoreComparator implements Comparator<HighScore> {

		@Override
		public int compare(HighScore score1, HighScore score2) {
			return score2.getScore().compareTo(score1.getScore());//Descending order
		}

	}

}

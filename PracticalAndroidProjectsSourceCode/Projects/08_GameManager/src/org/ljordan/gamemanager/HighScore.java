package org.ljordan.gamemanager;

import java.net.URLEncoder;

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

	public static void main(String[] args) {
		try {
			HighScore score = new HighScore();
			score.setDate(12345678l);
			score.setGameName("orb quest");
			score.setLatitude(43.12);
			score.setLongitude(-77.67);
			score.setScore(100l);
			score.setUsername("ljordan");

			System.out.println(URLEncoder.encode(score.toJSONObject()
					.toString(), "UTF-8"));
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}
}

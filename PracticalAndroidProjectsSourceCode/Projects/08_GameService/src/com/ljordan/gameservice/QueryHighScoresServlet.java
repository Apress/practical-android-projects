package com.ljordan.gameservice;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.repackaged.org.json.JSONArray;

@SuppressWarnings("serial")
public class QueryHighScoresServlet extends HttpServlet {

	private static final String PARAM_COUNT = "count";
	private static final String PARAM_GTR_LAT = "gtr_lat";
	private static final String PARAM_LST_LAT = "lst_lat";
	private static final String PARAM_GTR_LON = "gtr_lon";
	private static final String PARAM_LST_LON = "lst_lon";
	private static final String PARAM_GTR_SCORE = "gtr_score";
	private static final String PARAM_USERNAME = "username";
	private static final String PARAM_LST_SCORE = "lst_score";
	private static final String PARAM_GAMENAME = "game_name";

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		String count = req.getParameter(PARAM_COUNT);
		String gtrLat = req.getParameter(PARAM_GTR_LAT);
		String lstLat = req.getParameter(PARAM_LST_LAT);
		String gtrLon = req.getParameter(PARAM_GTR_LON);
		String lstLon = req.getParameter(PARAM_LST_LON);
		String username = req.getParameter(PARAM_USERNAME);
		String gtrScore = req.getParameter(PARAM_GTR_SCORE);
		String lstScore = req.getParameter(PARAM_LST_SCORE);
		String gameName = req.getParameter(PARAM_GAMENAME);

		List<HighScore> highScores = queryHighScores(count, lstLat, gtrLat,
				lstLon, gtrLon, username, lstScore, gtrScore, gameName);

		resp.setContentType("application/json");
		Writer writer = resp.getWriter();

		try {

			JSONArray result = new JSONArray();
			for (HighScore highscore : highScores) {
				result.put(highscore.toJSONObject());
			}
			writer.write(result.toString());

		} catch (Exception e) {
			throw new IOException(e);
		} finally {
			writer.close();
		}
	}

	private List<HighScore> queryHighScores(String count, String lstLat,
			String gtrLat, String lstLon, String gtrLon, String username,
			String lstScore, String gtrScore, String gameName) {

		List<HighScore> results = new ArrayList<HighScore>();

		PersistenceManager pm = null;
		try {
			pm = PMF.get().getPersistenceManager();

			Map<String, String> paramNameToType = new HashMap<String, String>();
			Map<String, Object> paramNameToValue = new HashMap<String, Object>();
			List<String> filters = new ArrayList<String>();

			if (lstLat != null) {
				filters.add("latitude < plstLat");
				paramNameToType.put("plstLat", "Double");
				paramNameToValue.put("plstLat", Double.parseDouble(lstLat));
			}
			if (gtrLat != null) {
				filters.add("latitude > pgtrLat");
				paramNameToType.put("pgtrLat", "Double");
				paramNameToValue.put("pgtrLat", Double.parseDouble(gtrLat));
			}
			if (lstLon != null) {
				filters.add("longitude < plstLon");
				paramNameToType.put("plstLon", "Double");
				paramNameToValue.put("plstLon", Double.parseDouble(lstLon));
			}
			if (gtrLon != null) {
				filters.add("longitude > pgtrLon");
				paramNameToType.put("pgtrLon", "Double");
				paramNameToValue.put("pgtrLon", Double.parseDouble(gtrLon));
			}
			if (username != null) {
				filters.add("username == pusername");
				paramNameToType.put("pusername", "String");
				paramNameToValue.put("pusername", username);
			}
			if (lstScore != null) {
				filters.add("score < plstScore");
				paramNameToType.put("plstScore", "Long");
				paramNameToValue.put("plstScore", Double.parseDouble(lstScore));
			}
			if (gtrScore != null) {
				filters.add("score > pgtrScore");
				paramNameToType.put("pgtrScore", "Long");
				paramNameToValue.put("pgtrScore", Long.parseLong(gtrScore));
			}
			if (gameName != null) {
				filters.add("gameName == pgameName");
				paramNameToType.put("pgameName", "String");
				paramNameToValue.put("pgameName", gameName);
			}

			Query query = pm.newQuery(HighScore.class);

			query.setOrdering("score desc");
			if (count != null) {
				query.setRange(0, Long.parseLong(count));
			}

			if (filters.size() == 0) {
				for (Object obj : (List) query.execute()) {
					results.add((HighScore) obj);
				}
				return results;
			} else {

				StringBuffer filter = new StringBuffer();

				ListIterator<String> li = filters.listIterator();
				while (li.hasNext()) {
					filter.append(li.next());
					if (li.hasNext()) {
						filter.append(" & ");
					}
				}

				List values = new ArrayList();
				StringBuffer parameters = new StringBuffer();
				Iterator<Map.Entry<String, String>> i = paramNameToType
						.entrySet().iterator();

				while (i.hasNext()) {
					Map.Entry<String, String> param = i.next();
					parameters.append(param.getValue());
					parameters.append(' ');
					parameters.append(param.getKey());
					if (i.hasNext()) {
						parameters.append(',');
					}
					values.add(paramNameToValue.get(param.getKey()));
				}
				query.setFilter(filter.toString());
				query.declareParameters(parameters.toString());

				for (Object obj : (List) query.executeWithArray(values
						.toArray())) {
					results.add((HighScore) obj);
				}
				return results;
			}

		} finally {
			pm.close();
		}
	}
}

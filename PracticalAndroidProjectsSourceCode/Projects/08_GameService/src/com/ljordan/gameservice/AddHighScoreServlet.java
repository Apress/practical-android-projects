package com.ljordan.gameservice;

import java.io.IOException;
import java.io.Writer;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.repackaged.org.json.JSONObject;

@SuppressWarnings("serial")
public class AddHighScoreServlet extends HttpServlet {

	public final static String PARAM_HIGHSCORE = "highscore";

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		String json = req.getParameter(PARAM_HIGHSCORE);

		resp.setContentType("application/json");
		Writer writer = resp.getWriter();

		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {

			HighScore highScore = new HighScore(new JSONObject(json));

			pm.makePersistent(highScore);

			writer.write(highScore.toJSONObject().toString());
		} catch (Exception e) {
			throw new IOException(e);
		} finally {
			pm.close();
			writer.close();
		}

	}
}

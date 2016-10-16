package org.ljordan.orb_quest;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.view.View;

public class HighScoreView extends View {

	public final static String PREFS_ORB_QUEST = "PREF_ORB_QUEST";
	public final static String PREF_HIGH_SCORE = "PREF_HIGH_SCORE";

	private List<HighScore> highscores;

	public HighScoreView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public HighScoreView(Context context) {
		super(context);
		init();
	}

	private void init() {
		SharedPreferences settings = getContext().getSharedPreferences(
				PREFS_ORB_QUEST, 0);
		String json = settings.getString(PREF_HIGH_SCORE,
				HighScore.createDefaultScores());
		try {
			JSONArray jsonArray = new JSONArray(json);

			highscores = HighScore.toList(jsonArray);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void onDraw(Canvas canvas) {
		int width = getWidth();
		int height = getHeight();

		//Draw Background
		canvas.drawColor(Color.GRAY);

		Rect innerRect = new Rect(5, 5, width - 5, height - 5);
		Paint innerPaint = new Paint();
		LinearGradient linearGradient = new LinearGradient(0, 0, 0, height,
				Color.LTGRAY, Color.DKGRAY, Shader.TileMode.MIRROR);
		innerPaint.setShader(linearGradient);

		canvas.drawRect(innerRect, innerPaint);

		//Draw Title
		Path titlePath = new Path();
		titlePath.moveTo(10, 70);
		titlePath.cubicTo(width / 3, 90, width / 3 * 2, 50, width - 10, 70);

		Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		titlePaint.setColor(Color.RED);
		titlePaint.setTextSize(38);
		titlePaint.setShadowLayer(5, 0, 5, Color.BLACK);

		canvas.drawTextOnPath("Your High Scores", titlePath, 0, 0, titlePaint);

		//Draw Line
		Paint linePaint = new Paint();
		linePaint.setStrokeWidth(10);
		linePaint.setColor(Color.WHITE);
		linePaint.setStrokeCap(Cap.ROUND);

		float[] direction = new float[] { 0, -5, -5 };
		EmbossMaskFilter maskFilter = new EmbossMaskFilter(direction, .5f, 8, 3);
		linePaint.setMaskFilter(maskFilter);

		canvas.drawLine(15, 100, width - 15, 100, linePaint);

		//Draw Scores
		Paint scorePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		scorePaint.setShadowLayer(5, 0, 5, Color.BLACK);
		scorePaint.setTextSize(20);

		RadialGradient radialGradient = new RadialGradient(width / 2,
				height / 2, width, Color.WHITE, Color.GREEN, TileMode.MIRROR);
		scorePaint.setShader(radialGradient);

		int index = 0;
		for (HighScore score : highscores) {
			canvas.drawText(score.getUsername(), 40, 150 + index * 30,
					scorePaint);
			canvas.drawText("" + score.getScore(), width - 115,
					150 + index * 30, scorePaint);

			index++;
		}

	}
}

package org.ljordan.orb_quest;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class GameActivity extends Activity {

	private final static int DIALOG_CONFIRM_SHARE = 10;

	private TextView turnsTextView;
	private TextView scoreTextView;
	private GameView gameView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.game);

		turnsTextView = (TextView) findViewById(R.id.turnsTextView);
		scoreTextView = (TextView) findViewById(R.id.scoreTextView);

		gameView = (GameView) findViewById(R.id.gameView);

		gameView.reset(this);
	}

	public void updateValues(int score, int turns) {
		scoreTextView.setText("" + score);
		turnsTextView.setText("" + turns + "  ");
	}

	public Long getScore() {
		return Long.parseLong(scoreTextView.getText().toString());
	}

	public void endGame() {
		showDialog(DIALOG_CONFIRM_SHARE);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == DIALOG_CONFIRM_SHARE) {
			return new ScoreDialog(this);
		} else {
			return null;
		}
	}

	public void dialogClosed() {
		gameView.reset(this);
	}
}

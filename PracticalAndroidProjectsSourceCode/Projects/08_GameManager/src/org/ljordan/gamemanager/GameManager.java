package org.ljordan.gamemanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class GameManager extends Activity implements View.OnClickListener {

	public final static String SERVICE_URL = "http://pap-game-service.appspot.com/";

	private Button topTenButton;
	private Button usersOfGameButton;
	private Button locationButton;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		topTenButton = (Button) findViewById(R.id.viewTopTen);
		usersOfGameButton = (Button) findViewById(R.id.usersOfGame);
		locationButton = (Button) findViewById(R.id.viewLocation);

		topTenButton.setOnClickListener(this);
		usersOfGameButton.setOnClickListener(this);
		locationButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View button) {
		if (button == topTenButton) {
			Intent intent = new Intent(this, TopTenActivity.class);
			startActivity(intent);
		} else if (button == usersOfGameButton) {
			Intent intent = new Intent(this, UsersOfGameActivity.class);
			startActivity(intent);
		} else if (button == locationButton) {
			Intent intent = new Intent(this, UsersLocationActivity.class);
			startActivity(intent);
		}
		//unknown button.
	}
}
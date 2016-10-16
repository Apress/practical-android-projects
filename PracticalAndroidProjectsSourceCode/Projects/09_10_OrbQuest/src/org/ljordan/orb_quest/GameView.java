package org.ljordan.orb_quest;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

public class GameView extends ViewGroup implements View.OnClickListener {

	private int orb_ids[] = new int[3];
	private Random random = new Random();

	private OrbView selectedOrbView = null;
	private boolean acceptInput = true;

	private int score = 0;
	private int turns = 10;

	private GameActivity gameActivity;

	public GameView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public GameView(Context context) {
		super(context);
		init();
	}

	private void init() {
		setBackgroundDrawable(new Background());

		orb_ids[0] = R.drawable.red_orb;
		orb_ids[1] = R.drawable.green_orb;
		orb_ids[2] = R.drawable.blue_orb;
	}

	public void reset(GameActivity gameActivity) {
		this.gameActivity = gameActivity;
		score = 0;
		turns = 10;
		acceptInput = true;

		removeAllViews();

		for (int c = 0; c < 5; c++) {
			for (int r = 0; r < 5; r++) {
				OrbView orbView = new OrbView(getContext(), c, r,
						random.nextInt(3));
				addView(orbView);
			}
		}
		gameActivity.updateValues(score, turns);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
		int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
		int size = Math.min(parentHeight - 20, parentWidth - 20);
		this.setMeasuredDimension(size, size);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int size = getWidth();
		int oneFifth = size / 5;

		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			OrbView orbView = (OrbView) getChildAt(i);
			int left = oneFifth * orbView.getCol();
			int top = oneFifth * orbView.getRow();
			int right = oneFifth * orbView.getCol() + oneFifth;
			int bottom = oneFifth * orbView.getRow() + oneFifth;
			orbView.layout(left, top, right, bottom);
		}
	}

	@Override
	public void onClick(View v) {
		if (acceptInput) {
			if (v instanceof OrbView) {
				OrbView orbView = (OrbView) v;
				if (selectedOrbView == null) {
					selectedOrbView = orbView;
					Animation scale = AnimationUtils.loadAnimation(
							getContext(), R.anim.scale_down);

					orbView.startAnimation(scale);
				} else {
					if (orbView != selectedOrbView) {
						swapOrbs(selectedOrbView, orbView);
						selectedOrbView = null;
					} else {
						Animation scale = AnimationUtils.loadAnimation(
								getContext(), R.anim.scale_up);

						orbView.startAnimation(scale);
						selectedOrbView = null;
					}

				}
			}
		}
	}

	protected void swapOrbs(OrbView orb1, OrbView orb2) {
		turns--;

		acceptInput = false;
		//swap locations
		int col1 = orb1.getCol();
		int row1 = orb1.getRow();
		int col2 = orb2.getCol();
		int row2 = orb2.getRow();

		orb1.setCol(col2);
		orb1.setRow(row2);
		orb2.setCol(col1);
		orb2.setRow(row1);

		//Animate Orb1
		TranslateAnimation trans1 = new TranslateAnimation(0, orb2.getLeft()
				- orb1.getLeft(), 0, orb2.getTop() - orb1.getTop());
		trans1.setDuration(500);
		trans1.setStartOffset(500);

		ScaleAnimation scaleUp1 = new ScaleAnimation(0.5f, 1.0f, 0.5f, 1.0f,
				Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF,
				.5f);
		scaleUp1.setDuration(500);
		scaleUp1.setStartOffset(1000);

		AnimationSet set1 = new AnimationSet(false);
		set1.addAnimation(scaleUp1);
		set1.addAnimation(trans1);

		orb1.startAnimation(set1);

		//Animate Orb2
		ScaleAnimation scaleDown2 = new ScaleAnimation(1.0f, 0.5f, 1.0f, 0.5f,
				Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF,
				.5f);
		scaleDown2.setDuration(500);
		scaleDown2.setInterpolator(new AnticipateOvershootInterpolator());

		TranslateAnimation trans2 = new TranslateAnimation(0, orb1.getLeft()
				- orb2.getLeft(), 0, orb1.getTop() - orb2.getTop());
		trans2.setDuration(500);
		trans2.setStartOffset(500);

		ScaleAnimation scaleUp2 = new ScaleAnimation(1.0f, 2.0f, 1.0f, 2.0f,
				Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF,
				.5f);
		scaleUp2.setDuration(500);
		scaleUp2.setStartOffset(1000);

		AnimationSet set2 = new AnimationSet(false);
		set2.addAnimation(scaleDown2);
		set2.addAnimation(scaleUp2);
		set2.addAnimation(trans2);

		set2.setAnimationListener(new RunAfter() {
			@Override
			public void run() {
				requestLayout();
				checkMatches();
			}
		});
		orb2.startAnimation(set2);
	}

	protected void doneAnimating() {
		requestLayout();
		acceptInput = true;
		gameActivity.updateValues(score, turns);
		if (turns <= 0) {
			gameActivity.endGame();
		}
	}

	protected void checkMatches() {
		Set<OrbView> matchingRows = new HashSet<OrbView>();
		Set<OrbView> matchingCols = new HashSet<OrbView>();

		for (int r = 0; r < 5; r++) {
			Set<OrbView> oneSet = new HashSet<OrbView>();

			OrbView zero = findOrbView(0, r);
			boolean allSame = true;
			for (int c = 0; c < 5; c++) {
				OrbView orbView = findOrbView(c, r);
				if (orbView.getOrbType() != zero.getOrbType()) {
					allSame = false;
					break;
				}
				oneSet.add(orbView);
			}

			if (allSame) {
				matchingRows.addAll(oneSet);
			}
		}

		for (int c = 0; c < 5; c++) {
			Set<OrbView> oneSet = new HashSet<OrbView>();

			OrbView zero = findOrbView(c, 0);
			boolean allSame = true;
			for (int r = 0; r < 5; r++) {
				OrbView orbView = findOrbView(c, r);
				if (orbView.getOrbType() != zero.getOrbType()) {
					allSame = false;
					break;
				}
				oneSet.add(orbView);
			}

			if (allSame) {
				for (OrbView orb : oneSet) {
					if (!matchingRows.contains(orb)) {
						matchingCols.add(orb);
					}
				}
			}
		}

		if (matchingRows.size() == 0 && matchingCols.size() == 0) {
			doneAnimating();
			return;
		}

		int size = getWidth();
		boolean runAfterSet = false;

		final Set<OrbView> allOrbs = new HashSet<GameView.OrbView>(matchingCols);
		allOrbs.addAll(matchingRows);

		if (matchingRows.size() != 0) {
			for (OrbView orbView : matchingRows) {

				ScaleAnimation scaleDown = new ScaleAnimation(1.0f, 0.5f, 1.0f,
						0.5f, Animation.RELATIVE_TO_SELF, 0.5f,
						Animation.RELATIVE_TO_SELF, 0.5f);
				scaleDown.setDuration(500);
				scaleDown.setFillAfter(true);

				TranslateAnimation trans = new TranslateAnimation(0, size, 0, 0);
				trans.setDuration(500);
				trans.setStartOffset(500);
				trans.setFillAfter(true);

				AnimationSet set = new AnimationSet(false);
				set.addAnimation(scaleDown);
				set.addAnimation(trans);

				if (!runAfterSet) {
					runAfterSet = true;
					set.setAnimationListener(new RunAfter() {
						@Override
						public void run() {
							updateRemovedOrbs(allOrbs);
						}
					});
				}

				orbView.startAnimation(set);
			}
		}

		if (matchingCols.size() != 0) {
			for (OrbView orbView : matchingCols) {
				ScaleAnimation scaleDown = new ScaleAnimation(1.0f, 0.5f, 1.0f,
						0.5f, Animation.RELATIVE_TO_SELF, 0.5f,
						Animation.RELATIVE_TO_SELF, 0.5f);
				scaleDown.setDuration(500);
				scaleDown.setFillAfter(true);

				TranslateAnimation trans = new TranslateAnimation(0, 0, 0, size);
				trans.setDuration(500);
				trans.setStartOffset(500);
				trans.setFillAfter(true);

				AnimationSet set = new AnimationSet(false);
				set.addAnimation(scaleDown);
				set.addAnimation(trans);

				if (!runAfterSet) {
					runAfterSet = true;
					set.setAnimationListener(new RunAfter() {
						@Override
						public void run() {
							updateRemovedOrbs(allOrbs);
						}
					});
				}

				orbView.startAnimation(set);
			}
		}

	}

	private void updateRemovedOrbs(Set<OrbView> allOrbs) {
		score += allOrbs.size() * 5;
		for (OrbView orbView : allOrbs) {
			orbView.setRandomType();
		}
		requestLayout();
		checkMatches();
	}

	protected OrbView findOrbView(int col, int row) {
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			View v = getChildAt(i);
			if (v instanceof OrbView) {
				OrbView orbView = (OrbView) v;
				if (orbView.getCol() == col && orbView.getRow() == row) {
					return orbView;
				}
			}
		}
		return null;
	}

	protected class OrbView extends ImageView {
		private int orbType;
		private int col;
		private int row;

		protected OrbView(Context context, int col, int row, int orbType) {
			super(context);
			this.col = col;
			this.row = row;
			this.orbType = orbType;

			Drawable image = getResources().getDrawable(orb_ids[orbType]);
			setImageDrawable(image);
			setClickable(true);
			setOnClickListener(GameView.this);
		}

		public int getOrbType() {
			return orbType;
		}

		public void setRandomType() {
			orbType = random.nextInt(3);
			Drawable image = getResources().getDrawable(orb_ids[orbType]);
			setImageDrawable(image);
		}

		public int getCol() {
			return col;
		}

		public int getRow() {
			return row;
		}

		public void setCol(int col) {
			this.col = col;
		}

		public void setRow(int row) {
			this.row = row;
		}
	}

	private abstract class RunAfter implements Animation.AnimationListener,
			Runnable {

		@Override
		public void onAnimationEnd(Animation animation) {
			run();
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		@Override
		public void onAnimationStart(Animation animation) {
		}

	}

}

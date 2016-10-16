package org.ljordan.orb_quest;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class AboutView extends SurfaceView implements SurfaceHolder.Callback {

	public static Random random = new Random();

	private boolean animating = true;
	private AnimationThread thread;

	private List<Sprite> sprites = new ArrayList<Sprite>();

	public AboutView(Context context, AttributeSet attrs) {
		super(context, attrs);

		SurfaceHolder surfaceHolder = getHolder();
		surfaceHolder.addCallback(this);
		thread = new AnimationThread(surfaceHolder);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		//called when the size of the surface changes, we are not handling this case.
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		animating = true;
		thread.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
		animating = false;
		while (retry) {
			try {
				thread.join();
				retry = false;
			} catch (InterruptedException e) {
			}
		}
	}

	private class AnimationThread extends Thread {
		private SurfaceHolder surfaceHolder;

		AnimationThread(SurfaceHolder surfaceHolder) {
			this.surfaceHolder = surfaceHolder;
		}

		@Override
		public void run() {
			while (animating) {
				Canvas c = null;
				try {
					c = surfaceHolder.lockCanvas(null);
					synchronized (surfaceHolder) {
						doDraw(c);
					}
				} finally {
					if (c != null) {
						surfaceHolder.unlockCanvasAndPost(c);
					}
				}
			}
		}

		public void doDraw(Canvas canvas) {
			addSprites();

			canvas.drawColor(Color.WHITE);

			for (Sprite sprite : sprites) {
				sprite.update(getWidth(), getHeight());
				sprite.draw(canvas);
			}

		}

		private void addSprites() {
			if (sprites.size() == 0) {
				Drawable rOrb = getResources().getDrawable(R.drawable.red_orb);
				Drawable gOrb = getResources()
						.getDrawable(R.drawable.green_orb);
				Drawable bOrb = getResources().getDrawable(R.drawable.blue_orb);

				int width = getWidth();
				int height = getHeight();

				sprites.add(new Sprite(rOrb, width, height));
				sprites.add(new Sprite(gOrb, width, height));
				sprites.add(new Sprite(bOrb, width, height));
			}
		}
	}
}

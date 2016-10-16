package org.ljordan.orb_quest;

import java.util.Random;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

public class Sprite extends Drawable {
	public static Random random = new Random();

	//current location
	private float x;
	private float y;
	private float radius;

	//used for updates
	private float deltaX;
	private float deltaY;
	private float deltaRadius;

	//what the Sprite looks like
	private Drawable drawable;

	public Sprite(Drawable drawable, float width, float height) {
		this.drawable = drawable;

		//Randomize radius
		radius = 10 + random.nextFloat() * 30;

		//Randomize Location
		x = radius + random.nextFloat() * (width - radius);
		y = radius + random.nextFloat() * (height - radius);

		//Randomize Direction
		double direction = random.nextDouble() * Math.PI * 2;
		float speed = random.nextFloat() * .3f + .7f;

		deltaX = (float) Math.cos(direction) * speed;
		deltaY = (float) Math.sin(direction) * speed;

		//Randomize 
		if (random.nextBoolean()) {
			deltaRadius = random.nextFloat() * .2f + .1f;
		} else {
			deltaRadius = random.nextFloat() * -.2f - .1f;
		}

	}

	public void update(int width, int height) {
		if (radius > 40 || radius < 15) {
			deltaRadius *= -1;
		}
		radius += deltaRadius;

		if (x + radius > width) {
			deltaX *= -1;
			x = width - radius;
		} else if (x - radius < 0) {
			deltaX *= -1;
			x = radius;
		}

		if (y + radius > height) {
			deltaY *= -1;
			y = height - radius;
		} else if (y - radius < 0) {
			deltaY *= -1;
			y = radius;
		}
		x += deltaX;
		y += deltaY;

	}

	@Override
	public void draw(Canvas canvas) {

		Rect bounds = new Rect(Math.round(x - radius), Math.round(y - radius),
				Math.round(x + radius), Math.round(y + radius));
		drawable.setBounds(bounds);

		drawable.draw(canvas);
	}

	@Override
	public int getOpacity() {
		return drawable.getOpacity();
	}

	@Override
	public void setAlpha(int alpha) {
		drawable.setAlpha(alpha);
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		drawable.setColorFilter(cf);
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public Drawable getDrawable() {
		return drawable;
	}

	public void setDrawable(Drawable drawable) {
		this.drawable = drawable;
	}

	public float getRadius() {
		return radius;
	}

	public void setRadius(float radius) {
		this.radius = radius;
	}

}

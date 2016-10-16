package org.ljordan.orb_quest;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;

public class Background extends LayerDrawable {

	public Background() {
		super(new Drawable[] { new ColorDrawable(Color.WHITE),
				new GridDrawable() });
	}

	private static class GridDrawable extends ShapeDrawable {
		private GridDrawable() {
			super(createGridPath());
			getPaint().setColor(Color.GRAY);
			getPaint().setStrokeWidth(1.0f);
			getPaint().setStyle(Paint.Style.FILL);
		}
	}

	private static PathShape createGridPath() {
		float size = 1000;
		float colOrRowSize = size / 5.0f;
		float fivePercent = size * 0.05f;

		float onePercent = size * 0.01f;

		Path lines = new Path();
		for (int i = 0; i < 4; i++) {
			float x = i * colOrRowSize + colOrRowSize;

			lines.moveTo(x - onePercent, fivePercent);
			lines.lineTo(x + onePercent, fivePercent);
			lines.lineTo(x + onePercent, size - fivePercent);
			lines.lineTo(x - onePercent, size - fivePercent);
			lines.close();
		}
		for (int i = 0; i < 4; i++) {
			float y = i * colOrRowSize + colOrRowSize;

			lines.moveTo(fivePercent, y - onePercent);
			lines.lineTo(fivePercent, y + onePercent);
			lines.lineTo(size - fivePercent, y + onePercent);
			lines.lineTo(size - fivePercent, y - onePercent);
			lines.close();
		}

		return new PathShape(lines, size, size);
	}

}

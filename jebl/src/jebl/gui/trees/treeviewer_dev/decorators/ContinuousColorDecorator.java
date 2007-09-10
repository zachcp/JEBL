package jebl.gui.trees.treeviewer_dev.decorators;

import jebl.util.Attributable;

import java.util.*;
import java.awt.*;

/**
 * This decorator takes an attribute name and a set of attibutable Objects. It
 * autodetects the type of attributes and then provides a colouring scheme for them
 * based on a gradient between color1 & color2.
 *
 * @author Andrew Rambaut
 * @version $Id$
 */
public class ContinuousColorDecorator implements Decorator {

	public ContinuousColorDecorator(ContinousScale continuousScale,
	                                Color color1, Color color2) throws NumberFormatException {
		this.continuousScale = continuousScale;
		this.color1 = new float[4];
		color1.getRGBComponents(this.color1);
		this.color2 = new float[4];
		color2.getRGBComponents(this.color2);
	}

	// Decorator INTERFACE
	public Paint getPaint(Paint paint) {
		if (this.paint == null) return paint;
		return this.paint;
	}

	public Paint getFillPaint(Paint paint) {
		if (this.fillPaint == null) return paint;
		return fillPaint;
	}

	public Stroke getStroke(Stroke stroke) {
		return stroke;
	}

	public Font getFont(Font font) {
		return font;
	}

	public void setItem(Object item) {
		if (item instanceof Attributable) {
			setAttributableItem((Attributable)item);
		}
	}

	// Private methods
	private void setAttributableItem(Attributable item) {
		paint = null;

		double value = continuousScale.getValue(item);

		if (!Double.isNaN(value)) {
			float p = (float)value;
			float q = 1.0F - p;

			paint = new Color(
					color1[0] * p + color2[0] * q,
					color1[1] * p + color2[1] * q,
					color1[2] * p + color2[2] * q,
					color1[3] * p + color2[3] * q);
			fillPaint = new Color(paint.getRed(), paint.getGreen(), paint.getBlue(), paint.getAlpha() / 2);
		} else {
			paint = fillPaint = null;
		}
	}

	private final ContinousScale continuousScale;

	private final float[] color1;
	private final float[] color2;

	private Color paint = null;
	private Color fillPaint = null;
}

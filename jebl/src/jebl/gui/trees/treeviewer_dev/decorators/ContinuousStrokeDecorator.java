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
public class ContinuousStrokeDecorator implements Decorator {

	public ContinuousStrokeDecorator(ContinousScale continuousScale,
	                                float minWidth, float maxWidth) throws NumberFormatException {
		this.continuousScale = continuousScale;
		this.minWidth = minWidth;
		this.maxWidth = maxWidth;
	}

	// Decorator INTERFACE
	public Paint getPaint(Paint paint) {
		return paint;
	}

	public Paint getFillPaint(Paint paint) {
		return paint;
	}

	public Stroke getStroke(Stroke stroke) {
		if (this.stroke == null) {
			return stroke;
		}
		return this.stroke;
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

		double value = continuousScale.getValue(item);

		if (!Double.isNaN(value)) {
			stroke = new BasicStroke((float)((value * (maxWidth - minWidth)) + minWidth));
		} else {
			stroke = null;
		}
	}

	public ContinousScale getContinuousScale() {
		return continuousScale;
	}

	private final ContinousScale continuousScale;

	private final float minWidth;
	private final float maxWidth;

	private Stroke stroke = null;
}
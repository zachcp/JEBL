package jebl.gui.trees.treeviewer_dev.decorators;

import jebl.util.Attributable;

import java.awt.*;
import java.util.*;

/**
 * This decorator takes an attribute name and a set of attibutable Objects.
 * Colours are given to each individual value.
 *
 * If the data take more values than colors, then they will wrap around
 *
 * @author Andrew Rambaut
 * @version $Id$
 */
public class DiscreteColorDecorator implements Decorator {

	public static Color[] DEFAULT_PAINTS = new Color[] {
			new Color(204, 255, 255),
			new Color(0, 255, 255),
			new Color(255, 204, 153),
			new Color(255, 204, 0),
			new Color(0, 204, 255),
			new Color(0, 255, 0),
			new Color(255, 255, 153),
			new Color(0, 0, 128),
			new Color(198, 66, 0),
			new Color(51, 102, 255),
			new Color(153, 204, 255),
			new Color(255, 153, 0),
			new Color(255, 255, 0),
			new Color(255, 102, 0),
			new Color(230, 6, 6),
			new Color(204, 255, 153),
			new Color(0, 255, 153),
			new Color(0, 0, 255),
			new Color(204, 153, 255),
			new Color(204, 255, 204),
			Color.DARK_GRAY
	};

	public DiscreteColorDecorator(String attributeName, Set<? extends Attributable> items, Color[] paints) {
		this.attributeName = attributeName;

		// First collect the set of all attribute values
		Set<Object> values = new TreeSet<Object>();
		for (Attributable item : items) {
			Object value = item.getAttribute(attributeName);
			if (value != null) {
				values.add(value);
			}
		}

		// now create a paint map for these values
		int i = 0;
		for (Object value : values) {
			colourMap.put(value, paints[i]);
			i = (i + 1) % paints.length;
		}

	}

	// Decorator INTERFACE
	public Paint getPaint(Paint paint) {
		if (this.paint == null) return paint;
		return this.paint;
	}

	public Paint getFillPaint(Paint paint) {
		return paint;
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
		Object value = item.getAttribute(attributeName);
		if (value != null) {
			paint = colourMap.get(value);
		}
	}

	private String attributeName = null;

	Map<Object, Paint> colourMap = new HashMap<Object, Paint>();

	private Paint paint = null;
}

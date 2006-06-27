package jebl.gui.trees.treeviewer_dev.painters;

import jebl.evolution.graphs.Node;
import jebl.evolution.trees.Tree;

import java.awt.*;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public abstract class NodePainter extends AbstractPainter<Node> {


	protected NodePainter() {
	}

	// Abstract

	public abstract String[] getAttributes();

    public abstract void setupAttributes(Tree tree);

    public abstract void setDisplayAttribute(String display, String attribute);

	// Getters

	public Paint getForeground() {
		return foreground;
	}

	public Paint getBackground() {
		return background;
	}

	public Paint getBorderPaint() {
		return borderPaint;
	}

	public Stroke getBorderStroke() {
		return borderStroke;
	}

	public boolean isVisible() {
	    return visible;
	}

	// Setters

	public void setBackground(Paint background) {
	    this.background = background;
	    firePainterChanged();
	}

	public void setBorder(Paint borderPaint, Stroke borderStroke) {
	    this.borderPaint = borderPaint;
	    this.borderStroke = borderStroke;
	    firePainterChanged();
	}

	public void setForeground(Paint foreground) {
	    this.foreground = foreground;
	    firePainterChanged();
	}

	public void setVisible(boolean visible) {
	    this.visible = visible;
	    firePainterChanged();
	}

	private Paint foreground = Color.BLACK;
	private Paint background = null;
	private Paint borderPaint = null;
	private Stroke borderStroke = null;

	private boolean visible = true;

}

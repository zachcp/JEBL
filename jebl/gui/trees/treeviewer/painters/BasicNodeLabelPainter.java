package jebl.gui.trees.treeviewer.painters;

import jebl.evolution.graphs.Node;
import jebl.evolution.trees.Tree;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class BasicNodeLabelPainter extends AbstractLabelPainter<Node> {

	public BasicNodeLabelPainter(String attribute, Tree tree, int defaultSize) {
		super(defaultSize);
		this.attribute = attribute;
		this.tree = tree;
	}

	public BasicNodeLabelPainter(String attribute, Tree tree) {
	    super();
		this.attribute = attribute;
		this.tree = tree;
	}

	protected double getMaxLabelWidth(Graphics2D g2) {
		double maxLabelWidth = 0.0;

		FontMetrics fm = g2.getFontMetrics();
		for (Node node : tree.getNodes()) {
			String label = getLabel(node);
			if (label != null) {
				Rectangle2D rect = fm.getStringBounds(label, g2);
				if (rect.getWidth() > maxLabelWidth) {
				    maxLabelWidth = rect.getWidth();
				}
			}
		}

		return maxLabelWidth;
	}

	protected String getLabel(Node node) {
		Object value = node.getAttribute(attribute);
		if (value != null) {
			return value.toString();
		}
		return null;
	}

	public String getTitle() {
		return "Node Labels";
	}

	private final Tree tree;
	private final String attribute;
}

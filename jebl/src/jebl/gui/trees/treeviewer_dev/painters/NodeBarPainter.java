package jebl.gui.trees.treeviewer_dev.painters;

import jebl.evolution.graphs.Node;
import jebl.evolution.trees.Tree;
import jebl.evolution.trees.RootedTree;
import jebl.gui.trees.treeviewer_dev.TreePane;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class NodeBarPainter extends NodePainter {

	public NodeBarPainter() {

	    setupAttributes(null);
	}

	public void setupAttributes(Tree tree) {
	    java.util.List<String> attributeNames = new ArrayList<String>();
	    if (tree != null) {
	        Set<String> nodeAttributes = new TreeSet<String>();
	        for (Node node : tree.getNodes()) {
	            nodeAttributes.addAll(node.getAttributeNames());
	        }
	        attributeNames.addAll(nodeAttributes);
	    }

	    this.attributes = new String[attributeNames.size()];
	    attributeNames.toArray(this.attributes);

	    firePainterSettingsChanged();
	}

	public void setTreePane(TreePane treePane) {
	    this.treePane = treePane;
	}

	public void calibrate(Graphics2D g2, Node item) {
	    RootedTree tree = treePane.getTree();

	    FontMetrics fm = g2.getFontMetrics();
	    preferredHeight = fm.getHeight();
	    preferredWidth = 0;

		double upper = tree.getHeight(item);
		double lower = tree.getHeight(item);

		Object value = item.getAttribute(displayAttribute);
		if (value != null ) {
			Pattern p = Pattern.compile("{\\s*(\\S+)\\s*,\\s*(\\S+)\\s*}");
			Matcher m = p.matcher(value.toString());
			if (m.matches()) {
			    try {
			        lower = Integer.parseInt(m.group(1));
				    upper = Integer.parseInt(m.group(2));
			    } catch (NumberFormatException nfe) {
			        // ignore (just use the current state).
			    }
			} else {
				// todo - warn the user somehow?
			}
		}

		Rectangle2D rect = treePane.getTreeLayout().getHeightArea(lower, upper).getBounds2D();

		preferredWidth = rect.getWidth();
		preferredHeight = rect.getHeight();
		preferredWidth = 40;
		preferredHeight = 20;
	}

	public double getPreferredWidth() {
	    return preferredWidth;
	}

	public double getPreferredHeight() {
	    return preferredHeight;
	}

	public double getHeightBound() {
	    return preferredHeight;
	}

	/**
	 * The bounds define the shape of the bar so just draw it
	 * @param g2
	 * @param item
	 * @param justification
	 * @param bounds
	 */
	public void paint(Graphics2D g2, Node item, Justification justification, Rectangle2D bounds) {
	    if (getBackground() != null) {
	        g2.setPaint(getBackground());
	        g2.fill(bounds);
	    }

	    if (getBorderPaint() != null && getBorderStroke() != null) {
	        g2.setPaint(getBorderPaint());
	        g2.setStroke(getBorderStroke());
	    }

		g2.draw(bounds);
	}

	public String[] getAttributes() {
	    return attributes;
	}

	public void setDisplayAttribute(String displayAttribute) {
	    this.displayAttribute = displayAttribute;
	    firePainterChanged();
	}

	private double preferredWidth;
	private double preferredHeight;

	protected String displayAttribute;
	protected String[] attributes;

	protected TreePane treePane;
}

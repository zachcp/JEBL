package jebl.gui.trees.treeviewer.painters;

import jebl.evolution.graphs.Node;
import jebl.evolution.trees.Tree;
import jebl.evolution.trees.RootedTree;
import jebl.gui.trees.treeviewer.controlpanels.ControlPanel;

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

	protected String getLabel(Node node) {
        if (attribute.equals("node heights") && tree instanceof RootedTree) {
            return Double.toString(((RootedTree)tree).getHeight(node));
        } else if (attribute.equals("branch lengths") && tree instanceof RootedTree) {
                return Double.toString(((RootedTree)tree).getLength(node));
        } else {
            Object value = node.getAttribute(attribute);
            if (value != null) {
                return value.toString();
            }
        }
        return null;
	}

	public String getTitle() {
		return "Node Labels";
	}

	private final Tree tree;
	private final String attribute;

}

package jebl.gui.trees.treeviewer_dev.painters;

import jebl.evolution.trees.Tree;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.graphs.Node;
import jebl.gui.trees.treeviewer_dev.TreePane;

import java.util.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;
import java.awt.geom.Line2D;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class NodeBarPainter extends NodePainter {

    public static final String LOWER_ATTRIBUTE = "lower";
    public static final String UPPER_ATTRIBUTE = "upper";

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

    public Rectangle2D calibrate(Graphics2D g2, Node item) {
        Point2D nodePoint1 = treePane.getTreeLayout().getNodePoint(item);
        Point2D nodePoint2 = treePane.getTreeLayout().getSecondaryNodePoint(item);

        Line2D bar = new Line2D.Double(nodePoint1, nodePoint2);

        return bar.getBounds2D();
    }

    public double getPreferredWidth() {
        return 1.0;
    }

    public double getPreferredHeight() {
        return 1.0;
    }

    public double getHeightBound() {
        return 1.0;
    }

    /**
     * The bounds define the shape of the bar so just draw it
     * @param g2
     * @param item
     * @param justification
     * @param bounds
     */
    public void paint(Graphics2D g2, Node item, Justification justification, Rectangle2D bounds) {
        Point2D nodePoint1 = treePane.getTreeLayout().getNodePoint(item);
        Point2D nodePoint2 = treePane.getTreeLayout().getSecondaryNodePoint(item);

        RootedTree tree = treePane.getTree();

        double height = tree.getHeight(item);
        double upper = height;
        double lower = height;

        Object value = item.getAttribute(displayAttributes.get(NodeBarPainter.LOWER_ATTRIBUTE));
        if (value != null ) {
            if (value instanceof Number) {
                lower = ((Number)value).doubleValue();
            } else {
                lower = Double.parseDouble(value.toString());
            }
        } else {
            // todo - warn the user somehow?
        }

        value = item.getAttribute(displayAttributes.get(NodeBarPainter.UPPER_ATTRIBUTE));
        if (value != null ) {
            if (value instanceof Number) {
                upper = ((Number)value).doubleValue();
            } else {
                upper = Double.parseDouble(value.toString());
            }
        } else {
            // todo - warn the user somehow?
        }

        Line2D bar = new Line2D.Double(upper, nodePoint1.getY(), lower, nodePoint1.getY());

        g2.setPaint(getForeground());
        g2.setStroke(getStroke());

        g2.draw(bar);
    }

    public String[] getAttributes() {
        return attributes;
    }

    public void setDisplayAttribute(String display, String attribute) {
        displayAttributes.put(display, attribute);
        firePainterChanged();
    }

    private double preferredWidth;
    private double preferredHeight;

    protected Map<String, String> displayAttributes = new HashMap<String, String>();
    protected String[] attributes;

    protected TreePane treePane;
}

package jebl.gui.trees.treeviewer_dev.painters;

import jebl.evolution.graphs.Node;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.Tree;
import jebl.gui.trees.treeviewer_dev.TreePane;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D;
import java.util.*;

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

        this.attributeNames = new String[attributeNames.size()];
        attributeNames.toArray(this.attributeNames);

        firePainterSettingsChanged();
    }

    public void setTreePane(TreePane treePane) {
        this.treePane = treePane;
    }

    public Line2D getNodeBar() {
        return nodeBar;
    }

    public Rectangle2D calibrate(Graphics2D g2, Node node) {
        RootedTree tree = treePane.getTree();

        double height = tree.getHeight(node);
        double upper = height;
        double lower = height;

        boolean hasBar = false;

        Object value = node.getAttribute(lowerAttributeName);
        if (value != null ) {
            if (value instanceof Number) {
                lower = ((Number)value).doubleValue();
            } else {
                lower = Double.parseDouble(value.toString());
            }
            hasBar = true;
        } else {
            // todo - warn the user somehow?
        }

        value = node.getAttribute(upperAttributeName);
        if (value != null ) {
            if (value instanceof Number) {
                upper = ((Number)value).doubleValue();
            } else {
                upper = Double.parseDouble(value.toString());
            }
            hasBar = true;
        } else {
            // todo - warn the user somehow?
        }

        nodeBar = null;

        if (hasBar) {
            Line2D barPath = treePane.getTreeLayout().getNodeBarPath(node);
            double x1 = barPath.getX1();
            double y1 = barPath.getY1();
            double x2 = barPath.getX2();
            double y2 = barPath.getY2();

            double dx = x2 - x1;
            double dy = y2 - y1;

            nodeBar = new Line2D.Double(
                    x1 - (dx * (upper - height)), y1 - (dy * (upper - height)),
                    x1 + (dx * (height - lower)), y1 + (dy * (height - lower)));

        }

        if (nodeBar == null) {
            return new Rectangle2D.Double(0,0,0,0);
        }

        return nodeBar.getBounds2D();
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
     * The bounds define the shape of the nodeBar so just draw it
     * @param g2
     * @param node
     * @param justification
     * @param barShape
     */
    public void paint(Graphics2D g2, Node node, Justification justification, Shape barShape) {
        if (barShape != null) {
            g2.setPaint(getForeground());
            g2.setStroke(getStroke());

            g2.draw(barShape);
        }

    }

    /**
     * The bounds define the shape of the nodeBar so just draw it
     * @param g2
     * @param node
     * @param justification
     * @param bounds
     */
    public void paint(Graphics2D g2, Node node, Justification justification, Rectangle2D bounds) {
        throw new UnsupportedOperationException("This version of paint is not used in NodeBarPainter");
    }

    public String[] getAttributeNames() {
        return attributeNames;
    }

    public String getLowerAttributeName() {
        return lowerAttributeName;
    }

    public String getUpperAttributeName() {
        return upperAttributeName;
    }

    public void setLowerAttributeName(String attributeName) {
        this.lowerAttributeName = attributeName;
        firePainterChanged();
    }

    public void setUpperAttributeName(String attributeName) {
        this.upperAttributeName = attributeName;
        firePainterChanged();
    }

    private double preferredWidth;
    private double preferredHeight;

    private String lowerAttributeName = null;
    private String upperAttributeName = null;
    private String[] attributeNames;

    private TreePane treePane;

    private Line2D nodeBar = null;
}

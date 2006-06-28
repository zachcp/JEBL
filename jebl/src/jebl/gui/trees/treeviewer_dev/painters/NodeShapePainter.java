package jebl.gui.trees.treeviewer_dev.painters;

import jebl.evolution.graphs.Node;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.Tree;
import jebl.gui.trees.treeviewer_dev.TreePane;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;
import java.util.*;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class NodeShapePainter extends NodePainter {

	public static final String AREA_ATTRIBUTE = "area";
	public static final String RADIUS_ATTRIBUTE = "radius";

	public static final String WIDTH_ATTRIBUTE = "width";
	public static final String HEIGHT_ATTRIBUTE = "height";

    public static final String LOWER_ATTRIBUTE = "lower";
    public static final String UPPER_ATTRIBUTE = "upper";

	public enum NodeShape {
	    CIRCLE("Circle"),
	    RECTANGLE("Rectangle");

	    NodeShape(String name) {
	        this.name = name;
	    }

	    public String getName() {
	        return name;
	    }

	    public String toString() {
	        return name;
	    }

	    private final String name;
	}

    public NodeShapePainter() {

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

        double height = tree.getHeight(item);
        double upper = height;
        double lower = height;

        Object value = item.getAttribute(displayAttributes.get(LOWER_ATTRIBUTE));
        if (value != null ) {
            if (value instanceof Number) {
                lower = ((Number)value).doubleValue();
            } else {
                lower = Double.parseDouble(value.toString());
            }
        } else {
            // todo - warn the user somehow?
        }

        value = item.getAttribute(displayAttributes.get(UPPER_ATTRIBUTE));
        if (value != null ) {
            if (value instanceof Number) {
                upper = ((Number)value).doubleValue();
            } else {
                upper = Double.parseDouble(value.toString());
            }
        } else {
            // todo - warn the user somehow?
        }

        Rectangle2D rect = treePane.getTreeLayout().getHeightArea(lower, upper).getBounds2D();

        preferredWidth = rect.getWidth();
        preferredHeight = 0.2;

        xOffset = height - upper;
        yOffset = -0.1;
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

    public Rectangle2D getBounds(Point2D nodePoint) {
        return new Rectangle2D.Double(nodePoint.getX() + xOffset, nodePoint.getY() + yOffset, preferredWidth, preferredHeight);
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

    public void setDisplayAttribute(String display, String attribute) {
        displayAttributes.put(display, attribute);
        firePainterChanged();
    }

    public void setDisplayValues(String display, double value) {
        displayValues.put(display, new Double(value));
        firePainterChanged();
    }

    private double preferredWidth;
    private double preferredHeight;

    private double xOffset, yOffset;

    protected Map<String, String> displayAttributes = new HashMap<String, String>();
    protected Map<String, Number> displayValues = new HashMap<String, Number>();
    protected String[] attributes;

    protected TreePane treePane;
}

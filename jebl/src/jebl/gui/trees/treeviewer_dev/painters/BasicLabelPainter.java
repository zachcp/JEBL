package jebl.gui.trees.treeviewer_dev.painters;

import jebl.evolution.graphs.Node;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.Tree;
import jebl.gui.trees.treeviewer_dev.TreePane;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;

/**
 * A simple implementation of LabelPainter that can be used to display
 * tip, node or branch labels. It can display, taxon names, branch lengths,
 * node heights or other attributeNames of nodes.
 *
 * @author Andrew Rambaut
 * @version $Id$
 */
public class BasicLabelPainter extends LabelPainter<Node> {

    public static final String TAXON_NAMES = "Taxon Names";
    public static final String NODE_HEIGHTS = "Node Heights";
    public static final String BRANCH_LENGTHS = "Branch Lengths";

    public enum PainterIntent {
        NODE,
        BRANCH,
        TIP
    };

    public BasicLabelPainter(PainterIntent intent) {
        this.intent = intent;

        setupAttributes(null);

        if (this.displayAttribute == null) {
            this.displayAttribute = attributes[0];
        } else {
            this.displayAttribute = "";
        }

    }

    public void setupAttributes(Tree tree) {
        List<String> attributeNames = new ArrayList<String>();
        switch( intent ) {
            case TIP: {
                attributeNames.add(TAXON_NAMES);
                attributeNames.add(NODE_HEIGHTS);
                attributeNames.add(BRANCH_LENGTHS);
                break;
            }
            case NODE: {
                attributeNames.add(NODE_HEIGHTS);
                attributeNames.add(BRANCH_LENGTHS);
                break;
            }
            case BRANCH: {
                attributeNames.add(BRANCH_LENGTHS);
                attributeNames.add(NODE_HEIGHTS);
                break;
            }
        }

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

    protected String getLabel(Tree tree, Node node) {
        if (displayAttribute.equalsIgnoreCase(TAXON_NAMES)) {
            return tree.getTaxon(node).getName();
        }

        if ( tree instanceof RootedTree) {
            final RootedTree rtree = (RootedTree) tree;

            if (displayAttribute.equalsIgnoreCase(NODE_HEIGHTS) ) {
                return getNumberFormat().format(rtree.getHeight(node));
            } else if (displayAttribute.equalsIgnoreCase(BRANCH_LENGTHS) ) {
                return getNumberFormat().format(rtree.getLength(node));
            }
        }

        Object value = node.getAttribute(displayAttribute);
        if (value != null) {
            return value.toString();
        }
        return null;
    }

    public Rectangle2D calibrate(Graphics2D g2, Node item) {
        Tree tree = treePane.getTree();

        final Font oldFont = g2.getFont();
        g2.setFont(getFont());

        FontMetrics fm = g2.getFontMetrics();
        preferredHeight = fm.getHeight();
        preferredWidth = 0;

        String label = getLabel(tree, item);
        if (label != null) {
            Rectangle2D rect = fm.getStringBounds(label, g2);
            preferredWidth = rect.getWidth();
        }

        yOffset = (float)fm.getAscent();

        g2.setFont(oldFont);

        return new Rectangle2D.Double(0.0, 0.0, preferredWidth, preferredHeight);
    }

    public double getPreferredWidth() {
        return preferredWidth;
    }

    public double getPreferredHeight() {
        return preferredHeight;
    }

    public double getHeightBound() {
        return preferredHeight + yOffset;
    }

    public void paint(Graphics2D g2, Node item, Justification justification, Rectangle2D bounds) {
        Tree tree = treePane.getTree();

        Font oldFont = g2.getFont();

        if (getBackground() != null) {
            g2.setPaint(getBackground());
            g2.fill(bounds);
        }

        if (getBorderPaint() != null && getBorderStroke() != null) {
            g2.setPaint(getBorderPaint());
            g2.setStroke(getBorderStroke());
            g2.draw(bounds);
        }

        g2.setPaint(getForeground());
        g2.setFont(getFont());

        String label = getLabel(tree, item);
        if (label != null) {

            Rectangle2D rect = g2.getFontMetrics().getStringBounds(label, g2);

            float xOffset;
            float y = yOffset + (float) bounds.getY();
            switch (justification) {
                case CENTER:
                    xOffset = (float)(-rect.getWidth()/2.0);
                    y = yOffset + (float) rect.getY();
                    //xOffset = (float) (bounds.getX() + (bounds.getWidth() - rect.getWidth()) / 2.0);
                    break;
                case FLUSH:
                case LEFT:
                    xOffset = (float) bounds.getX();
                    break;
                case RIGHT:
                    xOffset = (float) (bounds.getX() + bounds.getWidth() - rect.getWidth());
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized alignment enum option");
            }

            g2.drawString(label, xOffset, y);
        }

        g2.setFont(oldFont);
    }

    public String[] getAttributes() {
        return attributes;
    }

    public void setDisplayAttribute(String displayAttribute) {
        this.displayAttribute = displayAttribute;
        firePainterChanged();
    }

    private PainterIntent intent;

    private double preferredWidth;
    private double preferredHeight;
    private float yOffset;

    protected String displayAttribute;
    protected String[] attributes;

    protected TreePane treePane;
}

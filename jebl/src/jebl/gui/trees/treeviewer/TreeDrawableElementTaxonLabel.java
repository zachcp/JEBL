package jebl.gui.trees.treeviewer;

import jebl.evolution.graphs.Node;
import jebl.evolution.trees.Tree;
import jebl.evolution.trees.Utils;
import jebl.gui.trees.treeviewer.painters.BasicLabelPainter;
import jebl.gui.trees.treeviewer.painters.Painter;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

/**
 * @author Joseph Heled
 * @version $Id$
 *          <p/>
 *          Created by IntelliJ IDEA.
 *          User: joseph
 *          Date: 19/12/2006
 *          Time: 12:38:58
 */
public class TreeDrawableElementTaxonLabel extends TreeDrawableElementLabel {

    private Rectangle2D defaultBounds;
    private Rectangle2D minBounds = null;
    private Node nodeWithLongestTaxon;
    private BasicLabelPainter painter;
    int defaultSize;
    private int curSize;
    private Painter.Justification taxonLabelJustification;
    AffineTransform save;
    // debug
    Tree tree;

    TreeDrawableElementTaxonLabel(Tree tree, Node node, Painter.Justification taxonLabelJustification,
                                  Rectangle2D labelBounds, AffineTransform transform, int priority,
                                  Node nodeWithLongestTaxon, BasicLabelPainter painter) {
        super(node, labelBounds, transform, priority);

        defaultBounds = labelBounds;
        this.nodeWithLongestTaxon = nodeWithLongestTaxon != null ? nodeWithLongestTaxon : node;
        //this.node = node;
        this.taxonLabelJustification = taxonLabelJustification;
        this.painter = painter;
        curSize = defaultSize = (int)painter.getFontSize();

        save = new AffineTransform(transform);

        this.tree = tree;
    }

    public void setSize(int size, Graphics2D g2) {
        if( curSize != size ) {
            // System.out.println("Set size of " + getDebugName() + " to " + size);
            Rectangle2D newBounds;
            if( size == getMaxSize() ) {
                newBounds = defaultBounds;
            } else if( size == getMinSize() && minBounds != null ) {
                newBounds = minBounds;
            } else {
                // set it up
                // do it more effciently, share between all
                float s = painter.getFontSize();
                painter.setFontSize(size, false);
                painter.calibrate(g2);
                newBounds = new Rectangle2D.Double(0.0, 0.0, painter.getWidth(g2, nodeWithLongestTaxon), painter.getPreferredHeight());
                if( size == getMinSize() ) {
                    minBounds = newBounds;
                }
                painter.setFontSize(s, false);
                painter.calibrate(g2);
            }        

            final double dx = newBounds.getWidth() - bounds.getWidth();
            final double dy = newBounds.getHeight() - bounds.getHeight();

            bounds = newBounds;
            if( taxonLabelJustification != Painter.Justification.CENTER ) {
                transform.translate(taxonLabelJustification == Painter.Justification.RIGHT ? -dx : 0 , -dy/2);
            } else {
                transform.translate(-dx/2, -dy/2);
            }
            curSize = size;
        }
    }

    public String getDebugName() {
       if( tree.isExternal(node) ) return  tree.getTaxon(node).getName();
       return Utils.DEBUGsubTreeRep(Utils.rootTheTree(tree), node);
    }

    public int getCurrentSize() {
        return curSize;
    }

    protected void drawIt(Graphics2D g2) {
        AffineTransform oldTransform = g2.getTransform();

        g2.transform(transform);
        float s = painter.getFontSize();
        if( painter.setFontSize(curSize, false) ) {
            painter.calibrate(g2);
        }
        painter.paint(g2, node, taxonLabelJustification, bounds);

        if( painter.setFontSize(s, false) ) {
            painter.calibrate(g2);
        }
        g2.setTransform(oldTransform);
    }

    public int getMinSize() {
        return Math.min((int)painter.getFontMinSize(), defaultSize);
    }

    public int getMaxSize() {
        return defaultSize;
    }
}
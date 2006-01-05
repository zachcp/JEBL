package jebl.gui.trees.treeviewer.treelayouts;

import jebl.evolution.graphs.Node;
import jebl.evolution.trees.Tree;
import jebl.gui.trees.treeviewer.controlpanels.ControlsProvider;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public interface TreeLayout extends ControlsProvider {

    enum AxisType {
        CONTINUOUS,
        DISCRETE
    };

    /**
     * Set the tree for the layout0
     * @param tree
     */
    void setTree(Tree tree);

    /**
     * Add a listener for this layout
     * @param listener
     */
    void addTreeLayoutListener(TreeLayoutListener listener);

    /**
     * Remove a listener from this layout
     * @param listener
     */
    void removeTreeLayoutListener(TreeLayoutListener listener);

    /**
     * Force the layout to re-layout all its components
     */
    void invalidate();

    /**
     * Return whether the x axis is continuous or discrete
     * @return
     */
    AxisType getXAxisType();

    /**
     * Return whether the y axis is continuous or discrete
     * @return
     */
    AxisType getYAxisType();


    /**
     * Return whether the two axis scales should be maintained
     * relative to each other
     * @return
     */
    boolean maintainAspectRatio();

    /**
     * Return the height (from the youngest tip) for the given
     * 2d point. Some layouts won't be able to produce this and
     * may throw an UnsupportedOperationException.
     * @param point
     * @return
     */
    double getHeightOfPoint(Point2D point);

    /**
     * Return a line that defines a particular height. Some layouts
     * won't be able to produce this and may throw an UnsupportedOperationException.
     * @param height
     * @return the line
     */
    Line2D getHeightLine(double height);

    /**
     * Return a shape that defines a particular height interval. Some layouts
     * won't be able to produce this and may throw an UnsupportedOperationException.
     * @param height
     * @return the area
     */
    Shape getHeightArea(double height1, double height2);

    /**
     * Return the point in 2d space of the given node
     * @param node
     * @return the point
     */
    Point2D getNodePoint(Node node);

    /**
     * Return the shape that represents the given branch
     * @param node
     * @return the branch shape
     */
    Shape getBranchPath(Node node);

    Line2D getTaxonLabelPath(Node node);
    Line2D getBranchLabelPath(Node node);
    Line2D getNodeLabelPath(Node node);

	Shape getCalloutPath(Node node);
}

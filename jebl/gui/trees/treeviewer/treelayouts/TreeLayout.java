package jebl.gui.trees.treeviewer.treelayouts;

import jebl.evolution.graphs.Node;
import jebl.evolution.trees.Tree;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public interface TreeLayout {

    enum AxisType {
        CONTINUOUS,
        DISCRETE
    };

    void setTree(Tree tree);

    void addTreeLayoutListener(TreeLayoutListener listener);
    void removeTreeLayoutListener(TreeLayoutListener listener);

    void invalidate();

    AxisType getXAxisType();
    AxisType getYAxisType();
    boolean maintainAspectRatio();

    Point2D getNodePoint(Node node);
    Shape getBranchPath(Node node);

    Line2D getTaxonLabelPath(Node node);
    Line2D getBranchLabelPath(Node node);
    Line2D getNodeLabelPath(Node node);

	Shape getCalloutPath(Node node);

    JPanel getControlPanel();
}

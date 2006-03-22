package jebl.gui.trees.treeviewer.treelayouts;

import jebl.evolution.graphs.Graph;
import jebl.evolution.graphs.Node;
import org.virion.jam.controlpanels.ControlPalette;
import org.virion.jam.controlpanels.Controls;
import org.virion.jam.controlpanels.ControlsSettings;
import org.virion.jam.panels.OptionsPanel;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class RadialTreeLayout extends AbstractTreeLayout {

    public AxisType getXAxisType() {
        return AxisType.CONTINUOUS;
    }

    public AxisType getYAxisType() {
        return AxisType.CONTINUOUS;
    }

    public boolean maintainAspectRatio() {
        return true;
    }

    public double getHeightOfPoint(Point2D point) {
        throw new UnsupportedOperationException("Method getHeightOfPoint() is not supported in this TreeLayout");
    }

    public Line2D getHeightLine(double height) {
        throw new UnsupportedOperationException("Method getHeightOfPoint() is not supported in this TreeLayout");
    }

    public Shape getHeightArea(double height1, double height2) {
        throw new UnsupportedOperationException("Method getHeightOfPoint() is not supported in this TreeLayout");
    }

    public void setControlPalette(ControlPalette controlPalette) {
        // do nothing...
    }

    public List<Controls> getControls(boolean detachPrimaryCheckbox) {

        List<Controls> controlsList = new ArrayList<Controls>();

        if (controls == null) {
            OptionsPanel optionsPanel = new OptionsPanel();

            controls = new Controls("Layout", optionsPanel, true);
        }

        controlsList.add(controls);

        return controlsList;
    }

    public void setSettings(ControlsSettings settings) {
    }

    public void getSettings(ControlsSettings settings) {
    }

    private Controls controls = null;

    protected void validate() {
        nodePoints.clear();
        branchPaths.clear();
        taxonLabelPaths.clear();

        try {
            Node root = this.tree.getRootNode();

            constructNode(root, 0.0, Math.PI * 2, 0.0, 0.0, 0.0);

            Line2D branchPath = new Line2D.Double(0.0, 0.0, 0.0, 0.0);

            // add the branchPath to the map of branch paths
            branchPaths.put(root, branchPath);

        } catch (Graph.NoEdgeException e) {
            e.printStackTrace();
        }
    }

    private Point2D constructNode(Node node, double angleStart, double angleFinish, double xPosition, double yPosition, double length) throws Graph.NoEdgeException {

        double branchAngle = (angleStart + angleFinish) / 2.0;

        Point2D nodePoint = new Point2D.Double(xPosition + (length * Math.cos(branchAngle)), yPosition + (length * Math.sin(branchAngle)));

        if (!tree.isExternal(node)) {

            List<Node> children = tree.getChildren(node);
            int[] leafCounts = new int[children.size()];
            int sumLeafCount = 0;

            int i = 0;
            for (Node child : children) {
                leafCounts[i] = jebl.evolution.trees.Utils.getExternalNodeCount(tree, child);
                sumLeafCount += leafCounts[i];
                i++;
            }

            double span = (angleFinish - angleStart);
            double a1 = angleStart;
            double a2 = angleStart;

            i = 0;
            for (Node child : children) {

                double childLength = tree.getLength(child);
                a1 = a2;
                a2 = a1 + (span * leafCounts[i] / sumLeafCount);

                Point2D childPoint = constructNode(child, a1, a2, nodePoint.getX(), nodePoint.getY(), childLength);

                Line2D branchPath = new Line2D.Double(nodePoint.getX(), nodePoint.getY(), childPoint.getX(), childPoint.getY());

                // add the branchPath to the map of branch paths
                branchPaths.put(child, branchPath);

                Point2D branchLabelPoint1 = new Point2D.Double(xPosition + (length * 0.5 * Math.cos(branchAngle)),
                        yPosition + (length * 0.5 * Math.sin(branchAngle)));
                Point2D branchLabelPoint2 = new Point2D.Double(xPosition + (((length * 0.5) + 1.0) * Math.cos(branchAngle)),
                        yPosition + (((length * 0.5) + 1.0) * Math.sin(branchAngle)));
                Line2D branchLabelPath = new Line2D.Double(branchLabelPoint1, branchLabelPoint2);

                branchLabelPaths.put(child, branchLabelPath);
                i++;
            }

            Point2D nodeLabelPoint = new Point2D.Double(xPosition + ((length + 1.0) * Math.cos(branchAngle)),
                    yPosition + ((length + 1.0) * Math.sin(branchAngle)));

            Line2D nodeLabelPath = new Line2D.Double(nodePoint, nodeLabelPoint);
            nodeLabelPaths.put(node, nodeLabelPath);
        } else {

            Point2D taxonPoint = new Point2D.Double(xPosition + ((length + 1.0) * Math.cos(branchAngle)),
                    yPosition + ((length + 1.0) * Math.sin(branchAngle)));

            Line2D taxonLabelPath = new Line2D.Double(nodePoint, taxonPoint);
            taxonLabelPaths.put(node, taxonLabelPath);
        }

        // add the node point to the map of node points
        nodePoints.put(node, nodePoint);

        return nodePoint;
    }


}
package jebl.gui.trees.treeviewer.treelayouts;

import jebl.evolution.graphs.Graph;
import jebl.evolution.graphs.Node;
import org.virion.jam.controlpanels.ControlPalette;
import org.virion.jam.controlpanels.Controls;
import org.virion.jam.controlpanels.ControlsSettings;
import org.virion.jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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

    // radians
    private double rootAngle = 0.0;
    public void setRootAngle(double angleInDeg) {
        this.rootAngle = angleInDeg* (Math.PI/180.0) ;
        invalidate();
    }


    public List<Controls> getControls(boolean detachPrimaryCheckbox) {

        List<Controls> controlsList = new ArrayList<Controls>();

        if (controls == null) {
            OptionsPanel optionsPanel = new OptionsPanel();

            final JSlider slider1 = new JSlider(SwingConstants.HORIZONTAL, 0, 3600, 0);
            slider1.setValue((int) (rootAngle * 10));
            slider1.setPaintTicks(true);
            slider1.setPaintLabels(true);

            slider1.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    double value = (slider1.getValue() / 10.0);
                    setRootAngle(value % 360);
                }
            });
            optionsPanel.addComponentWithLabel("Root Angle:", slider1, true);

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
            final Node root = tree.getRootNode();

            constructNode(root, rootAngle, rootAngle + Math.PI * 2, 0.0, 0.0, 0.0);

            if( !tree.conceptuallyUnrooted() ) {
                Line2D branchPath = new Line2D.Double(0.0, 0.0, 0.0, 0.0);

                // add the branchPath to the map of branch paths
                branchPaths.put(root, branchPath);
            }

        } catch (Graph.NoEdgeException e) {
            e.printStackTrace();
        }
    }



    private Point2D constructNode(Node node, double angleStart, double angleFinish, double xPosition,
                                  double yPosition, double length) throws Graph.NoEdgeException {

        final double branchAngle = (angleStart + angleFinish) / 2.0;

        final double directionX = Math.cos(branchAngle);
        final double directionY = Math.sin(branchAngle);
        Point2D nodePoint = new Point2D.Double(xPosition + (length * directionX), yPosition + (length * directionY));

        // System.out.println("Node: " + Utils.DEBUGsubTreeRep(tree, node) + " at " + nodePoint);

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

            final double span = (angleFinish - angleStart);
            double a2 = angleStart;

            i = 0;
            for (Node child : children) {

                final double childLength = tree.getLength(child);
                double a1 = a2;
                a2 = a1 + (span * leafCounts[i] / sumLeafCount);

                Point2D childPoint = constructNode(child, a1, a2, nodePoint.getX(), nodePoint.getY(), childLength);

                Line2D branchPath =
                        new Line2D.Double(nodePoint.getX(), nodePoint.getY(), childPoint.getX(), childPoint.getY());

                // add the branchPath to the map of branch paths
                branchPaths.put(child, branchPath);

                branchLabelPaths.put(child, (Line2D)branchPath.clone());
                i++;
            }

            Point2D nodeLabelPoint = new Point2D.Double(xPosition + ((length + 1.0) * directionX),
                    yPosition + ((length + 1.0) * directionY));

            Line2D nodeLabelPath = new Line2D.Double(nodePoint, nodeLabelPoint);
            nodeLabelPaths.put(node, nodeLabelPath);
        } else {

            Point2D taxonPoint = new Point2D.Double(xPosition + ((length + 1.0) * directionX),
                    yPosition + ((length + 1.0) * directionY));

            Line2D taxonLabelPath = new Line2D.Double(nodePoint, taxonPoint);
            taxonLabelPaths.put(node, taxonLabelPath);
        }

        // add the node point to the map of node points
        nodePoints.put(node, nodePoint);

        return nodePoint;
    }
}
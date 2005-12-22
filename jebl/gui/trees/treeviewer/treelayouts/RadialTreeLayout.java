package jebl.gui.trees.treeviewer.treelayouts;

import jebl.evolution.graphs.Graph;
import jebl.evolution.graphs.Node;

import javax.swing.*;
import java.awt.geom.*;
import java.util.List;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
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

    public JPanel getControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        return panel;
    }

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

                i++;
            }

        } else {

            Point2D taxonPoint = new Point2D.Double(xPosition + ((length + 1.0) * Math.cos(branchAngle)), yPosition + ((length + 1.0) * Math.sin(branchAngle)));

            Line2D taxonLabelPath = new Line2D.Double(nodePoint, taxonPoint);
            taxonLabelPaths.put(node, taxonLabelPath);
        }

        // add the node point to the map of node points
        nodePoints.put(node, nodePoint);

        return nodePoint;
    }


}

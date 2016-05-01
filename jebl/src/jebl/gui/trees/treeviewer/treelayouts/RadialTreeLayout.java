package jebl.gui.trees.treeviewer.treelayouts;

import jebl.evolution.graphs.Graph;
import jebl.evolution.graphs.Node;
import jebl.evolution.trees.Tree;
import org.virion.jam.controlpanels.ControlPalette;
import org.virion.jam.controlpanels.Controls;
import org.virion.jam.controlpanels.ControlsSettings;
import org.virion.jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * @author Andrew Rambaut
 */
public class RadialTreeLayout extends AbstractTreeLayout {

    private static Preferences getPrefs() {
        return Preferences.userNodeForPackage(RadialTreeLayout.class);
    }

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

    public boolean alignTaxa() {
        return false;
    }

    private double distToNode( Point2D nodeLoc, Node to, AffineTransform transform) {
        final Point2D parentLoc = getNodePoint(to);
        final double dx = (parentLoc.getX() - nodeLoc.getX()) * transform.getScaleX();
        final double dy = (parentLoc.getY() - nodeLoc.getY()) * transform.getScaleY();
        return dx*dx + dy *dy;
    }

    public int getNodeMarkerRadiusUpperLimit(Node node, AffineTransform transform) {
        final Node parent = tree.getParent(node);
        double d = Double.MAX_VALUE;
        final Point2D nodeLoc = getNodePoint(node);
        if( parent != null)  {
            d = distToNode(nodeLoc, parent, transform);
        }
        final List<Node> nodes = tree.getChildren(node);
        for( Node n : nodes ) {
            final double dc = distToNode(nodeLoc, n, transform);

            d = Math.min(dc, d);  assert d >= 0;
        }

        return (int)(Math.sqrt(d) * 0.33);
    }

    public boolean smallSubTree(Node node, AffineTransform transform) {
        final List<Node> children = tree.getChildren(node);
        if( children.size() < 2 ) return false;

        int method = 0;
        if( method == 0 )  {
            Point2D[] tc = new Point2D.Double[2];
            int[] indices = {0, children.size()-1};
            for(int nc = 0; nc < 2; ++nc) {
                tc[nc] = new Point2D.Double();
                transform.transform(nodePoints.get(children.get(indices[nc])), tc[nc]);
            }

            return tc[0].distanceSq(tc[1]) < 5 * 5;
        }

        if( method == 1 ) {
            Point2D[] tc = new Point2D.Double[children.size()];
            for(int nc = 0; nc < children.size(); ++nc) {
                tc[nc] = new Point2D.Double();
                transform.transform(nodePoints.get(children.get(nc)), tc[nc]);
            }

            for(int nc = 0; nc < children.size(); ++nc) {
                int[] dirs = {-1, +1};
                for(int d : dirs ) {
                    int k = d + nc;
                    if( 0 <= k && k < children.size() ) {
                        if( tc[nc].distanceSq(tc[k])  < 5*5 ) {
                            return true;
                        }
                    }
                }
            }
        }

//        int nShortest = 0;
//        double shortest = tree.getLength(children.get(nShortest));
//        for(int nc = 1; nc < children.size(); ++nc) {
//            final double length = tree.getLength(children.get(nc));
//            if( length < shortest ) {
//                shortest = length;
//                nShortest = nc;
//            }
//        }
//
//        Point2D origin = new Point2D.Double();
//        transform.transform(new Point2D.Double(0, 0), origin);
//
//        final Point2D location = nodePoints.get(node);
//        final Point2D slocation = nodePoints.get(children.get(nShortest));
//        int[] dirs = {-1, +1};
//        for(int d : dirs ) {
//            int k = d + nShortest;
//            if( 0 <= k && k < children.size() ) {
//                final Point2D d1 = nodePoints.get(children.get(k));
//                final Line2D l = new Line2D.Double(location, d1);
//                final double v = l.ptLineDist(slocation);
//
//                Point2D dst = new Point2D.Double();
//                transform.transform(new Point2D.Double(0, v), dst);
//
//                if( dst.distanceSq(origin) < 5*5 ) {
//                    return true;
//                }
////                final double x = dst.getX();
////                final double y = dst.getY();
////                if( x*x + y*y < 5*5 ) {
////                    return true;
////                }
//            }
//        }


        return false;
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

    @Override
     public void setTree(Tree tree) {
        super.setTree(tree);
        if(tree != null) {
            setRootAngle(getPrefs().getDouble("radial_root_angle", 180.0));
        }
    }

    public List<Controls> getControls(boolean detachPrimaryCheckbox) {

        List<Controls> controlsList = new ArrayList<Controls>();

        if (controls == null) {
            OptionsPanel optionsPanel = new OptionsPanel();

            final JSlider slider1 = new JSlider(SwingConstants.HORIZONTAL, 0, 360, 0);
            slider1.setValue((int) (rootAngle*(180.0/Math.PI))+180);
            slider1.setPaintTicks(true);
            slider1.setPaintLabels(true);

            slider1.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    int value = slider1.getValue() - 180;
                    setRootAngle(value);
                    getPrefs().putDouble("radial_root_angle", value);
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



    private Point2D constructNode(final Node node,
                                  final double angleStart, final double angleFinish,
                                  final double xPosition, final double yPosition, final double length)
            throws Graph.NoEdgeException {

        final double branchAngle = (angleStart + angleFinish) / 2.0;

        final double directionX = Math.cos(branchAngle);
        final double directionY = Math.sin(branchAngle);

        final double x = xPosition + (length * directionX);
        final double y = yPosition + (length * directionY);
        final Point2D nodePoint = new Point2D.Double(x, y);

        // System.out.println("Node: " + Utils.DEBUGsubTreeRep(tree, node) + " at " + nodePoint);

        final double x2 = xPosition + ((length + 1.0) * directionX);
        final double y2 = yPosition + ((length + 1.0) * directionY);

        if (!tree.isExternal(node)) {

            List<Node> children = tree.getChildren(node);
            int[] leafCounts = new int[children.size()];
            int totalLeafs = 0;

            for(int i = 0; i < children.size(); ++i) {
                final Node child  = children.get(i);
                leafCounts[i] = jebl.evolution.trees.Utils.getExternalNodeCount(tree, child);
                totalLeafs += leafCounts[i];
            }

            final double span = angleFinish - angleStart;
            double a2 = angleStart;

            for(int i = 0; i < children.size(); ++i) {
                final Node child = children.get(i);

                final double childLength = tree.getLength(child);
                double a1 = a2;
                a2 = a1 + (span * leafCounts[i] / totalLeafs);

                final Point2D childPoint = constructNode(child, a1, a2, x, y, childLength);

                double toY = childPoint.getY();
                double toX = childPoint.getX();
                if( childLength == 0 ) {
                   final double a = (a1 + a2) / 2.0;
                    double epsilon = 1e-9;
                    final double dx = Math.cos(a);
                    final double dy = Math.sin(a);
                    toX = x + epsilon * dx;
                    toY = y + epsilon * dy;
                }

                Line2D branchPath = new Line2D.Double(x, y, toX, toY);

                // add the branchPath to the map of branch paths
                branchPaths.put(child, branchPath);

                branchLabelPaths.put(child, (Line2D)branchPath.clone());
            }

            final Point2D nodeLabelPoint = new Point2D.Double(x2, y2);

            final Line2D nodeLabelPath = new Line2D.Double(nodePoint, nodeLabelPoint);
            nodeLabelPaths.put(node, nodeLabelPath);
        } else {

            Point2D taxonPoint = new Point2D.Double(x2, y2);

            final Line2D taxonLabelPath = new Line2D.Double(nodePoint, taxonPoint);
            taxonLabelPaths.put(node, taxonLabelPath);
        }

        // add the node point to the map of node points
        nodePoints.put(node, nodePoint);

        return nodePoint;
    }
}
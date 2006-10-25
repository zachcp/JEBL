package jebl.gui.trees.treeviewer_dev.treelayouts;

import jebl.evolution.graphs.Node;

import java.awt.*;
import java.awt.geom.*;
import java.util.List;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class PolarTreeLayout extends AbstractTreeLayout {

    public enum TipLabelPosition {
        FLUSH,
        RADIAL,
        HORIZONTAL
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

    public Shape getHeightLine(double height) {
        return new Ellipse2D.Double(0.0, 0.0, height * 2.0, height * 2.0);
    }

    public Shape getHeightArea(double height1, double height2) {
        Area area1 = new Area(new Ellipse2D.Double(0.0, 0.0, height2 * 2.0, height2 * 2.0));
        Area area2 = new Area(new Ellipse2D.Double(0.0, 0.0, height1 * 2.0, height1 * 2.0));
        area1.subtract(area2);
        return area1;
    }


    public double getRootAngle() {
        return rootAngle;
    }

    public double getAngularRange() {
        return angularRange;
    }

    public boolean isShowingRootBranch() {
        return showingRootBranch;
    }

    public double getRootLength() {
        return rootLength;
    }

    public TipLabelPosition getTipLabelPosition() {
        return tipLabelPosition;
    }

    public void setRootAngle(double rootAngle) {
        this.rootAngle = rootAngle;
        invalidate();
    }

    public void setAngularRange(double angularRange) {
        this.angularRange = angularRange;
        invalidate();
    }

    public void setShowingRootBranch(boolean showingRootBranch) {
        this.showingRootBranch = showingRootBranch;
        invalidate();
    }

    public void setRootLength(double rootLength) {
        this.rootLength = rootLength;
        invalidate();
    }

    public void setTipLabelPosition(TipLabelPosition tipLabelPosition) {
        this.tipLabelPosition = tipLabelPosition;
        invalidate();
    }

    protected void validate() {
        nodePoints.clear();
        branchPaths.clear();
        collapsedShapes.clear();
        tipLabelPaths.clear();
        nodeLabelPaths.clear();
        nodeBarPaths.clear();
        calloutPaths.clear();

        Node root = this.tree.getRootNode();
        double rl = (rootLength * this.tree.getHeight(root)) * 10.0;

        maxXPosition = 0.0;
        getMaxXPosition(root, rl);

        yPosition = 0.0;
        yIncrement = 1.0 / tree.getExternalNodes().size();

        final Point2D rootPoint = constructNode(root, rl);

        if (showingRootBranch) {
            // construct a root branch line
            final double y = rootPoint.getY();
            Line2D line = new Line2D.Double(transform(0.0, y), transform(rootPoint.getX(), y));

            // add the line to the map of branch paths
            branchPaths.put(root, line);
        }
    }

    private Point2D constructNode(Node node, double xPosition) {

        Point2D nodePoint;

        if (!tree.isExternal(node)) {

            if (collapseAttributeName != null && node.getAttribute(collapseAttributeName) != null) {
                nodePoint = constructCollapsedNode(node, xPosition);
            } else {
                double yPos = 0.0;

                List<Node> children = tree.getChildren(node);
                Point2D[] childPoints = new Point2D[children.size()];

                int i = 0;
                for (Node child : children) {

                    final double length = tree.getLength(child);
                    childPoints[i] = constructNode(child, xPosition + length);
                    yPos += childPoints[i].getY();

                    i++;
                }

                // the y-position of the node is the average of the child nodes
                yPos /= children.size();

                nodePoint = new Point2D.Double(xPosition, yPos);
                Point2D transformedNodePoint = transform(nodePoint);

                final double start = getAngle(yPos);

                i = 0;
                for (Node child : children) {

                    GeneralPath branchPath = new GeneralPath();
                    final Point2D transformedChildPoint = transform(childPoints[i]);

                    final Point2D transformedShoulderPoint = transform(
                            nodePoint.getX(), childPoints[i].getY());

                    Object[] colouring = null;
                    if (branchColouringAttribute != null) {
                        colouring = (Object[])child.getAttribute(branchColouringAttribute);
                    }
                    if (colouring != null) {
                        // If there is a colouring, then we break the path up into
                        // segments. This should allow use to iterate along the segments
                        // and colour them as we draw them.

                        float nodeHeight = (float) tree.getHeight(node);
                        float childHeight = (float) tree.getHeight(child);

                        double x1 = childPoints[i].getX();
                        double x0 = nodePoint.getX();

                        branchPath.moveTo(
                                (float) transformedChildPoint.getX(),
                                (float) transformedChildPoint.getY());

                        for (int j = 0; j < colouring.length - 1; j+=2) {
                            double height = ((Number)colouring[j+1]).doubleValue();
                            double p = (height - childHeight) / (nodeHeight - childHeight);
                            double x = x1 - ((x1 - x0) * p);
                            final Point2D transformedPoint = transform(x, childPoints[i].getY());
                            branchPath.lineTo(
                                    (float) transformedPoint.getX(),
                                    (float) transformedPoint.getY());
                        }
                        branchPath.lineTo(
                                (float) transformedShoulderPoint.getX(),
                                (float) transformedShoulderPoint.getY());
                    } else {
                        branchPath.moveTo(
                                (float) transformedChildPoint.getX(),
                                (float) transformedChildPoint.getY());

                        branchPath.lineTo(
                                (float) transformedShoulderPoint.getX(),
                                (float) transformedShoulderPoint.getY());
                    }

                    final double finish = getAngle(childPoints[i].getY());

                    Arc2D arc = new Arc2D.Double();
                    arc.setArcByCenter(0.0, 0.0, nodePoint.getX(), finish, start - finish, Arc2D.OPEN);
                    branchPath.append(arc, true);

                    // add the branchPath to the map of branch paths
                    branchPaths.put(child, branchPath);

                    final double x3 = (nodePoint.getX() + childPoints[i].getX()) / 2;

                    Line2D branchLabelPath = new Line2D.Double(
                            transform(x3 - 1.0, childPoints[i].getY()),
                            transform(x3 + 1.0, childPoints[i].getY()));

                    branchLabelPaths.put(child, branchLabelPath);

                    i++;
                }

                Line2D nodeLabelPath = new Line2D.Double(
                        transform(nodePoint.getX(), yPos),
                        transform(nodePoint.getX() + 1.0, yPos));

                nodeLabelPaths.put(node, nodeLabelPath);

                Line2D nodeBarPath = new Line2D.Double(
                        transform(nodePoint.getX(), yPos),
                        transform(nodePoint.getX() - 1.0, yPos));

                nodeBarPaths.put(node, nodeBarPath);

                // add the node point to the map of node points
                nodePoints.put(node, transformedNodePoint);
            }
        } else {

            nodePoint = new Point2D.Double(xPosition, yPosition);
            Point2D transformedNodePoint = transform(nodePoint);

            Line2D tipLabelPath;

            if (tipLabelPosition == TipLabelPosition.FLUSH) {

                tipLabelPath = new Line2D.Double(transformedNodePoint, transform(xPosition + 1.0, yPosition));

            } else if (tipLabelPosition == TipLabelPosition.RADIAL) {

                tipLabelPath = new Line2D.Double(transform(maxXPosition, yPosition),
                        transform(maxXPosition + 1.0, yPosition));

                Line2D calloutPath = new Line2D.Double(transformedNodePoint, transform(maxXPosition, yPosition));

                calloutPaths.put(node, calloutPath);

            } else if (tipLabelPosition == TipLabelPosition.HORIZONTAL) {
                // this option disabled in getControls (JH)
                throw new UnsupportedOperationException("Not implemented yet");
            } else {
                // this is a bug
                throw new IllegalArgumentException("Unrecognized enum value");
            }

            tipLabelPaths.put(node, tipLabelPath);

            yPosition += yIncrement;

            // add the node point to the map of node points
            nodePoints.put(node, transformedNodePoint);
        }

        return nodePoint;
    }

    private Point2D constructCollapsedNode(Node node, double xPosition) {

        Point2D nodePoint;

        Object[] values = (Object[])node.getAttribute(collapseAttributeName);
        int tipCount = (Integer)values[0];
        double tipHeight = (Double)values[1];
        double height = tree.getHeight(node);
        double maxXPos = xPosition + height - tipHeight;

        double minYPos = yPosition;
        yPosition += yIncrement * (tipCount - 1);
        double maxYPos = yPosition;
        yPosition += yIncrement;

        // the y-position of the node is the average of the child nodes
        double yPos = (maxYPos + minYPos) / 2;

        nodePoint = new Point2D.Double(xPosition, yPos);
        Point2D transformedNodePoint0 = transform(nodePoint);
        Point2D transformedNodePoint1 = transform(new Point2D.Double(maxXPos, minYPos));
        Point2D transformedNodePoint2 = transform(new Point2D.Double(maxXPos, maxYPos));

        GeneralPath collapsedShape = new GeneralPath();

        collapsedShape.moveTo((float)transformedNodePoint0.getX(), (float)transformedNodePoint0.getY());
        collapsedShape.lineTo((float)transformedNodePoint1.getX(), (float)transformedNodePoint1.getY());
        final double start = getAngle(maxYPos);
        final double finish = getAngle(minYPos);
        Arc2D arc = new Arc2D.Double();
        arc.setArcByCenter(0.0, 0.0, maxXPos, finish, start - finish, Arc2D.OPEN);
        collapsedShape.append(arc, true);
        collapsedShape.closePath();

        // add the collapsedShape to the map of branch paths
        collapsedShapes.put(node, collapsedShape);

        Line2D nodeLabelPath = new Line2D.Double(
                transform(nodePoint.getX(), yPos),
                transform(nodePoint.getX() + 1.0, yPos));

        nodeLabelPaths.put(node, nodeLabelPath);

        Line2D nodeBarPath = new Line2D.Double(
                transform(nodePoint.getX(), yPos),
                transform(nodePoint.getX() - 1.0, yPos));

        nodeBarPaths.put(node, nodeBarPath);

        if (showingCollapsedTipLabels) {
            constructCollapsedTipLabelPaths(node, maxXPos, new double[] { minYPos });
        }

        // add the node point to the map of node points
        nodePoints.put(node, transformedNodePoint0);

        return nodePoint;
    }

    private void constructCollapsedTipLabelPaths(Node node, double xPosition, double[] yPosition) {

        if (!tree.isExternal(node)) {
            for (Node child :  tree.getChildren(node)) {
                constructCollapsedTipLabelPaths(child, xPosition, yPosition);
            }
        } else {

            Point2D nodePoint = new Point2D.Double(xPosition, yPosition[0]);

            Point2D transformedNodePoint = transform(nodePoint);

            Line2D tipLabelPath;

            if (tipLabelPosition == TipLabelPosition.FLUSH) {

                tipLabelPath = new Line2D.Double(transformedNodePoint, transform(xPosition + 1.0, yPosition[0]));

            } else if (tipLabelPosition == TipLabelPosition.RADIAL) {

                tipLabelPath = new Line2D.Double(transform(maxXPosition, yPosition[0]),
                        transform(maxXPosition + 1.0, yPosition[0]));

                Line2D calloutPath = new Line2D.Double(transformedNodePoint, transform(maxXPosition, yPosition[0]));

                calloutPaths.put(node, calloutPath);

            } else if (tipLabelPosition == TipLabelPosition.HORIZONTAL) {
                // this option disabled in getControls (JH)
                throw new UnsupportedOperationException("Not implemented yet");
            } else {
                // this is a bug
                throw new IllegalArgumentException("Unrecognized enum value");
            }

            tipLabelPaths.put(node, tipLabelPath);

            yPosition[0] += yIncrement;

        }
    }

    private void getMaxXPosition(Node node, double xPosition) {

        if (!tree.isExternal(node)) {

            List<Node> children = tree.getChildren(node);

            for (Node child : children) {
                final double length = tree.getLength(child);
                getMaxXPosition(child, xPosition + length);
            }

        } else {
            if (xPosition > maxXPosition) {
                maxXPosition = xPosition;
            }
        }
    }

    /**
     * Polar transform
     *
     * @param point
     * @return the point in polar space
     */
    private Point2D transform(Point2D point) {
        return transform(point.getX(), point.getY());
    }

    /**
     * Polar transform
     *
     * @param x
     * @param y
     * @return the point in polar space
     */
    private Point2D transform(double x, double y) {
        double r = - Math.toRadians(getAngle(y));
        double tx = x * Math.cos(r);
        double ty = x * Math.sin(r);
        return new Point2D.Double(tx, ty);
    }

    private double getAngle(double y) {
        return rootAngle - ((360.0 - angularRange) * 0.5) - (y * angularRange);
    }

    private double yPosition;
    private double yIncrement;

    private double maxXPosition;

    private double rootAngle = 180.0;
    private double rootLength = 0.01;
    private double angularRange = 360.0;

    private boolean showingRootBranch = true;

    private TipLabelPosition tipLabelPosition = TipLabelPosition.FLUSH;
}

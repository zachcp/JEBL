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

    public Line2D getHeightLine(double height) {
        throw new UnsupportedOperationException("Method getHeightOfPoint() is not supported in this TreeLayout");
    }

    public Shape getHeightArea(double height1, double height2) {
        throw new UnsupportedOperationException("Method getHeightOfPoint() is not supported in this TreeLayout");
    }

	public double getRootAngle() {
		return rootAngle;
	}

	public double getRootLength() {
		return rootLength;
	}

	public double getAngularRange() {
		return angularRange;
	}

	public TipLabelPosition getTipLabelPosition() {
		return tipLabelPosition;
	}

    public void setRootAngle(double rootAngle) {
        this.rootAngle = rootAngle;
        invalidate();
    }

    public void setRootLength(double rootLength) {
        this.rootLength = rootLength;
        invalidate();
    }

    public void setAngularRange(double angularRange) {
        this.angularRange = angularRange;
        invalidate();
    }

    public void setTipLabelPosition(TipLabelPosition tipLabelPosition) {
        this.tipLabelPosition = tipLabelPosition;
        invalidate();
    }

    protected void validate() {
        nodePoints.clear();
        branchPaths.clear();
        tipLabelPaths.clear();
        calloutPaths.clear();

        Node root = this.tree.getRootNode();
        double rl = (rootLength * this.tree.getHeight(root)) * 10.0;

        maxXPosition = 0.0;
        getMaxXPosition(root, rl);

        yPosition = 0.0;
        yIncrement = 1.0 / tree.getExternalNodes().size();

        final Point2D rootPoint = constructNode(root, rl);

        // construct a root branch line
        final double y = rootPoint.getY();
        Line2D line = new Line2D.Double(transform(0.0, y), transform(rootPoint.getX(), y));

        // add the line to the map of branch paths
        branchPaths.put(root, line);
    }

    private Point2D constructNode(Node node, double xPosition) {

        Point2D nodePoint;
        Point2D transformedNodePoint;

        if (!tree.isExternal(node)) {

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
            transformedNodePoint = transform(nodePoint);

            final double start = getAngle(yPos);

            i = 0;
            for (Node child : children) {

                GeneralPath branchPath = new GeneralPath();

                final double childY = childPoints[i].getY();

                final double finish = getAngle(childY);

                Arc2D arc = new Arc2D.Double();
                arc.setArcByCenter(0.0, 0.0, nodePoint.getX(), start, finish - start, Arc2D.OPEN);
                branchPath.append(arc, true);

                final Point2D p = transform(childPoints[i]);
                branchPath.lineTo((float) p.getX(), (float) p.getY());

                // add the branchPath to the map of branch paths
                branchPaths.put(child, branchPath);

                final double x3 = (nodePoint.getX() + childPoints[i].getX()) / 2;

                Line2D branchLabelPath = new Line2D.Double(transform(x3 - 1.0, childY), transform(x3 + 1.0, childY));

                branchLabelPaths.put(child, branchLabelPath);

                i++;
            }

            // Line2D nodeLabelPath = new Line2D.Double(transform(maxXPosition, yPos), transform(maxXPosition + 1.0, yPos));
            Line2D nodeLabelPath = new Line2D.Double(transform(nodePoint.getX(), yPos), transform(nodePoint.getX()+ 1.0, yPos));

            nodeLabelPaths.put(node, nodeLabelPath);
        } else {

            nodePoint = new Point2D.Double(xPosition, yPosition);
            transformedNodePoint = transform(nodePoint);

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
        }

        // add the node point to the map of node points
        nodePoints.put(node, transformedNodePoint);

        return nodePoint;
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

    private TipLabelPosition tipLabelPosition = TipLabelPosition.FLUSH;
}

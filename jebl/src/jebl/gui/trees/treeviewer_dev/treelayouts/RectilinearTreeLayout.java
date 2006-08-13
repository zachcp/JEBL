package jebl.gui.trees.treeviewer_dev.treelayouts;

import jebl.evolution.graphs.Node;

import java.awt.*;
import java.awt.geom.*;
import java.util.List;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class RectilinearTreeLayout extends AbstractTreeLayout {

	public AxisType getXAxisType() {
		return AxisType.CONTINUOUS;
	}

	public AxisType getYAxisType() {
		return AxisType.DISCRETE;
	}

	public boolean isShowingRootBranch() {
		return true;
	}

	public boolean maintainAspectRatio() {
		return false;
	}

	public double getHeightOfPoint(Point2D point) {
		return point.getX();
	}

	public Shape getHeightLine(double height) {
		double x = height;
		double y1 = 0.0;
		double y2 = 1.0;
		return new Line2D.Double(x, y1, x, y2);
	}

	public Shape getHeightArea(double height1, double height2) {
		double x = height1;
		double y = 0.0;
		double w = height2 - height1;
		double h = 1.0;
		return new Rectangle2D.Double(x, y, w, h);
	}

	public boolean isAlignTipLabels() {
		return alignTipLabels;
	}

	public double getRootLength() {
		return rootLength;
	}

	public double getCurvature() {
		return 1.0 - xProportion;
	}

	public void setAlignTipLabels(boolean alignTipLabels) {
		this.alignTipLabels = alignTipLabels;
		invalidate();
	}

	public void setCurvature(double curvature) {
		setBranchCurveProportion(1.0 - curvature, 1.0 - curvature);
	}

	public void setBranchCurveProportion(double xProportion, double yProportion) {
		this.xProportion = xProportion;
		this.yProportion = yProportion;
		invalidate();
	}

	public void setRootLength(double rootLength) {
		this.rootLength = rootLength;
		invalidate();
	}

	protected void validate() {

		nodePoints.clear();
		branchPaths.clear();
		tipLabelPaths.clear();
		calloutPaths.clear();

		maxXPosition = 0.0;

		yPosition = 0.0;
		yIncrement = 1.0 / (tree.getExternalNodes().size() + 1);

		Node root = this.tree.getRootNode();
		double rl = rootLength * this.tree.getHeight(root);

		maxXPosition = 0.0;
		getMaxXPosition(root, rl);

		Point2D rootPoint = constructNode(root, rl);

		// construct a root branch line
		Line2D line = new Line2D.Double(0.0, rootPoint.getY(), rootPoint.getX(), rootPoint.getY());

		// add the line to the map of branch paths
		branchPaths.put(root, line);

	}

	private Point2D constructNode(Node node, double xPosition) {

		Point2D nodePoint;

		if (!tree.isExternal(node)) {

			double yPos = 0.0;

			List<Node> children = tree.getChildren(node);
			for (Node child : children) {

				double length = tree.getLength(child);
				Point2D childPoint = constructNode(child, xPosition + length);
				yPos += childPoint.getY();
			}

			// the y-position of the node is the average of the child nodes
			yPos /= children.size();

			nodePoint = new Point2D.Double(xPosition, yPos);

			for (Node child : children) {

				Point2D childPoint = nodePoints.get(child);

				GeneralPath branchPath = new GeneralPath();

				// start point
				float x0 = (float) nodePoint.getX();
				float y0 = (float) nodePoint.getY();

				// end point
				float x1 = (float) childPoint.getX();
				float y1 = (float) childPoint.getY();

				if (xProportion < 1.0 || yProportion < 1.0) {
					// if the curvature is on then we simply don't
					// do tree colouring - I just can't be bothered to
					// implement it (and it would probably be confusing anyway).
					float x2 = x1 - ((x1 - x0) * (float) xProportion);
					float y2 = y0 + ((y1 - y0) * (float) yProportion);

					branchPath.moveTo(x0, y0);
					branchPath.lineTo(x0, y2);
					branchPath.quadTo(x0, y1, x2, y1);
					branchPath.lineTo(x1, y1);
				} else {

					Object[] colouring = null;
					if (colouringAttributeName != null) {
						colouring = (Object[])child.getAttribute(colouringAttributeName);
					}
					if (colouring != null) {
						// If there is a colouring, then we break the path up into
						// segments. This should allow use to iterate along the segments
						// and colour them as we draw them.


						float nodeHeight = (float) tree.getHeight(node);
						float childHeight = (float) tree.getHeight(child);

						float dist = x1 - x0;

						// to help this, we are going to draw the branch backwards
						branchPath.moveTo(x1, y1);
						for (int i = 0; i < colouring.length - 1; i+=2) {
							float height = ((Number)colouring[i+1]).floatValue();
							float p = (height - childHeight) / (nodeHeight - childHeight);
							float x = x1 - (dist * p);
							branchPath.lineTo(x, y1);
						}
						branchPath.lineTo(x0, y1);
						branchPath.lineTo(x0, y0);
					} else {
						branchPath.moveTo(x0, y0);
						branchPath.lineTo(x0, y1);
						branchPath.lineTo(x1, y1);
					}
				}

				// add the branchPath to the map of branch paths
				branchPaths.put(child, branchPath);

				double x3 = (nodePoint.getX() + childPoint.getX()) / 2;
				Line2D branchLabelPath = new Line2D.Double(
						x3 - 1.0, childPoint.getY(),
						x3 + 1.0, childPoint.getY());

				branchLabelPaths.put(child, branchLabelPath);
			}

			Line2D nodeLabelPath = new Line2D.Double(
					nodePoint.getX(), nodePoint.getY(),
					nodePoint.getX() + 1.0, nodePoint.getY());

			nodeLabelPaths.put(node, nodeLabelPath);

			Line2D nodeBarPath = new Line2D.Double(
					nodePoint.getX() - 1.0, nodePoint.getY(),
					nodePoint.getX() + 1.0, nodePoint.getY());

			nodeBarPaths.put(node, nodeBarPath);
		} else {

			nodePoint = new Point2D.Double(xPosition, yPosition);

			Line2D tipLabelPath;

			if (alignTipLabels) {

				tipLabelPath = new Line2D.Double(
						maxXPosition, nodePoint.getY(),
						maxXPosition + 1.0, nodePoint.getY());

				Line2D calloutPath = new Line2D.Double(
						nodePoint.getX(), nodePoint.getY(),
						maxXPosition, nodePoint.getY());

				calloutPaths.put(node, calloutPath);

			} else {
				tipLabelPath = new Line2D.Double(
						nodePoint.getX(), nodePoint.getY(),
						nodePoint.getX() + 1.0, nodePoint.getY());

			}

			tipLabelPaths.put(node, tipLabelPath);

			yPosition += yIncrement;

		}

		// add the node point to the map of node points
		nodePoints.put(node, nodePoint);

		return nodePoint;
	}

	private void getMaxXPosition(Node node, double xPosition) {

		if (!tree.isExternal(node)) {

			List<Node> children = tree.getChildren(node);

			for (Node child : children) {
				double length = tree.getLength(child);
				getMaxXPosition(child, xPosition + length);
			}

		} else {
			if (xPosition > maxXPosition) {
				maxXPosition = xPosition;
			}
		}
	}

	private double yPosition;
	private double yIncrement;

	private double maxXPosition;

	private double xProportion = 1.0;
	private double yProportion = 1.0;

	private double rootLength = 0.01;

	private boolean alignTipLabels = false;

}
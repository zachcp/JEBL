package jebl.gui.trees.treeviewer.treelayouts;

import jebl.evolution.graphs.Node;
import org.virion.jam.controlpanels.*;
import org.virion.jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
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

    public boolean maintainAspectRatio() {
        return false;
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

    public List<Controls> getControls() {

	    List<Controls> controlsList = new ArrayList<Controls>();

        if (controls == null) {
            OptionsPanel optionsPanel = new OptionsPanel();

            final JSlider slider1 = new JSlider(SwingConstants.HORIZONTAL, 0, 10000, 0);
            slider1.setValue((int)(rootLength * 10000));
            slider1.setPaintTicks(true);
            slider1.setPaintLabels(true);

            slider1.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    double value = slider1.getValue();
                    setRootLength(value / 10000.0);
                }
            });
            optionsPanel.addComponentWithLabel("Root Length:", slider1, true);

            final JSlider slider2 = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 0);
            slider2.setPaintTicks(true);
            slider2.setPaintLabels(true);

            slider2.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    double value = 1.0 - (((double)slider2.getValue()) / 100.0);
                    setBranchCurveProportion(value, value);
                }
            });
            optionsPanel.addComponentWithLabel("Curvature:", slider2, true);

            final JCheckBox checkBox1 = new JCheckBox("Align Taxon Labels");

            checkBox1.setSelected(alignTaxonLabels);
            checkBox1.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    setAlignTaxonLabels(checkBox1.isSelected());
                }
            });
            optionsPanel.addComponent(checkBox1);

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

    public void setRootLength(double rootLength) {
        this.rootLength = rootLength;
        invalidate();
    }

    public void setBranchCurveProportion(double xProportion, double yProportion) {
        this.xProportion = xProportion;
        this.yProportion = yProportion;
        invalidate();
    }

	public void setAlignTaxonLabels(boolean alignTaxonLabels) {
		this.alignTaxonLabels = alignTaxonLabels;
		invalidate();
	}

	protected void validate() {
        nodePoints.clear();
        branchPaths.clear();
        taxonLabelPaths.clear();
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
		Line2D line = new Line2D.Double( 0.0, rootPoint.getY(), rootPoint.getX(), rootPoint.getY());

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
                float x0 = (float)nodePoint.getX();
                float y0 = (float)nodePoint.getY();

                // end point
                float x1 = (float)childPoint.getX();
                float y1 = (float)childPoint.getY();

                float x2 = x1 - ((x1 - x0) * (float)xProportion);
                float y2 = y0 + ((y1 - y0) * (float)yProportion);

                branchPath.moveTo(x0, y0);
                branchPath.lineTo(x0, y2);
                branchPath.quadTo(x0, y1, x2, y1);
                branchPath.lineTo(x1, y1);

                // add the branchPath to the map of branch paths
                branchPaths.put(child, branchPath);

	            double x3 = (nodePoint.getX() + childPoint.getX()) / 2;
	            Line2D branchLabelPath = new Line2D.Double(
				            x3, childPoint.getY(),
				            x3 + 1.0, childPoint.getY());

	            branchLabelPaths.put(child, branchLabelPath);

            }

	        Line2D nodeLabelPath = new Line2D.Double(
				        nodePoint.getX(), nodePoint.getY(),
				        nodePoint.getX() + 1.0, nodePoint.getY());

	        nodeLabelPaths.put(node, nodeLabelPath);

        } else {

            nodePoint = new Point2D.Double(xPosition, yPosition);

	        Line2D taxonLabelPath;

	        if (alignTaxonLabels) {

		        taxonLabelPath = new Line2D.Double(
				        maxXPosition, nodePoint.getY(),
				        maxXPosition + 1.0, nodePoint.getY());

		        Line2D calloutPath = new Line2D.Double(
				        nodePoint.getX(), nodePoint.getY(),
				        maxXPosition, nodePoint.getY());

		        calloutPaths.put(node, calloutPath);

	        } else {
		        taxonLabelPath = new Line2D.Double(
				        nodePoint.getX(), nodePoint.getY(),
				        nodePoint.getX() + 1.0, nodePoint.getY());

	        }

	        taxonLabelPaths.put(node, taxonLabelPath);

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

	private boolean alignTaxonLabels = false;
}
package jebl.gui.trees.treeviewer.treelayouts;

import jebl.evolution.graphs.Node;
import jebl.gui.trees.treeviewer.controlpanels.Controls;
import jebl.gui.trees.treeviewer.controlpanels.OptionsPanel;
import jebl.gui.trees.treeviewer.controlpanels.ControlPanel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.*;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class PolarTreeLayout extends AbstractTreeLayout {

	public enum TaxonLabelPosition {
		FLUSH,
		RADIAL,
		HORIZONTAL
	};

	public AxisType getXAxisType() {
		return AxisType.CONTINUOUS;
	}

	public AxisType getYAxisType() {
		return AxisType.CONTINUOUS;
	}

	public boolean maintainAspectRatio() {
		return true;
	}

    public void setControlPanel(ControlPanel controlPanel) {
        // do nothing...
    }

    public List<Controls> getControls() {

        List<Controls> controls = new ArrayList<Controls>();

        if (optionsPanel == null) {
            optionsPanel = new OptionsPanel();

            final JSlider slider1 = new JSlider(SwingConstants.HORIZONTAL, 0, 3600, 0);
            slider1.setValue((int)(180.0 - (rootAngle * 10)));
            slider1.setPaintTicks(true);
            slider1.setPaintLabels(true);

            slider1.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    double value = 180 + (slider1.getValue() / 10.0);
                    setRootAngle(value % 360);
                }
            });
            optionsPanel.addComponentWithLabel("Root Angle:", slider1, true);

            final JSlider slider2 = new JSlider(SwingConstants.HORIZONTAL, 0, 10000, 0);
            slider2.setValue((int)(rootLength * 10000));
            slider2.setPaintTicks(true);
            slider2.setPaintLabels(true);

            slider2.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    double value = slider2.getValue();
                    setRootLength(value / 10000.0);
                }
            });
            optionsPanel.addComponentWithLabel("Root Length:", slider2, true);

            final JSlider slider3 = new JSlider(SwingConstants.HORIZONTAL, 0, 3600, 0);
            slider3.setValue((int)(360.0 - (angularRange * 10)));
            slider3.setPaintTicks(true);
            slider3.setPaintLabels(true);

            slider3.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    double value = 360.0 - (slider3.getValue() / 10.0);
                    setAngularRange(value);
                }
            });
            optionsPanel.addComponentWithLabel("Angle Range:", slider3, true);

            final JComboBox combo1 = new JComboBox();
            for (TaxonLabelPosition position : TaxonLabelPosition.values()) {
                if( position != TaxonLabelPosition.HORIZONTAL ) // not implemented yet
                  combo1.addItem(position);
            }
            combo1.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent itemEvent) {
                    setTaxonLabelPosition((TaxonLabelPosition)combo1.getSelectedItem());

                }
            });
            optionsPanel.addComponentWithLabel("Label Position:", combo1);
        }

        controls.add(new Controls("Layout", optionsPanel));

        return controls;
    }
    private OptionsPanel optionsPanel = null;

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

	public void setTaxonLabelPosition(TaxonLabelPosition taxonLabelPosition) {
		this.taxonLabelPosition = taxonLabelPosition;
		invalidate();
	}

	protected void validate() {
		nodePoints.clear();
		branchPaths.clear();
		taxonLabelPaths.clear();
		calloutPaths.clear();

		Node root = this.tree.getRootNode();
		double rl = (rootLength * this.tree.getHeight(root)) * 10.0;

		maxXPosition = 0.0;
		getMaxXPosition(root, rl);

		yPosition = 0.0;
		yIncrement = 1.0 / tree.getExternalNodes().size();

		Point2D rootPoint = constructNode(root, rl);

		// construct a root branch line
		Line2D line = new Line2D.Double(
				transform(0.0, rootPoint.getY()),
				transform(rootPoint.getX(), rootPoint.getY()));

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

				double length = tree.getLength(child);
				childPoints[i] = constructNode(child, xPosition + length);
				yPos += childPoints[i].getY();

				i++;
			}

			// the y-position of the node is the average of the child nodes
			yPos /= children.size();

			nodePoint = new Point2D.Double(xPosition, yPos);
			transformedNodePoint = transform(nodePoint);

			i = 0;
			for (Node child : children) {

				GeneralPath branchPath = new GeneralPath();


				double start = getAngle(nodePoint.getY());
				double finish = getAngle(childPoints[i].getY());

				Arc2D arc = new Arc2D.Double();
				arc.setArcByCenter(0.0, 0.0, nodePoint.getX(), start, finish - start, Arc2D.OPEN);
				branchPath.append(arc, true);

				Point2D p = transform(childPoints[i]);
				branchPath.lineTo((float)p.getX(), (float)p.getY());

				// add the branchPath to the map of branch paths
				branchPaths.put(child, branchPath);

				i++;
			}

			Line2D nodeLabelPath = new Line2D.Double(
							transform(maxXPosition, nodePoint.getY()),
							transform(maxXPosition + 1.0, nodePoint.getY()));

			nodeLabelPaths.put(node, nodeLabelPath);
		} else {

			nodePoint = new Point2D.Double(xPosition, yPosition);
			transformedNodePoint = transform(nodePoint);

			Line2D taxonLabelPath;

			if (taxonLabelPosition == TaxonLabelPosition.FLUSH) {

				taxonLabelPath = new Line2D.Double(transformedNodePoint,
						transform(nodePoint.getX() + 1.0, nodePoint.getY()));

			} else if (taxonLabelPosition == TaxonLabelPosition.RADIAL) {

				taxonLabelPath = new Line2D.Double(
						transform(maxXPosition, nodePoint.getY()),
						transform(maxXPosition + 1.0, nodePoint.getY()));

				Line2D calloutPath = new Line2D.Double(
						transformedNodePoint,
						transform(maxXPosition, nodePoint.getY()));

				calloutPaths.put(node, calloutPath);

			} else if (taxonLabelPosition == TaxonLabelPosition.HORIZONTAL) {
                // this option disabled in getControls (JH)
				throw new UnsupportedOperationException("Not implemented yet");
			} else {
                // this is a bug
                throw new IllegalArgumentException("Unrecognized enum value");
			}

			taxonLabelPaths.put(node, taxonLabelPath);

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
				double length = tree.getLength(child);
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
	 * @param point
	 * @return the point in polar space
	 */
	private Point2D transform(Point2D point) {
		return transform(point.getX(), point.getY());
	}

	/**
	 * Polar transform
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

	private TaxonLabelPosition taxonLabelPosition = TaxonLabelPosition.FLUSH;
}
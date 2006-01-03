package jebl.gui.trees.treeviewer;

import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.Tree;
import jebl.gui.trees.treeviewer.controlpanels.ControlsProvider;
import jebl.gui.trees.treeviewer.controlpanels.Controls;
import jebl.gui.trees.treeviewer.decorators.BranchDecorator;
import jebl.gui.trees.treeviewer.painters.Painter;
import jebl.gui.trees.treeviewer.painters.PainterListener;
import jebl.gui.trees.treeviewer.treelayouts.TreeLayout;
import jebl.gui.trees.treeviewer.treelayouts.TreeLayoutListener;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.geom.*;
import java.awt.print.*;
import java.util.*;
import java.util.List;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class TreePane extends JComponent implements ControlsProvider, PainterListener, Printable {

	public TreePane() {
	}

	public void setTree(Tree tree) {
		this.tree = (RootedTree)tree;
		treeLayout.setTree(tree);

		calibrated = false;
		invalidate();
		repaint();
	}

    public void painterChanged() {
        calibrated = false;
        repaint();
    }

	public boolean isShowingRootBranch() {
		return showingRootBranch;
	}

	public void setShowingRootBranch(boolean showingRootBranch) {
		this.showingRootBranch = showingRootBranch;
		calibrated = false;
		repaint();
	}

	public boolean isShowingTaxonLabels() {
		return showingTaxonLabels;
	}

	public void setShowingTaxonLabels(boolean showingTaxonLabels) {
		this.showingTaxonLabels = showingTaxonLabels;
		calibrated = false;
		repaint();
	}

	public boolean isShowingTaxonCallouts() {
		return showingTaxonCallouts;
	}

	public void setShowingTaxonCallouts(boolean showingTaxonCallouts) {
		this.showingTaxonCallouts = showingTaxonCallouts;
		calibrated = false;
		repaint();
	}

	public void setSelectedNode(Node selectedNode) {
		selectedNodes.clear();
		selectedTaxa.clear();
        addSelectedNode(selectedNode);
	}

	public void setSelectedTaxon(Taxon selectedTaxon) {
		selectedNodes.clear();
		selectedTaxa.clear();
        addSelectedTaxon(selectedTaxon);
	}

	public void setSelectedClade(Node selectedNode) {
		selectedNodes.clear();
		selectedTaxa.clear();
        addSelectedClade(selectedNode);
	}

	public void setSelectedTaxa(Node selectedNode) {
		selectedNodes.clear();
		selectedTaxa.clear();
		addSelectedTaxa(selectedNode);
	}

	public void addSelectedNode(Node selectedNode) {
		if (selectedNode != null) {
			selectedNodes.add(selectedNode);
		}
        fireSelectionChanged();
		repaint();
	}

	public void addSelectedTaxon(Taxon selectedTaxon) {
		if (selectedTaxon != null) {
			selectedTaxa.add(selectedTaxon);
		}
        fireSelectionChanged();
		repaint();
	}

	public void addSelectedClade(Node selectedNode) {
		if (selectedNode != null) {
            addSelectedChildClades(selectedNode);
		}
        fireSelectionChanged();
		repaint();
	}

    private void addSelectedChildClades(Node selectedNode) {
        selectedNodes.add(selectedNode);
        for (Node child : tree.getChildren(selectedNode)) {
            addSelectedChildClades(child);
        }
    }

	public void addSelectedTaxa(Node selectedNode) {
		if (selectedNode != null) {
            addSelectedChildTaxa(selectedNode);
        }
        fireSelectionChanged();
		repaint();
	}

    private void addSelectedChildTaxa(Node selectedNode) {
        if (tree.isExternal(selectedNode)) {
            selectedTaxa.add(tree.getTaxon(selectedNode));
        }
        for (Node child : tree.getChildren(selectedNode)) {
            addSelectedChildTaxa(child);
        }
    }

	public void clearSelection() {
		selectedNodes.clear();
		selectedTaxa.clear();
        fireSelectionChanged();
		repaint();
	}

	public void annotateSelectedNodes(String name, Object value) {
		for (Node selectedNode : selectedNodes) {
			selectedNode.setAttribute(name, value);
		}
		repaint();
	}

	public void annotateSelectedTaxa(String name, Object value) {
		for (Taxon selectedTaxon : selectedTaxa) {
			selectedTaxon.setAttribute(name, value);
		}
		repaint();
	}

	public void setTreeLayout(TreeLayout treeLayout) {
		this.treeLayout = treeLayout;
		treeLayout.setTree(tree);
		treeLayout.addTreeLayoutListener(new TreeLayoutListener() {
			public void treeLayoutChanged() {
				calibrated = false;
				repaint();
			}
		});
		calibrated = false;
		invalidate();
		repaint();
	}

	public void setTaxonLabelPainter(Painter<Taxon> taxonLabelPainter) {
        if (this.taxonLabelPainter != null) {
            this.taxonLabelPainter.removePainterListener(this);
        }
        this.taxonLabelPainter = taxonLabelPainter;
        this.taxonLabelPainter.addPainterListener(this);
		calibrated = false;
		repaint();
	}

    public Painter<Taxon> getTaxonLabelPainter() {
        return taxonLabelPainter;
    }

	public void setNodeLabelPainter(Painter<Node> nodeLabelPainter) {
        if (this.nodeLabelPainter != null) {
            this.nodeLabelPainter.removePainterListener(this);
        }
        this.nodeLabelPainter = nodeLabelPainter;
        this.nodeLabelPainter.addPainterListener(this);
		calibrated = false;
		repaint();
	}

    public Painter<Node> getNodeLabelPainter() {
        return nodeLabelPainter;
    }

    public void setBranchDecorator(BranchDecorator branchDecorator) {
		this.branchDecorator = branchDecorator;
		calibrated = false;
		repaint();
	}

	public void setBranchLineWeight(float weight) {
		branchLineStroke = new BasicStroke(weight);
		selectionStroke = new BasicStroke(Math.max(weight + 4.0F, weight * 1.5F), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		repaint();
	}

	public void setPreferredSize(Dimension dimension) {
		if (treeLayout.maintainAspectRatio()) {
			super.setPreferredSize(new Dimension(dimension.width, dimension.height));
		} else {
			super.setPreferredSize(dimension);
		}

		calibrated = false;
	}

	public Node getNodeAt(Graphics2D g2, Point point) {
		Rectangle rect = new Rectangle(point.x - 1, point.y - 1, 3, 3);

		for (Node node : tree.getExternalNodes()) {
			Shape taxonLabelBound = taxonLabelBounds.get(tree.getTaxon(node));

			if (taxonLabelBound != null && g2.hit(rect, taxonLabelBound, false)) {
				return node;
			}
		}

		for (Node node : tree.getNodes()) {
			Shape branchPath = transform.createTransformedShape(treeLayout.getBranchPath(node));
			if (branchPath != null && g2.hit(rect, branchPath, true)) {
				return node;
			}
		}

		return null;
	}

	public Set<Node> getNodesAt(Graphics2D g2, Rectangle rect) {

		Set<Node> nodes = new HashSet<Node>();
		for (Node node : tree.getExternalNodes()) {
			Shape taxonLabelBound = taxonLabelBounds.get(tree.getTaxon(node));
			if (taxonLabelBound != null && g2.hit(rect, taxonLabelBound, false)) {
				nodes.add(node);
			}
		}

		for (Node node : tree.getNodes()) {
			Shape branchPath = transform.createTransformedShape(treeLayout.getBranchPath(node));
			if (branchPath != null && g2.hit(rect, branchPath, true)) {
				nodes.add(node);
			}
		}

		return nodes;
	}

	public Set<Node> getSelectedNodes() {
		return selectedNodes;
	}

    public Set<Taxon> getSelectedTaxa() {
        return selectedTaxa;
    }

	public Rectangle2D getDragRectangle() {
		return dragRectangle;
	}

	public void setDragRectangle(Rectangle2D dragRectangle) {
		this.dragRectangle = dragRectangle;
		repaint();
	}

	public void scrollPointToVisible(Point point) {
		scrollRectToVisible(new Rectangle(point.x, point.y, 0, 0));
	}

	public Point2D getRootPoint() {
		return null;
	}

	public Rectangle2D getTreeBounds() {
		return null;
	}

	public List<Controls> getControls() {

		List<Controls> controls = new ArrayList<Controls>();

		controls.addAll(treeLayout.getControls());

		if (controlsPanel == null) {
			controlsPanel = new JPanel();
			Box box = Box.createVerticalBox();

			final JCheckBox checkBox1 = new JCheckBox("Show Taxon Labels");
			checkBox1.setFont(checkBox1.getFont().deriveFont(11.0f));
			checkBox1.setAlignmentX(Component.LEFT_ALIGNMENT);
			box.add(checkBox1);

			checkBox1.setSelected(isShowingTaxonLabels());
			checkBox1.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent changeEvent) {
					setShowingTaxonLabels(checkBox1.isSelected());
				}
			});

			final JCheckBox checkBox2 = new JCheckBox("Show Root Branch");
			checkBox2.setFont(checkBox2.getFont().deriveFont(11.0f));
			checkBox2.setAlignmentX(Component.LEFT_ALIGNMENT);
			box.add(checkBox2);

			checkBox2.setSelected(isShowingRootBranch());
			checkBox2.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent changeEvent) {
					setShowingRootBranch(checkBox2.isSelected());

				}
			});

			JPanel panel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
			JLabel label = new JLabel("Line Weight:");
			label.setFont(label.getFont().deriveFont(11.0f));
			panel2.add(label);
			final JSpinner spinner2 = new JSpinner(new SpinnerNumberModel(1.0, 0.01, 48.0, 1.0));

			spinner2.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent changeEvent) {
					setBranchLineWeight(((Double)spinner2.getValue()).floatValue());
				}
			});
			panel2.add(spinner2);
			panel2.setAlignmentX(Component.LEFT_ALIGNMENT);
			box.add(panel2);
		}
		controls.add(new Controls("Formatting", controlsPanel));

		if (getTaxonLabelPainter() != null) {
			controls.addAll(getTaxonLabelPainter().getControls());
		}

		if (getNodeLabelPainter() != null) {
			controls.addAll(getNodeLabelPainter().getControls());
		}

	    return controls;
    }

	private JPanel controlsPanel = null;

	private final Set<TreeSelectionListener> treeSelectionListeners = new HashSet<TreeSelectionListener>();

    public void addTreeSelectionListener(TreeSelectionListener treeSelectionListener) {
        treeSelectionListeners.add(treeSelectionListener);
    }

    public void removeTreeSelectionListener(TreeSelectionListener treeSelectionListener) {
        treeSelectionListeners.remove(treeSelectionListener);
    }

    private void fireSelectionChanged() {
        for (TreeSelectionListener treeSelectionListener : treeSelectionListeners) {
            treeSelectionListener.selectionChanged();
        }
    }

	public void paint(Graphics graphics) {
		if (tree == null) return;

		Graphics2D g2 = (Graphics2D)graphics;

		Paint oldPaint = g2.getPaint();
		Stroke oldStroke = g2.getStroke();

		for (Node selectedNode : selectedNodes) {
			Shape branchPath = transform.createTransformedShape(treeLayout.getBranchPath(selectedNode));
			g2.setPaint(selectionPaint);
			g2.setStroke(selectionStroke);
			g2.draw(branchPath);
		}

        for (Taxon selectedTaxon : selectedTaxa) {
            g2.setPaint(selectionPaint);
            Shape labelBounds = taxonLabelBounds.get(selectedTaxon);
            if (labelBounds != null) {
                g2.fill(labelBounds);
            }
        }

		drawTree(g2, getWidth(), getHeight());

		if (dragRectangle != null) {
			g2.setPaint(new Color(128, 128, 128, 128));
			g2.fill(dragRectangle);

			g2.setStroke(new BasicStroke(2.0F));
			g2.setPaint(new Color(255, 255, 255, 128));
			g2.draw(dragRectangle);

			g2.setPaint(oldPaint);
			g2.setStroke(oldStroke);
		}

	}

	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {

		if (tree == null || pageIndex > 0) return NO_SUCH_PAGE;

		Graphics2D g2 = (Graphics2D)graphics;
		g2.translate (pageFormat.getImageableX(), pageFormat.getImageableY());

		calibrated = false;
		setDoubleBuffered(false);

		drawTree(g2, pageFormat.getImageableWidth(), pageFormat.getImageableHeight());

		setDoubleBuffered(true);
		calibrated = false;

		return PAGE_EXISTS;
	}

	protected void drawTree(Graphics2D g2, double width, double height) {

		if (!calibrated) calibrate(g2, width, height);

		AffineTransform oldTransform = g2.getTransform();
		Paint oldPaint = g2.getPaint();
		Stroke oldStroke = g2.getStroke();
		Font oldFont = g2.getFont();

		Set<Node> externalNodes = tree.getExternalNodes();
		for (Node node : externalNodes) {

			Shape branchPath = transform.createTransformedShape(treeLayout.getBranchPath(node));
			Taxon taxon = tree.getTaxon(node);

			if (taxonLabelPainter != null && showingTaxonLabels ) {

				AffineTransform taxonTransform = taxonLabelTransforms.get(taxon);
				Painter.LabelAlignment taxonLabelAlignment = taxonLabelAlignments.get(taxon);
				g2.transform(taxonTransform);

				taxonLabelPainter.paint(g2, taxon, taxonLabelAlignment, new Insets(0, 0, 0, 0));

				g2.setTransform(oldTransform);

			}

			if (showingTaxonCallouts) {
				Shape calloutPath = transform.createTransformedShape(treeLayout.getCalloutPath(node));
				if (calloutPath != null) {
					g2.setStroke(taxonCalloutStroke);
					g2.draw(calloutPath);
				}
			}

			if (branchDecorator != null) {
				g2.setPaint(branchDecorator.getBranchPaint(tree, node));
			} else {
				g2.setPaint(Color.BLACK);
			}

			g2.setStroke(branchLineStroke);
			g2.draw(branchPath);
		}

		Set<Node> internalNodes = tree.getInternalNodes();
		Node rootNode = tree.getRootNode();
		for (Node node : internalNodes) {
			if (showingRootBranch || node != rootNode) {
				Shape branchPath = transform.createTransformedShape(treeLayout.getBranchPath(node));
				g2.setStroke(branchLineStroke);
				if (branchDecorator != null) {
					g2.setPaint(branchDecorator.getBranchPaint(tree, node));
				} else {
					g2.setPaint(Color.BLACK);
				}
				g2.draw(branchPath);

				if (nodeLabelPainter != null) {

					AffineTransform nodeTransform = nodeLabelTransforms.get(node);
					Painter.LabelAlignment nodeLabelAlignment = nodeLabelAlignments.get(node);
					g2.transform(nodeTransform);

					nodeLabelPainter.paint(g2, node, nodeLabelAlignment, new Insets(0, 0, 0, 0));

					g2.setTransform(oldTransform);

				}

			}
		}

		g2.setStroke(oldStroke);
		g2.setPaint(oldPaint);
		g2.setFont(oldFont);
	}

	private void calibrate(Graphics2D g2, double width, double height) {

		// First of all get the bounds for the unscaled tree
		Rectangle2D treeBounds = new Rectangle2D.Double(0.0, 0.0, 0.0, 0.0);

		// Create a new empty bounds
		Rectangle2D bounds = new Rectangle2D.Double(0,0,0,0);

		Node rootNode = tree.getRootNode();

		for (Node node : tree.getNodes()) {
			if (showingRootBranch || node != rootNode) {
                Shape branchPath = treeLayout.getBranchPath(node);
                // Add the bounds of the branch path to the overall bounds
                treeBounds.add(branchPath.getBounds2D());
            }
		}

		// add the tree bounds
		bounds.add(treeBounds);

		Set<Node> externalNodes = tree.getExternalNodes();

		double labelHeight = 0;
		double labelWidth = 0;
        Rectangle2D labelBounds = null;

		if (taxonLabelPainter != null && showingTaxonLabels) {
			taxonLabelPainter.calibrate(g2);
			labelHeight = taxonLabelPainter.getPreferredHeight();
			labelWidth = taxonLabelPainter.getPreferredWidth();
            labelBounds = new Rectangle2D.Double(0.0,  0.0, labelWidth, labelHeight);

            // Iterate though the external nodes
			for (Node node : externalNodes) {

				// Get the line that represents the path for the taxon label
				Line2D taxonPath = treeLayout.getTaxonLabelPath(node);

				// Work out how it is rotated and create a transform that matches that
                AffineTransform taxonTransform = calculateTransform(null, taxonPath, labelWidth, labelHeight);

				// and add the translated bounds to the overall bounds
				bounds.add(taxonTransform.createTransformedShape(labelBounds).getBounds2D());
			}
		}

		// get the difference between the tree's bounds and the overall bounds
		double xDiff = bounds.getWidth() - treeBounds.getWidth();
		double yDiff = bounds.getHeight() - treeBounds.getHeight();

		// Get the amount of canvas that is going to be taken up by the tree -
		// The rest is taken up by taxon labels which don't scale
		double w = width - insets.left - insets.right - xDiff;
		double h = height - insets.top - insets.bottom - yDiff;

		double xScale;
		double yScale;

		double xOffset = 0.0;
		double yOffset = 0.0;

		if (treeLayout.maintainAspectRatio()) {
			// If the tree is layed out in both dimensions then we
			// need to find out which axis has the least space and scale
			// the tree to that (to keep the aspect ratio.
			if ((w / treeBounds.getWidth()) < (h / treeBounds.getHeight())) {
				xScale = w / treeBounds.getWidth();
				yScale = xScale;
			} else {
				yScale = h / treeBounds.getHeight();
				xScale = yScale;
			}

			// and set the origin so that the center of the tree is in
			// the center of the canvas
			xOffset = ((width - (treeBounds.getWidth() * xScale)) / 2) - (treeBounds.getX() * xScale);
			yOffset = ((height - (treeBounds.getHeight() * yScale)) / 2) - (treeBounds.getY() * yScale);

		} else {
			// Otherwise just scale both dimensions
			xScale = w / treeBounds.getWidth();
			yScale = h / treeBounds.getHeight();

			// and set the origin in the top left corner
			xOffset = -bounds.getX();
			yOffset = -bounds.getY();
		}

		// Create the overall transform
		transform = new AffineTransform();
		transform.translate(xOffset + insets.left, yOffset + insets.top);
		transform.scale(xScale, yScale);

		// Clear the map of individual taxon label bounds and transforms
        taxonLabelBounds.clear();
		taxonLabelTransforms.clear();
		taxonLabelAlignments.clear();

		if (taxonLabelPainter != null) {
			// Iterate though the external nodes
			for (Node node : externalNodes) {
				Taxon taxon = tree.getTaxon(node);

				// Get the line that represents the path for the taxon label
				Line2D taxonPath = treeLayout.getTaxonLabelPath(node);

				// Work out how it is rotated and create a transform that matches that
				AffineTransform taxonTransform = calculateTransform(transform, taxonPath, labelWidth, labelHeight);

				// Store the transformed bounds in the map for use when selecting
				taxonLabelBounds.put(taxon, taxonTransform.createTransformedShape(labelBounds));

				// Store the transform in the map for use when drawing
				taxonLabelTransforms.put(taxon, taxonTransform);

				// Store the alignment in the map for use when drawing
				if (taxonPath.getX1() < taxonPath.getX2()) {
					taxonLabelAlignments.put(taxon, Painter.LabelAlignment.LEFT);
				} else {
					taxonLabelAlignments.put(taxon, Painter.LabelAlignment.RIGHT);
				}
			}
		}

		// Clear the map of individual node label bounds and transforms
        nodeLabelBounds.clear();
		nodeLabelTransforms.clear();
		nodeLabelAlignments.clear();

		if (nodeLabelPainter != null) {
			// Iterate though the external nodes
			for (Node node : tree.getNodes()) {

				// Get the line that represents the path for the taxon label
				Line2D labelPath = treeLayout.getNodeLabelPath(node);

				if (labelPath != null) {
					// Work out how it is rotated and create a transform that matches that
					AffineTransform taxonTransform = calculateTransform(transform, labelPath, labelWidth, labelHeight);

					// Store the transformed bounds in the map for use when selecting
					nodeLabelBounds.put(node, taxonTransform.createTransformedShape(labelBounds));

					// Store the transform in the map for use when drawing
					nodeLabelTransforms.put(node, taxonTransform);

					// Store the alignment in the map for use when drawing
					if (labelPath.getX1() < labelPath.getX2()) {
						nodeLabelAlignments.put(node, Painter.LabelAlignment.LEFT);
					} else {
						nodeLabelAlignments.put(node, Painter.LabelAlignment.RIGHT);
					}
				}
			}
		}

        calloutPaths.clear();

		calibrated = true;
	}

    private AffineTransform calculateTransform(AffineTransform globalTransform, Line2D line, double width, double height) {
        // Work out how it is rotated and create a transform that matches that
        AffineTransform lineTransform = new AffineTransform();

        Point2D origin = line.getP1();
        if (globalTransform != null) {
            globalTransform.transform(origin, origin);
        }

        double angle = Math.atan((line.getY2() - line.getY1()) / (line.getX2() - line.getX1()));
        lineTransform.rotate(angle, origin.getX(), origin.getY());

        // Now add a translate to the transform - if it is on the left then we need
        // to shift it by the entire width of the string.
        if (line.getX2() > line.getX1()) {

            lineTransform.translate(origin.getX() + labelXOffset, origin.getY() - (height / 2.0));
        } else {

            lineTransform.translate(origin.getX() - (labelXOffset + width), origin.getY() - (height / 2.0));
        }

        return lineTransform;
    }

    // Overridden methods to recalibrate tree when bounds change
	public void setBounds(int x, int y, int width, int height) {
		calibrated = false;
		super.setBounds(x, y, width, height);
	}

	public void setBounds(Rectangle rectangle) {
		calibrated = false;
		super.setBounds(rectangle);
	}

	public void setSize(Dimension dimension) {
		calibrated = false;
		super.setSize(dimension);
	}

	public void setSize(int width, int height) {
		calibrated = false;
		super.setSize(width, height);
	}

	private RootedTree tree = null;
	private TreeLayout treeLayout = null;

	private Insets margins = new Insets(6, 6, 6, 6);
	private Insets insets = new Insets(6, 6, 6, 6);

    private Set<Node> selectedNodes = new HashSet<Node>();
	private Set<Taxon> selectedTaxa = new HashSet<Taxon>();

	private Rectangle2D dragRectangle = null;

	private BranchDecorator branchDecorator = null;

	private float labelXOffset = 5.0F;
	private Painter<Taxon> taxonLabelPainter = null;
	private Painter<Node> nodeLabelPainter = null;

	private Stroke branchLineStroke = new BasicStroke(1.0F);
	private Stroke taxonCalloutStroke = new BasicStroke(0.5F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[] { 0.5f, 2.0f }, 0.0f);
	private Stroke selectionStroke = new BasicStroke(6.0F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	private Paint selectionPaint = new Color(180, 213, 254);

	private boolean calibrated = false;
	private AffineTransform transform = null;

	private boolean showingRootBranch = true;
	private boolean showingTaxonLabels = true;
	private boolean showingTaxonCallouts = true;

	private Map<Taxon, AffineTransform> taxonLabelTransforms = new HashMap<Taxon, AffineTransform>();
	private Map<Taxon, Shape> taxonLabelBounds = new HashMap<Taxon, Shape>();
	private Map<Taxon, Painter.LabelAlignment> taxonLabelAlignments = new HashMap<Taxon, Painter.LabelAlignment>();

	private Map<Node, AffineTransform> nodeLabelTransforms = new HashMap<Node, AffineTransform>();
	private Map<Node, Shape> nodeLabelBounds = new HashMap<Node, Shape>();
	private Map<Node, Painter.LabelAlignment> nodeLabelAlignments = new HashMap<Node, Painter.LabelAlignment>();

	private Map<Taxon, Shape> calloutPaths = new HashMap<Taxon, Shape>();

}
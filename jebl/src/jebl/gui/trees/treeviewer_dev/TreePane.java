package jebl.gui.trees.treeviewer_dev;

import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.*;
import jebl.gui.trees.treeviewer_dev.decorators.BranchDecorator;
import jebl.gui.trees.treeviewer_dev.painters.Painter;
import jebl.gui.trees.treeviewer_dev.painters.PainterListener;
import jebl.gui.trees.treeviewer_dev.painters.LabelPainter;
import jebl.gui.trees.treeviewer_dev.treelayouts.TreeLayout;
import jebl.gui.trees.treeviewer_dev.treelayouts.TreeLayoutListener;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.print.*;
import java.util.*;
import java.util.prefs.Preferences;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class TreePane extends JComponent implements PainterListener, Printable {

	private static Preferences PREFS = Preferences.userNodeForPackage(TreePaneController.class);

	private static final String DEFAULT_FOREGROUND_COLOUR_PREFS = "defaultForegroundColour";
	private static final String DEFAULT_BACKGROUND_COLOUR_PREFS = "defaultBackgroundColour";
	private static final String DEFAULT_SELECTION_COLOUR_PREFS = "defaultSelectionColour";
	private static final String DEFAULT_BRANCH_LINE_WIDTH_PREFS = "defaultBranchLineWidth";

	// The defaults if there is nothing in the preferences
	private static Color DEFAULT_FOREGROUND_COLOUR = Color.BLACK;
	private static Color DEFAULT_BACKGROUND_COLOUR = Color.WHITE;
	private static Color DEFAULT_SELECTION_COLOUR = new Color(180, 213, 254);
	private static float DEFAULT_BRANCH_LINE_WIDTH = 1.0f;

	public TreePane() {
		int foregroundRGB = PREFS.getInt(DEFAULT_FOREGROUND_COLOUR_PREFS, DEFAULT_FOREGROUND_COLOUR.getRGB());
		int backgroundRGB = PREFS.getInt(DEFAULT_BACKGROUND_COLOUR_PREFS, DEFAULT_BACKGROUND_COLOUR.getRGB());
		int selectionRGB = PREFS.getInt(DEFAULT_SELECTION_COLOUR_PREFS, DEFAULT_SELECTION_COLOUR.getRGB());
		float branchLineWidth = PREFS.getFloat(DEFAULT_BRANCH_LINE_WIDTH_PREFS, DEFAULT_BRANCH_LINE_WIDTH);

		setForeground(new Color(foregroundRGB));
		setBackground(new Color(backgroundRGB));
		setSelectionPaint(new Color(selectionRGB));
		setBranchStroke(new BasicStroke(branchLineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
	}

	public RootedTree getTree() {
		return tree;
	}

	public void setTree(RootedTree tree) {
		if (tree != null) {
			this.originalTree = tree;
			if (!originalTree.hasLengths()) {
				transformBranchesOn = true;
			}
			setupTree();
		} else {
			originalTree = null;
			this.tree = null;
			invalidate();
			repaint();
		}
	}

	private void setupTree() {
		tree = originalTree;

		if (orderBranchesOn) {
			tree = new SortedRootedTree(tree, branchOrdering);
		}

		if (transformBranchesOn || !this.tree.hasLengths()) {
			tree = new TransformedRootedTree(tree, branchTransform);
		}

        if (tipLabelPainter != null) {
            tipLabelPainter.setupAttributes(tree);
        }

        if (nodeLabelPainter != null) {
            nodeLabelPainter.setupAttributes(tree);
        }

        if (branchLabelPainter != null) {
            branchLabelPainter.setupAttributes(tree);
        }

        if (treeLayout != null) {
			treeLayout.setTree(tree);

			calibrated = false;
			invalidate();
			repaint();
		}
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

	public Rectangle2D getTreeBounds() {
		return treeBounds;
	}

	/**
	 * This returns the scaling factor between the graphical image and the branch
	 * lengths of the tree
	 *
	 * @return the tree scale
	 */
	public double getTreeScale() {
		return treeScale;
	}

	public void painterChanged() {
		calibrated = false;
		repaint();
	}

	public BasicStroke getBranchStroke() {
		return branchLineStroke;
	}

	public void setBranchStroke(BasicStroke stroke) {
		branchLineStroke = stroke;
		float weight = stroke.getLineWidth();
		selectionStroke = new BasicStroke(Math.max(weight + 4.0F, weight * 1.5F), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		repaint();
	}

	public BasicStroke getCalloutStroke() {
		return calloutStroke;
	}

	public void setCalloutStroke(BasicStroke calloutStroke) {
		this.calloutStroke = calloutStroke;
	}

	public Paint getSelectionPaint() {
		return selectionPaint;
	}

	public void setSelectionPaint(Paint selectionPaint) {
		this.selectionPaint = selectionPaint;
	}

	public boolean isTransformBranchesOn() {
		return transformBranchesOn;
	}

	public void setTransformBranchesOn(boolean transformBranchesOn) {
		this.transformBranchesOn = transformBranchesOn;
		setupTree();
	}

	public TransformedRootedTree.Transform getBranchTransform() {
		return branchTransform;
	}

	public void setBranchTransform(TransformedRootedTree.Transform branchTransform) {
		this.branchTransform = branchTransform;
		setupTree();
	}

	public boolean isOrderBranchesOn() {
		return orderBranchesOn;
	}

	public void setOrderBranchesOn(boolean orderBranchesOn) {
		this.orderBranchesOn = orderBranchesOn;
		setupTree();
	}

	public SortedRootedTree.BranchOrdering getBranchOrdering() {
		return branchOrdering;
	}

	public void setBranchOrdering(SortedRootedTree.BranchOrdering branchOrdering) {
		this.branchOrdering = branchOrdering;
		setupTree();
	}

	public RootedTree getOriginalTree() {
		return originalTree;
	}

	public boolean isShowingRootBranch() {
		return showingRootBranch;
	}

	public void setShowingRootBranch(boolean showingRootBranch) {
		this.showingRootBranch = showingRootBranch;
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

	/**
	 * Return whether the two axis scales should be maintained
	 * relative to each other
	 *
	 * @return a boolean
	 */
	public boolean maintainAspectRatio() {
		return treeLayout.maintainAspectRatio();
	}

	public void setTipLabelPainter(LabelPainter<Node> tipLabelPainter) {
		tipLabelPainter.setTreePane(this);
		if (this.tipLabelPainter != null) {
			this.tipLabelPainter.removePainterListener(this);
		}
		this.tipLabelPainter = tipLabelPainter;
		if (this.tipLabelPainter != null) {
			this.tipLabelPainter.addPainterListener(this);
		}
		calibrated = false;
		repaint();
	}

	public Painter<Node> getTipLabelPainter() {
		return tipLabelPainter;
	}

	public void setNodeLabelPainter(LabelPainter<Node> nodeLabelPainter) {
		nodeLabelPainter.setTreePane(this);
		if (this.nodeLabelPainter != null) {
			this.nodeLabelPainter.removePainterListener(this);
		}
		this.nodeLabelPainter = nodeLabelPainter;
		if (this.nodeLabelPainter != null) {
			this.nodeLabelPainter.addPainterListener(this);
		}
		calibrated = false;
		repaint();
	}

	public Painter<Node> getNodeLabelPainter() {
		return nodeLabelPainter;
	}

	public void setBranchLabelPainter(LabelPainter<Node> branchLabelPainter) {
		branchLabelPainter.setTreePane(this);
		if (this.branchLabelPainter != null) {
			this.branchLabelPainter.removePainterListener(this);
		}
		this.branchLabelPainter = branchLabelPainter;
		if (this.branchLabelPainter != null) {
			this.branchLabelPainter.addPainterListener(this);
		}
		calibrated = false;
		repaint();
	}

	public Painter<Node> getBranchLabelPainter() {
		return branchLabelPainter;
	}

	public void setScaleBarPainter(Painter<TreePane> scaleBarPainter) {
		scaleBarPainter.setTreePane(this);
		if (this.scaleBarPainter != null) {
			this.scaleBarPainter.removePainterListener(this);
		}
		this.scaleBarPainter = scaleBarPainter;
		if (this.scaleBarPainter != null) {
			this.scaleBarPainter.addPainterListener(this);
		}
		calibrated = false;
		repaint();
	}

	public Painter<TreePane> getScaleBarPainter() {
		return scaleBarPainter;
	}

	public void setBranchDecorator(BranchDecorator branchDecorator) {
		this.branchDecorator = branchDecorator;
		calibrated = false;
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

	public double getHeightAt(Graphics2D graphics2D, Point2D point) {
		try {
			point = transform.inverseTransform(point, null);
		} catch (NoninvertibleTransformException e) {
			e.printStackTrace();
		}
		return treeLayout.getHeightOfPoint(point);
	}

	public Node getNodeAt(Graphics2D g2, Point point) {
		Rectangle rect = new Rectangle(point.x - 1, point.y - 1, 3, 3);

		for (Node node : tree.getExternalNodes()) {
			Shape taxonLabelBound = tipLabelBounds.get(tree.getTaxon(node));

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
			Shape taxonLabelBound = tipLabelBounds.get(tree.getTaxon(node));
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

	public void setRuler(double rulerHeight) {
		this.rulerHeight = rulerHeight;
	}

	public void scrollPointToVisible(Point point) {
		scrollRectToVisible(new Rectangle(point.x, point.y, 0, 0));
	}


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

		final Graphics2D g2 = (Graphics2D) graphics;
		if (!calibrated) calibrate(g2, getWidth(), getHeight());

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
			Shape labelBounds = tipLabelBounds.get(selectedTaxon);
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

		Graphics2D g2 = (Graphics2D) graphics;
		g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

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

			if (tipLabelPainter != null && tipLabelPainter.isVisible()) {

				AffineTransform taxonTransform = tipLabelTransforms.get(taxon);
				Painter.Justification taxonLabelJustification = tipLabelJustifications.get(taxon);
				g2.transform(taxonTransform);

				tipLabelPainter.paint(g2, node, taxonLabelJustification,
						new Rectangle2D.Double(0.0, 0.0, tipLabelWidth, tipLabelPainter.getPreferredHeight()));

				g2.setTransform(oldTransform);

				if (showingTaxonCallouts) {
					Shape calloutPath = transform.createTransformedShape(treeLayout.getCalloutPath(node));
					if (calloutPath != null) {
						g2.setStroke(calloutStroke);
						g2.draw(calloutPath);
					}
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

		final Node rootNode = tree.getRootNode();
		for (Node node : tree.getNodes() ) {
			if ( showingRootBranch || node != rootNode ) {
				if( !tree.isExternal(node) ) {
					Shape branchPath = transform.createTransformedShape(treeLayout.getBranchPath(node));
					g2.setStroke(branchLineStroke);
					if (branchDecorator != null) {
						g2.setPaint(branchDecorator.getBranchPaint(tree, node));
					} else {
						g2.setPaint(Color.BLACK);
					}
					g2.draw(branchPath);

					if (nodeLabelPainter != null && nodeLabelPainter.isVisible()) {

						AffineTransform nodeTransform = nodeLabelTransforms.get(node);
						if (nodeTransform != null) {
							Painter.Justification nodeLabelJustification = nodeLabelJustifications.get(node);
							g2.transform(nodeTransform);

							nodeLabelPainter.paint(g2, node, nodeLabelJustification,
									new Rectangle2D.Double(0.0, 0.0, nodeLabelPainter.getPreferredWidth(), nodeLabelPainter.getPreferredHeight()));

							g2.setTransform(oldTransform);
						}
					}
				}

				if (branchLabelPainter != null && branchLabelPainter.isVisible()) {

					AffineTransform branchTransform = branchLabelTransforms.get(node);
					if (branchTransform != null) {
						g2.transform(branchTransform);

						branchLabelPainter.calibrate(g2, node);
						final double preferredWidth = branchLabelPainter.getPreferredWidth();
						final double preferredHeight = branchLabelPainter.getPreferredHeight();

						//Line2D labelPath = treeLayout.getBranchLabelPath(node);

						branchLabelPainter.paint(g2, node, Painter.Justification.CENTER,
								//new Rectangle2D.Double(-preferredWidth/2, -preferredHeight, preferredWidth, preferredHeight));
								new Rectangle2D.Double(0, 0, preferredWidth, preferredHeight));

						g2.setTransform(oldTransform);
					}
				}
			}
		}

		if (scaleBarPainter != null && scaleBarPainter.isVisible()) {
			scaleBarPainter.paint(g2, this, Painter.Justification.CENTER, scaleBarBounds);
		}

		g2.setStroke(oldStroke);
		g2.setPaint(oldPaint);
		g2.setFont(oldFont);
	}

	private void calibrate(Graphics2D g2, double width, double height) {

		// First of all get the bounds for the unscaled tree
		treeBounds = null;

		Node rootNode = tree.getRootNode();

		// bounds on branches
		for (Node node : tree.getNodes()) {
			if (showingRootBranch || node != rootNode) {
				final Shape branchPath = treeLayout.getBranchPath(node);
				// Add the bounds of the branch path to the overall bounds
				final Rectangle2D branchBounds = branchPath.getBounds2D();
				if (treeBounds == null) {
					treeBounds = branchBounds;
				} else {
					treeBounds.add(branchBounds);
				}
			}
		}

		// add the tree bounds
		final Rectangle2D bounds = treeBounds.getBounds2D(); // (YH) same as (Rectangle2D) treeBounds.clone();

		final Set<Node> externalNodes = tree.getExternalNodes();

		if (tipLabelPainter != null && tipLabelPainter.isVisible()) {

			tipLabelWidth = 0.0;

			// Find the longest taxon label
			for (Node node : externalNodes) {

				tipLabelPainter.calibrate(g2, node);
				tipLabelWidth = Math.max(tipLabelWidth, tipLabelPainter.getPreferredWidth());
			}

			final double labelHeight = tipLabelPainter.getPreferredHeight();

			for (Node node : externalNodes) {
				// don't see why is that needed here? taxonLabelPainternot used in this loop (YH)?
				//tipLabelPainter.calibrate(g2, node);
				Rectangle2D labelBounds = new Rectangle2D.Double(0.0, 0.0, tipLabelWidth, labelHeight);

				// Get the line that represents the path for the taxon label
				Line2D taxonPath = treeLayout.getTipLabelPath(node);

				// Work out how it is rotated and create a transform that matches that
				AffineTransform taxonTransform = calculateTransform(null, taxonPath, tipLabelWidth, labelHeight, true);

				// and add the translated bounds to the overall bounds
				bounds.add(taxonTransform.createTransformedShape(labelBounds).getBounds2D());
			}
		}

		if (nodeLabelPainter != null && nodeLabelPainter.isVisible()) {
			// Iterate though the nodes
			for (Node node : tree.getNodes()) {
				// Get the line that represents the path for the taxon label
				final Line2D labelPath = treeLayout.getNodeLabelPath(node);

				if (labelPath != null) {
					nodeLabelPainter.calibrate(g2, node);
					final double labelHeight = nodeLabelPainter.getPreferredHeight();
					final double labelWidth = nodeLabelPainter.getPreferredWidth();
					Rectangle2D labelBounds = new Rectangle2D.Double(0.0, 0.0, labelWidth, labelHeight);

					// Work out how it is rotated and create a transform that matches that
					AffineTransform labelTransform = calculateTransform(null, labelPath, labelWidth, labelHeight, true);

					// and add the translated bounds to the overall bounds
					bounds.add(labelTransform.createTransformedShape(labelBounds).getBounds2D());
				}
			}
		}

		if (branchLabelPainter != null && branchLabelPainter.isVisible()) {
			// Iterate though the nodes
			for (Node node : tree.getNodes()) {
				// Get the line that represents the path for the branch label
				final Line2D labelPath = treeLayout.getBranchLabelPath(node);

				if (labelPath != null) {
					branchLabelPainter.calibrate(g2, node);
					final double labelHeight = branchLabelPainter.getHeightBound();
					final double labelWidth = branchLabelPainter.getPreferredWidth();

					Rectangle2D labelBounds = new Rectangle2D.Double(0.0, 0.0, labelWidth, labelHeight);

					// Work out how it is rotated and create a transform that matches that
					AffineTransform labelTransform = calculateTransform(null, labelPath, labelWidth, labelHeight, false);

					// and add the translated bounds to the overall bounds
					bounds.add(labelTransform.createTransformedShape(labelBounds).getBounds2D());
				}
			}
		}

		if (scaleBarPainter != null && scaleBarPainter.isVisible()) {
			scaleBarPainter.calibrate(g2, this);
			scaleBarBounds = new Rectangle2D.Double(treeBounds.getX(), treeBounds.getY(),
					treeBounds.getWidth(), scaleBarPainter.getPreferredHeight());
			bounds.add(scaleBarBounds);
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

			treeScale = xScale;

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

			treeScale = xScale;
		}

		// Create the overall transform
		transform = new AffineTransform();
		transform.translate(xOffset + insets.left, yOffset + insets.top);
		transform.scale(xScale, yScale);

		// Get the bounds for the newly scaled tree
		treeBounds = null;
		for (Node node : tree.getNodes()) {
			if (showingRootBranch || node != rootNode) {
				final Shape branchPath = transform.createTransformedShape(treeLayout.getBranchPath(node));
				final Rectangle2D bounds2D = branchPath.getBounds2D();
				if (treeBounds == null) {
					treeBounds = bounds2D;
				} else {
					treeBounds.add(bounds2D);
				}
			}
		}

		// Clear the map of individual taxon label bounds and transforms
		tipLabelBounds.clear();
		tipLabelTransforms.clear();
		tipLabelJustifications.clear();

		if (tipLabelPainter != null && tipLabelPainter.isVisible()) {
			final double labelHeight = tipLabelPainter.getPreferredHeight();
			Rectangle2D labelBounds = new Rectangle2D.Double(0.0, 0.0, tipLabelWidth, labelHeight);

			// Iterate though the external nodes
			for (Node node : externalNodes) {
				Taxon taxon = tree.getTaxon(node);

				// Get the line that represents the path for the tip label
				Line2D tipPath = treeLayout.getTipLabelPath(node);

				// Work out how it is rotated and create a transform that matches that
				AffineTransform taxonTransform = calculateTransform(transform, tipPath, tipLabelWidth, labelHeight, true);

				// Store the transformed bounds in the map for use when selecting
				tipLabelBounds.put(taxon, taxonTransform.createTransformedShape(labelBounds));

				// Store the transform in the map for use when drawing
				tipLabelTransforms.put(taxon, taxonTransform);

				// Store the alignment in the map for use when drawing
				final Painter.Justification just = (tipPath.getX1() < tipPath.getX2()) ?
						Painter.Justification.LEFT : Painter.Justification.RIGHT;
				tipLabelJustifications.put(taxon, just);
			}
		}

		// Clear the map of individual node label bounds and transforms
		nodeLabelBounds.clear();
		nodeLabelTransforms.clear();
		nodeLabelJustifications.clear();

		if (nodeLabelPainter != null && nodeLabelPainter.isVisible()) {
			final double labelHeight = nodeLabelPainter.getPreferredHeight();
			final double labelWidth = nodeLabelPainter.getPreferredWidth();
			final Rectangle2D labelBounds = new Rectangle2D.Double(0.0, 0.0, labelWidth, labelHeight);

			// Iterate though the external nodes
			for (Node node : tree.getNodes()) {
				// Get the line that represents the path for the node label
				final Line2D labelPath = treeLayout.getNodeLabelPath(node);

				if (labelPath != null) {
					// Work out how it is rotated and create a transform that matches that
					AffineTransform labelTransform = calculateTransform(transform, labelPath, labelWidth, labelHeight, true);

					// Store the transformed bounds in the map for use when selecting
					nodeLabelBounds.put(node, labelTransform.createTransformedShape(labelBounds));

					// Store the transform in the map for use when drawing
					nodeLabelTransforms.put(node, labelTransform);

					// Store the alignment in the map for use when drawing
					if (labelPath.getX1() < labelPath.getX2()) {
						nodeLabelJustifications.put(node, Painter.Justification.LEFT);
					} else {
						nodeLabelJustifications.put(node, Painter.Justification.RIGHT);
					}
				}
			}
		}

		if (branchLabelPainter != null && branchLabelPainter.isVisible()) {


			// Iterate though the external nodes
			for (Node node : tree.getNodes()) {

				// Get the line that represents the path for the branch label
				Line2D labelPath = treeLayout.getBranchLabelPath(node);

				if (labelPath != null) {
					branchLabelPainter.calibrate(g2, node);
					final double labelHeight = branchLabelPainter.getPreferredHeight();
					final double labelWidth = branchLabelPainter.getPreferredWidth();
					final Rectangle2D labelBounds = new Rectangle2D.Double(0.0, 0.0, labelWidth, labelHeight);

					final double dx = labelPath.getP2().getX() - labelPath.getP1().getX();
					final double dy = labelPath.getP2().getY() - labelPath.getP1().getY();
					final double branchLength = Math.sqrt(dx*dx + dy*dy);

					final Painter.Justification just = labelPath.getX1() < labelPath.getX2() ? Painter.Justification.LEFT :
							Painter.Justification.RIGHT;

					// Work out how it is rotated and create a transform that matches that
					AffineTransform labelTransform = calculateTransform(transform, labelPath, labelWidth, labelHeight, false);
					// move to middle of branch - since the move is before the rotation
					final double direction = just == Painter.Justification.RIGHT ? 1 : -1;
					labelTransform.translate(-direction * xScale * branchLength /2, 0);

					// Store the transformed bounds in the map for use when selecting
					branchLabelBounds.put(node, labelTransform.createTransformedShape(labelBounds));

					// Store the transform in the map for use when drawing
					branchLabelTransforms.put(node, labelTransform);

					// Store the alignment in the map for use when drawing
					branchLabelJustifications.put(node, just);
				}
			}
		}

		if (scaleBarPainter != null && scaleBarPainter.isVisible()) {
			scaleBarPainter.calibrate(g2, this);
			final double h1 = scaleBarPainter.getPreferredHeight();
			scaleBarBounds = new Rectangle2D.Double(treeBounds.getX(), height - h1, treeBounds.getWidth(), h1);
		}

		calloutPaths.clear();

		calibrated = true;
	}

	private AffineTransform calculateTransform(AffineTransform globalTransform, Line2D line, double width, double height, boolean just) {
		// Work out how it is rotated and create a transform that matches that
		AffineTransform lineTransform = new AffineTransform();

		final Point2D origin = line.getP1();
		if (globalTransform != null) {
			globalTransform.transform(origin, origin);
		}

		final double dx = line.getX2() - line.getX1();
		final double angle = dx != 0.0 ? Math.atan((line.getY2() - line.getY1()) / dx) : 0.0;
		lineTransform.rotate(angle, origin.getX(), origin.getY());

		// Now add a translate to the transform - if it is on the left then we need
		// to shift it by the entire width of the string.
		final double ty = origin.getY() - (height / 2.0);
		if (!just || line.getX2() > line.getX1()) {
			lineTransform.translate(origin.getX() + labelXOffset, ty);
		} else {
			lineTransform.translate(origin.getX() - (labelXOffset + width), ty);
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

	private RootedTree originalTree = null;
	private RootedTree tree = null;
	private TreeLayout treeLayout = null;

	private boolean orderBranchesOn = false;
	private SortedRootedTree.BranchOrdering branchOrdering = SortedRootedTree.BranchOrdering.INCREASING_NODE_DENSITY;

	private boolean transformBranchesOn = false;
	private TransformedRootedTree.Transform branchTransform = TransformedRootedTree.Transform.CLADOGRAM;

	private Rectangle2D treeBounds = new Rectangle2D.Double();
	private double treeScale;

	//private Insets margins = new Insets(6, 6, 6, 6);
	private Insets insets = new Insets(6, 6, 6, 6);

	private Set<Node> selectedNodes = new HashSet<Node>();
	private Set<Taxon> selectedTaxa = new HashSet<Taxon>();

	private double rulerHeight = -1.0;
	private Rectangle2D dragRectangle = null;

	private BranchDecorator branchDecorator = null;

	private float labelXOffset = 5.0F;
	private LabelPainter<Node> tipLabelPainter = null;
	private double tipLabelWidth;
	private LabelPainter<Node> nodeLabelPainter = null;
	private LabelPainter<Node> branchLabelPainter = null;

	private Painter<TreePane> scaleBarPainter = null;
	private Rectangle2D scaleBarBounds = null;

	private BasicStroke branchLineStroke = new BasicStroke(1.0F);
	private BasicStroke calloutStroke = new BasicStroke(0.5F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[]{0.5f, 2.0f}, 0.0f);
	private Stroke selectionStroke = new BasicStroke(6.0F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	private Paint selectionPaint;

	private boolean calibrated = false;
	private AffineTransform transform = null;

	private boolean showingRootBranch = true;
	private boolean showingTaxonCallouts = true;

	private Map<Taxon, AffineTransform> tipLabelTransforms = new HashMap<Taxon, AffineTransform>();
	private Map<Taxon, Shape> tipLabelBounds = new HashMap<Taxon, Shape>();
	private Map<Taxon, Painter.Justification> tipLabelJustifications = new HashMap<Taxon, Painter.Justification>();

	private Map<Node, AffineTransform> nodeLabelTransforms = new HashMap<Node, AffineTransform>();
	private Map<Node, Shape> nodeLabelBounds = new HashMap<Node, Shape>();
	private Map<Node, Painter.Justification> nodeLabelJustifications = new HashMap<Node, Painter.Justification>();

	private Map<Node, AffineTransform> branchLabelTransforms = new HashMap<Node, AffineTransform>();
	private Map<Node, Shape> branchLabelBounds = new HashMap<Node, Shape>();
	private Map<Node, Painter.Justification> branchLabelJustifications = new HashMap<Node, Painter.Justification>();

	private Map<Taxon, Shape> calloutPaths = new HashMap<Taxon, Shape>();

}
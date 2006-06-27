package jebl.gui.trees.treeviewer;

import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.SortedRootedTree;
import jebl.evolution.trees.TransformedRootedTree;
import jebl.gui.trees.treeviewer.decorators.BranchDecorator;
import jebl.gui.trees.treeviewer.painters.Painter;
import jebl.gui.trees.treeviewer.painters.PainterListener;
import jebl.gui.trees.treeviewer.treelayouts.TreeLayout;
import jebl.gui.trees.treeviewer.treelayouts.TreeLayoutListener;
import org.virion.jam.controlpanels.ControlPalette;
import org.virion.jam.controlpanels.Controls;
import org.virion.jam.controlpanels.ControlsProvider;
import org.virion.jam.controlpanels.ControlsSettings;
import org.virion.jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class TreePane extends JComponent implements ControlsProvider, PainterListener, Printable {

    public TreePane() {
        setBackground(UIManager.getColor("TextArea.background"));
    }

    public RootedTree getTree() {
        return tree;
    }

    public void setTree(RootedTree tree) {
        this.originalTree = tree;
        if (!originalTree.hasLengths()) {
            transformBranches = true;
        }
        setupTree();
    }

    private void setupTree() {
        tree = originalTree;

        if (orderBranches) {
            tree = new SortedRootedTree(tree, branchOrdering);
        }

        if (transformBranches || !this.tree.hasLengths()) {
            tree = new TransformedRootedTree(tree, branchTransform);
        }

        treeLayout.setTree(tree);

        calibrated = false;
        invalidate();
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
        if (controlPalette != null) controlPalette.fireControlsChanged();
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

    public void setBranchOrdering(boolean orderBranches, SortedRootedTree.BranchOrdering branchOrdering) {
        this.orderBranches = orderBranches;
        this.branchOrdering = branchOrdering;
        setupTree();
        PREFS.getBoolean(orderBranchesPREFSkey, orderBranches);
    }

    public void setBranchTransform(boolean transformBranches, TransformedRootedTree.Transform branchTransform) {
        this.transformBranches = transformBranches;
        this.branchTransform = branchTransform;
        setupTree();
        PREFS.putBoolean(transforNanchesPREFSkey, transformBranches);
    }

    public boolean isShowingRootBranch() {
        return showingRootBranch;
    }

    public void setShowingRootBranch(boolean showingRootBranch) {
        this.showingRootBranch = showingRootBranch;
        calibrated = false;
        repaint();
        PREFS.putBoolean(showRootPREFSkey, showingRootBranch);
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

    private boolean canSelectNode(Node selectedNode) {
       return selectedNode != null && !(tree.conceptuallyUnrooted() && selectedNode == tree.getRootNode());
    }
    public void addSelectedNode(Node selectedNode) {
        if ( canSelectNode(selectedNode) ) {
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
        if ( canSelectNode(selectedNode) ) {
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

    public void setTaxonLabelPainter(Painter<Node> taxonLabelPainter) {
        if (this.taxonLabelPainter != null) {
            this.taxonLabelPainter.removePainterListener(this);
        }
        this.taxonLabelPainter = taxonLabelPainter;
        if (this.taxonLabelPainter != null) {
            this.taxonLabelPainter.addPainterListener(this);
        }
        controlPalette.fireControlsChanged();
        calibrated = false;
        repaint();
    }

    public Painter<Node> getTaxonLabelPainter() {
        return taxonLabelPainter;
    }

    public void setNodeLabelPainter(Painter<Node> nodeLabelPainter) {
        if (this.nodeLabelPainter != null) {
            this.nodeLabelPainter.removePainterListener(this);
        }
        this.nodeLabelPainter = nodeLabelPainter;
        if (this.nodeLabelPainter != null) {
            this.nodeLabelPainter.addPainterListener(this);
        }
        controlPalette.fireControlsChanged();
        calibrated = false;
        repaint();
    }

    public Painter<Node> getNodeLabelPainter() {
        return nodeLabelPainter;
    }

    public void setBranchLabelPainter(Painter<Node> branchLabelPainter) {
        if (this.branchLabelPainter != null) {
            this.branchLabelPainter.removePainterListener(this);
        }
        this.branchLabelPainter = branchLabelPainter;
        if (this.branchLabelPainter != null) {
            this.branchLabelPainter.addPainterListener(this);
        }
        controlPalette.fireControlsChanged();
        calibrated = false;
        repaint();
    }

    public Painter<Node> getBranchLabelPainter() {
        return branchLabelPainter;
    }

    public void setScaleBarPainter(Painter<TreePane> scaleBarPainter) {
        if (this.scaleBarPainter != null) {
            this.scaleBarPainter.removePainterListener(this);
        }
        this.scaleBarPainter = scaleBarPainter;
        if (this.scaleBarPainter != null) {
            this.scaleBarPainter.addPainterListener(this);
        }
        controlPalette.fireControlsChanged();
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

    private void setBranchLineWeightValues(float weight) {
        branchLineStroke = new BasicStroke(weight);
        selectionStroke = new BasicStroke(Math.max(weight + 4.0F, weight * 1.5F), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        PREFS.putFloat(branchWeightPREFSkey, weight);
    }

    public void setBranchLineWeight(float weight) {
        setBranchLineWeightValues(weight);
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

    public void setRuler(double rulerHeight) {
        this.rulerHeight = rulerHeight;
    }

    public void scrollPointToVisible(Point point) {
        scrollRectToVisible(new Rectangle(point.x, point.y, 0, 0));
    }

    public void setControlPalette(ControlPalette controlPalette) {
        this.controlPalette = controlPalette;
    }

    private ControlPalette controlPalette = null;


    public List<Controls> getControls(boolean detachPrimaryCheckbox) {

        List<Controls> controlsList = new ArrayList<Controls>();

        controlsList.addAll(treeLayout.getControls(detachPrimaryCheckbox));

        if (controls == null) {
            OptionsPanel optionsPanel = new OptionsPanel();

            transformCheck = new JCheckBox("Transform branches");
            optionsPanel.addComponent(transformCheck);

            transformBranches = PREFS.getBoolean(transforNanchesPREFSkey, transformBranches);

            transformCheck.setSelected(transformBranches);
            if (!originalTree.hasLengths()) {
                transformCheck.setEnabled(false);
            }

            final JComboBox combo1 = new JComboBox(TransformedRootedTree.Transform.values());
            combo1.setSelectedItem(branchTransform);
            combo1.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent itemEvent) {
                    setBranchTransform(true,
                            (TransformedRootedTree.Transform) combo1.getSelectedItem());

                }
            });
            final JLabel label1 = optionsPanel.addComponentWithLabel("Transform:", combo1);
            label1.setEnabled(transformCheck.isSelected());
            combo1.setEnabled(transformCheck.isSelected());

            transformCheck.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    final boolean selected = transformCheck.isSelected();
                    label1.setEnabled(selected);
                    combo1.setEnabled(selected);

                    setBranchTransform(selected, (TransformedRootedTree.Transform) combo1.getSelectedItem());
                }
            });

            final JCheckBox checkBox2 = new JCheckBox("Order branches");
            optionsPanel.addComponent(checkBox2);

            orderBranches = PREFS.getBoolean(orderBranchesPREFSkey, orderBranches);
            checkBox2.setSelected(orderBranches);

            final JComboBox combo2 = new JComboBox(SortedRootedTree.BranchOrdering.values());
            combo2.setSelectedItem(branchOrdering);
            combo2.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent itemEvent) {
                    setBranchOrdering(true,
                            (SortedRootedTree.BranchOrdering) combo2.getSelectedItem());
                }
            });

            final JLabel label2 = optionsPanel.addComponentWithLabel("Ordering:", combo2);
            label2.setEnabled(checkBox2.isSelected());
            combo2.setEnabled(checkBox2.isSelected());

            checkBox2.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    label2.setEnabled(checkBox2.isSelected());
                    combo2.setEnabled(checkBox2.isSelected());

                    setBranchOrdering(checkBox2.isSelected(),
                            (SortedRootedTree.BranchOrdering) combo2.getSelectedItem());
                }
            });

            if( ! tree.conceptuallyUnrooted() ) {
                final JCheckBox checkBox3 = new JCheckBox("Show Root Branch");
                optionsPanel.addComponent(checkBox3);

                boolean showRootBranch = PREFS.getBoolean(showRootPREFSkey, isShowingRootBranch());
                checkBox3.setSelected(showRootBranch);
                checkBox3.addChangeListener(new ChangeListener() {
                    public void stateChanged(ChangeEvent changeEvent) {
                        setShowingRootBranch(checkBox3.isSelected());
                    }
                });
            } else {
                // no root for unrooted
                showingRootBranch = false;
            }

            final JSpinner spinner = new JSpinner(new SpinnerNumberModel(1.0, 0.01, 48.0, 1.0));

            final float weight = PREFS.getFloat(branchWeightPREFSkey, 1.0F);
            setBranchLineWeightValues(weight);
            spinner.setValue(weight);

            spinner.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    setBranchLineWeight(((Double) spinner.getValue()).floatValue());
                }
            });
            optionsPanel.addComponentWithLabel("Line Weight:", spinner);

            controls = new Controls("Formatting", optionsPanel, true);
        }
        controlsList.add(controls);

        if (getTaxonLabelPainter() != null) {
            controlsList.addAll(getTaxonLabelPainter().getControls(detachPrimaryCheckbox));
        }

        if (getNodeLabelPainter() != null) {
            controlsList.addAll(getNodeLabelPainter().getControls(detachPrimaryCheckbox));
        }

        if (getBranchLabelPainter() != null) {
            controlsList.addAll(getBranchLabelPainter().getControls(detachPrimaryCheckbox));
        }

        if (getScaleBarPainter() != null) {
            controlsList.addAll(getScaleBarPainter().getControls(detachPrimaryCheckbox));
        }

        return controlsList;
    }

    public void setSettings(ControlsSettings settings) {
        transformCheck.setSelected((Boolean) settings.getSetting("Transformed"));
    }

    public void getSettings(ControlsSettings settings) {
        settings.putSetting("Transformed", transformCheck.isSelected());
    }

    private JCheckBox transformCheck;

    private Controls controls = null;

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
        RenderingHints rhints = g2.getRenderingHints();
        boolean antialiasOn = rhints.containsValue(RenderingHints.VALUE_ANTIALIAS_ON);
        if( ! antialiasOn ) {
          g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }

        if (!calibrated) calibrate(g2, width, height);

        AffineTransform oldTransform = g2.getTransform();
        Paint oldPaint = g2.getPaint();
        Stroke oldStroke = g2.getStroke();
        Font oldFont = g2.getFont();

        Set<Node> externalNodes = tree.getExternalNodes();
        for (Node node : externalNodes) {

            Shape branchPath = transform.createTransformedShape(treeLayout.getBranchPath(node));
            Taxon taxon = tree.getTaxon(node);

            if (taxonLabelPainter != null && taxonLabelPainter.isVisible()) {

                AffineTransform taxonTransform = taxonLabelTransforms.get(taxon);
                Painter.Justification taxonLabelJustification = taxonLabelJustifications.get(taxon);
                g2.transform(taxonTransform);

                taxonLabelPainter.paint(g2, node, taxonLabelJustification,
                        new Rectangle2D.Double(0.0, 0.0, taxonLabelWidth, taxonLabelPainter.getPreferredHeight()));

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

        if( ! antialiasOn ) {
           g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        }
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

        if (taxonLabelPainter != null && taxonLabelPainter.isVisible()) {

            taxonLabelWidth = 0.0;

            // Find the longest taxon label
            for (Node node : externalNodes) {

                taxonLabelPainter.calibrate(g2, node);
                taxonLabelWidth = Math.max(taxonLabelWidth, taxonLabelPainter.getPreferredWidth());
            }

            final double labelHeight = taxonLabelPainter.getPreferredHeight();

            for (Node node : externalNodes) {
                // don't see why is that needed here? taxonLabelPainternot used in this loop (YH)?
                //taxonLabelPainter.calibrate(g2, node);
                Rectangle2D labelBounds = new Rectangle2D.Double(0.0, 0.0, taxonLabelWidth, labelHeight);

                // Get the line that represents the path for the taxon label
                Line2D taxonPath = treeLayout.getTaxonLabelPath(node);

                // Work out how it is rotated and create a transform that matches that
                AffineTransform taxonTransform = calculateTransform(null, taxonPath, taxonLabelWidth, labelHeight, true);

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

        final double avilableW = width - insets.left - insets.right;
        final double avaialbeH = height - insets.top - insets.bottom;

        // get the difference between the tree's bounds and the overall bounds

        double xDiff = bounds.getWidth() - treeBounds.getWidth();
        double yDiff = bounds.getHeight() - treeBounds.getHeight();
        assert xDiff >= 0 && yDiff >= 0;

        // small tree, long labels, label bounds may get larger that window, protect against that

        if( xDiff >= avilableW ) {
           xDiff = Math.min(avilableW, bounds.getWidth()) - treeBounds.getWidth();
        }

        if( yDiff >= avaialbeH ) {
           yDiff = Math.min(avaialbeH, bounds.getHeight()) - treeBounds.getHeight();
        }
        // Get the amount of canvas that is going to be taken up by the tree -
        // The rest is taken up by taxon labels which don't scale

        final double w = avilableW - xDiff;
        final double h = avaialbeH - yDiff;

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

            treeScale = xScale;   assert treeScale > 0;

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

            treeScale = xScale;   assert treeScale > 0;
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
        taxonLabelBounds.clear();
        taxonLabelTransforms.clear();
        taxonLabelJustifications.clear();

        if (taxonLabelPainter != null && taxonLabelPainter.isVisible()) {
            final double labelHeight = taxonLabelPainter.getPreferredHeight();
            Rectangle2D labelBounds = new Rectangle2D.Double(0.0, 0.0, taxonLabelWidth, labelHeight);

            // Iterate though the external nodes
            for (Node node : externalNodes) {
                Taxon taxon = tree.getTaxon(node);

                // Get the line that represents the path for the taxon label
                Line2D taxonPath = treeLayout.getTaxonLabelPath(node);

                // Work out how it is rotated and create a transform that matches that
                AffineTransform taxonTransform = calculateTransform(transform, taxonPath, taxonLabelWidth, labelHeight, true);

                // Store the transformed bounds in the map for use when selecting
                taxonLabelBounds.put(taxon, taxonTransform.createTransformedShape(labelBounds));

                // Store the transform in the map for use when drawing
                taxonLabelTransforms.put(taxon, taxonTransform);

                // Store the alignment in the map for use when drawing
                final Painter.Justification just = (taxonPath.getX1() < taxonPath.getX2()) ?
                        Painter.Justification.LEFT : Painter.Justification.RIGHT;
                taxonLabelJustifications.put(taxon, just);
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

    private boolean orderBranches = false;
    private SortedRootedTree.BranchOrdering branchOrdering = SortedRootedTree.BranchOrdering.INCREASING_NODE_DENSITY;

    private boolean transformBranches = false;
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
    private Painter<Node> taxonLabelPainter = null;
    private double taxonLabelWidth;
    private Painter<Node> nodeLabelPainter = null;
    private Painter<Node> branchLabelPainter = null;

    private Painter<TreePane> scaleBarPainter = null;
    private Rectangle2D scaleBarBounds = null;

    private Stroke branchLineStroke = new BasicStroke(1.0F);
    private Stroke taxonCalloutStroke = new BasicStroke(0.5F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[]{0.5f, 2.0f}, 0.0f);
    private Stroke selectionStroke = new BasicStroke(6.0F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private Paint selectionPaint = new Color(180, 213, 254);

    private boolean calibrated = false;
    private AffineTransform transform = null;

    private boolean showingRootBranch = true;
    private boolean showingTaxonCallouts = true;

    private Map<Taxon, AffineTransform> taxonLabelTransforms = new HashMap<Taxon, AffineTransform>();
    private Map<Taxon, Shape> taxonLabelBounds = new HashMap<Taxon, Shape>();
    private Map<Taxon, Painter.Justification> taxonLabelJustifications = new HashMap<Taxon, Painter.Justification>();

    private Map<Node, AffineTransform> nodeLabelTransforms = new HashMap<Node, AffineTransform>();
    private Map<Node, Shape> nodeLabelBounds = new HashMap<Node, Shape>();
    private Map<Node, Painter.Justification> nodeLabelJustifications = new HashMap<Node, Painter.Justification>();

    private Map<Node, AffineTransform> branchLabelTransforms = new HashMap<Node, AffineTransform>();
    private Map<Node, Shape> branchLabelBounds = new HashMap<Node, Shape>();
    private Map<Node, Painter.Justification> branchLabelJustifications = new HashMap<Node, Painter.Justification>();

    private Map<Taxon, Shape> calloutPaths = new HashMap<Taxon, Shape>();

    private String transforNanchesPREFSkey = "transformBranches";
    private String orderBranchesPREFSkey = "orderBranches";
    private String showRootPREFSkey = "showRootBranch";
    private String branchWeightPREFSkey = "branchWeight";
    private static Preferences PREFS = Preferences.userNodeForPackage(TreePane.class);
}
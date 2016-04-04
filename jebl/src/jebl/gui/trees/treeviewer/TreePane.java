package jebl.gui.trees.treeviewer;

import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.*;
import jebl.gui.trees.treeviewer.decorators.BranchDecorator;
import jebl.gui.trees.treeviewer.decorators.RgbBranchDecorator;
import jebl.gui.trees.treeviewer.painters.*;
import jebl.gui.trees.treeviewer.painters.Painter;
import jebl.gui.trees.treeviewer.treelayouts.RectilinearTreeLayout;
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
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.Preferences;

/**
 *
 * @author Andrew Rambaut
 * @version $Id$
 */
@SuppressWarnings({"unused", "ForLoopReplaceableByForEach", "ToArrayCallWithZeroLengthArrayArgument", "OverlyLongMethod"})
public class TreePane extends JComponent implements ControlsProvider, PainterListener, Printable {

    /**
     * Trees with more than this number of taxa are treated as "big" meaning that some behaviour changes for efficiency.
     */
    static final int BIG_TREE_TAXA_THRESHOLD = 200;

    public Point mouseLocation = new Point(0,0);
    private String referenceSequenceName;

    //Visibility and collapse variables
    public final static String KEY_INVISIBLE_NODE = "&visible"; //Node annotation qualifier set when the node isn't visible
    public final static String KEY_INVISIBLE_NODE_WHEN_AUTOCOLLAPSE = "&visibleInAutocollapse"; //Set when node is invisible due to autocollapse threshold
    public final static String KEY_MAX_DISTANCE_TO_DESCENDANT = "&KEY_MAX_DISTANCE_TO_DESCENDANT";
    private Set<Node> manuallyExpanded = new HashSet<Node>(); //Any node that the user has explicitly expanded by double clicking
    private Set<Node> manuallyCollapsed = new HashSet<Node>(); //Any node that the user has explicitly collapsed by double clicking

    //Filter variables
    private String filterText = "";
    private List<Node> externalNodesThatFitFilter = new ArrayList<Node>();
    private List<Node> internalNodesAboveFilterNodes = new ArrayList<Node>();
    private int currentFilterNodeIndex;
    private boolean isFiltering;

    /**
     * {@link Tree#getTaxa()} returns a defensive copy which is extremely slow at times so this variable will be used instead to
     * hold the number of taxa in the tree.
     * <p>
     * This variable is written to in the {@link TreePane#setTree(RootedTree, Collection)} method.
     */
    private int numberOfTaxa;

    public TreePane() {
        setBackground(UIManager.getColor("TextArea.background"));
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                mouseLocation = e.getPoint();
                repaint();
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                mouseLocation = null;
                repaint();
            }
        });
    }

    public RootedTree getTree() {
        return tree;
    }

    public void setTree(RootedTree tree, Collection<Node> selectedNodes) {
        this.originalTree = tree;
        if (!originalTree.hasLengths()) {
            transformBranches = true;
        }

        Painter<?>[] pl = { taxonLabelPainter, nodeLabelPainter, branchLabelPainter };
        for( Painter<?> p : pl ) {
            if( p instanceof BasicLabelPainter ) {
                ((BasicLabelPainter)p).setTree(tree);
            }
        }


        selectedTaxa.clear();
        if( selectedNodes != this.selectedNodes ) {
            this.selectedNodes.clear();
        }
        if( selectedNodes != null ) {
            this.selectedNodes.addAll(selectedNodes);
        }

        this.numberOfTaxa = tree.getTaxa().size();

        setupTree();
    }

    public void recalibrate(){
        calibrated = false;
        repaint();
    }

    private void setupTree() {
        tree = originalTree;

        if (orderBranches) {
            tree = new SortedRootedTree(tree, branchOrdering);
        }

        if (transformBranches || !this.tree.hasLengths()) {
            tree = new TransformedRootedTree(tree, branchTransform);
        }

        nodesInOrder = Utils.getNodes(tree, tree.getRootNode());
        treeLayout.setTree(tree);

        setDistancesToAncestorsForAutoExpansion();
        if (isFiltering) {
            setFilterText(filterText);
        }
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
        if (controlPalette != null) {
            controlPalette.fireControlsChanged();
        }
        calibrated = false;
        invalidate();
        repaint();
    }
//
//    public Rectangle2D getTreeBounds() {
//        return treeBounds;
//    }

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
        if( this.orderBranches != orderBranches || this.branchOrdering != branchOrdering ) {
            this.orderBranches = orderBranches;
            this.branchOrdering = branchOrdering;
            setupTree();
            getPrefs().getBoolean(orderBranchesPREFSkey, orderBranches);
        }
    }

    public void setBranchTransform(boolean transformBranches, TransformedRootedTree.Transform branchTransform) {
        if( transformBranches != this.transformBranches || branchTransform != this.branchTransform ) {
            this.transformBranches = transformBranches;
            this.branchTransform = branchTransform;
            setupTree();
            getPrefs().putBoolean(transformBanchesPREFSkey, transformBranches);
        }
    }

    /**
     * returns the currently applied branch transform or null if there is no transform applied
     * @return the currently applied branch transform or null if there is no transform applied
     */
    public TransformedRootedTree.Transform getBranchTransform() {
        if(transformBranches) {
            return branchTransform;
        }
        return null;
    }

    public boolean isShowingRootBranch() {
        return showingRootBranch;
    }

    public void setShowingRootBranch(boolean showingRootBranch) {
        if( this.showingRootBranch != showingRootBranch ) {
            this.showingRootBranch = showingRootBranch;
            calibrated = false;
            repaint();
            getPrefs().putBoolean(showRootPREFSkey, showingRootBranch);
        }
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

    public void setSelectedClade(Node[] selectedNode) {
        selectedNodes.clear();
        selectedTaxa.clear();
        addSelectedClade(selectedNode, true);
    }

    public void setSelectedTaxa(Node selectedNode) {
        selectedNodes.clear();
        selectedTaxa.clear();
        addSelectedTaxa(selectedNode);
    }

    public void setSelectedNodes(Set<Node> nodes) {
        selectedNodes.clear();
        selectedTaxa.clear();
        addSelectedNodes(nodes);
    }

    private void addSelectedNodes(Set<Node> nodes) {
        selectedNodes.addAll(nodes);
        fireSelectionChanged();
        repaint();
    }

    private boolean canSelectNode(final Node selectedNode) {
        return selectedNode != null && isNodeVisible(selectedNode);
    }

    public void addSelectedNode(Node selectedNode, boolean add) {
        if ( canSelectNode(selectedNode) ) {
            if( add ) {
                selectedNodes.add(selectedNode);
            } else {
                selectedNodes.remove(selectedNode);
            }
        }
        fireSelectionChanged();
        repaint();
    }

    public void addSelectedNode(Node selectedNode) {
       addSelectedNode(selectedNode, true);
    }

    public void addSelectedTaxon(Taxon selectedTaxon) {
        if (selectedTaxon != null) {
            selectedTaxa.add(selectedTaxon);
        }
        fireSelectionChanged();
        repaint();
    }

    /**
     *
     * @param selectedNode
     * @param add  true to add, false to remove existing selection
     */
    public void addSelectedClade(Node[] selectedNode, boolean add) {
        if ( canSelectNode(selectedNode[0]) ) {
            addSelectedChildClades(selectedNode, add);
        }

        if( viewSubtree ) calibrated = false;

        fireSelectionChanged();
        repaint();
    }

    public void addSelectedClades(Collection<Node> nodes, boolean add) {
        for (Node node : nodes) {
            if ( canSelectNode(node) ) {
                addSelectedChildClades(new Node[] {node, null}, add);
            }
        }
        if( viewSubtree ) calibrated = false;
        fireSelectionChanged();
        repaint();
    }

    private void addSelectedChildClades(Node[] selectedNode, boolean add) {
        if( selectedNode[1] == null ) {
            addSelectedChildClades(selectedNode[0], null, add);
        } else {
           addSelectedChildClades(tree.getRootNode(), selectedNode[0], add);
        }
    }

    private void addSelectedChildClades(Node selectedNode, Node exclude, boolean add) {
        if( selectedNode == exclude ) return;

        if( add ) {
            selectedNodes.add(selectedNode);
        } else {
            selectedNodes.remove(selectedNode);
        }

        for (Node child : tree.getChildren(selectedNode)) {
            addSelectedChildClades(child, exclude, add);
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
     * Checks visibility of a node by looking at its qualifiers. Does not take the view area into account
     * @param node
     * @return true if a node is visible given the current autocollapse state
     */
    private boolean isNodeVisible(Node node) {
        if (autoCollapseNodes) {
            return node.getAttribute(KEY_INVISIBLE_NODE_WHEN_AUTOCOLLAPSE) == null && node.getAttribute(KEY_INVISIBLE_NODE) == null;
        } else {
            return node.getAttribute(KEY_INVISIBLE_NODE) == null;
        }
    }

    /**
     * Check if a node should be drawn in the collapsed state. Presence in the manuallyCollapsed or manuallyExpanded lists
     * override the autocollapse state given by (autoCollapseNodes && isBelowCollapseDistanceThreshold(node))
     * @param node
     * @return
     */
    private boolean isNodeCollapsed(Node node) {
         return manuallyCollapsed.contains(node) || (autoCollapseNodes && isBelowCollapseDistanceThreshold(node) && !manuallyExpanded.contains(node));
    }

    /**
     * Check if the distance between a node and its furthest descendant is lower than the collapse threshold, meaning
     * this node should be autocollapsed. For tips, the parent is checked.
     *
     * @param node
     * @return
     */
    private boolean isBelowCollapseDistanceThreshold(Node node) {
        if (tree.isExternal(node)) return isBelowCollapseDistanceThreshold(tree.getParent(node));
        Object distanceToAncestor = node.getAttribute(KEY_MAX_DISTANCE_TO_DESCENDANT);
        double distance;
        if (distanceToAncestor != null && distanceToAncestor instanceof Double) {
            distance = (Double) distanceToAncestor;
            if (distance < cladeDistanceThresholdToCollapse) {
                return true;
            } else {
                return false;
            }
        } else {
            throw new IllegalArgumentException("Internal node encountered with no distance element");
        }
    }

    /**
     * Called when a user double clicks a node to expand or contract it.
     * @param selectedNode
     */
    public void manuallyToggleExpandContract(final Node selectedNode) {
        final boolean shouldExpand = isNodeCollapsed(selectedNode);
        manuallyToggleExpandContract(selectedNode, shouldExpand);
    }

    /**
     * Expands or contracts a node when a user double clicks it. This manual state over rides any auto-collapsed
     * state by modifying the manuallyExpanded and manuallyContracted lists.
     *
     * @param selectedNode
     * @param expand True if this node should be expanded
     */
    public void manuallyToggleExpandContract(final Node selectedNode, boolean expand) {
        if( expand )  { //it was collapsed earlier, so is now manually expanded
            manuallyCollapsed.remove(selectedNode);
            manuallyExpanded.add(selectedNode);

            //Get a list of all the parents in a direct line to the root and add them to manuallyExpanded
            //This lets a node stay expanded even when the nodes above it should be auto-collapsed
            List<Node> parentsToRoot = getParentsToRoot(selectedNode);
            for (Node node : parentsToRoot) {
                manuallyExpanded.add(node);
                manuallyCollapsed.remove(node);
            }
        } else { //it wasn't collapsed before, so is now manually collapsed
            manuallyExpanded.remove(selectedNode);
            manuallyCollapsed.add(selectedNode);
        }

        //Change the visibility of nodes downstream of the double-clicked one
        setCladeVisibility(selectedNode, expand);

        manualListChanged(); //updates a label in the subtree collapse options
        calibrated = false;
        repaint();
    }

    /**
     * When manually contracting or expanding a node, this method make the visibility modifications needed by the children
     * of that node.
     *
     * When manually expanding a node, each child in the subtree under it becomes visible (has all the invisibility
     * qualifiers removed and added to manuallyExpanded list).
     *
     * When collapsing a node, each child becomes invisible and is added to the manuallyCollapsed list. We don't set the
     * auto-invisible attribute because this is controlled by resetNodeVisibilities(), which is called whenever a setting
     * to do with the autocollapse system changes.
     *
     * @param node
     * @param isExpandingNode
     */
    private void setCladeVisibility(final Node node, final boolean isExpandingNode) {
        for( final Node child : tree.getChildren(node) ) {
            if( isExpandingNode ) {
                child.removeAttribute(KEY_INVISIBLE_NODE); //Parent was collapsed but now is expanding, child set to visible
                child.removeAttribute(KEY_INVISIBLE_NODE_WHEN_AUTOCOLLAPSE); //Parent was collapsed but now is expanding, child set to visible
                manuallyCollapsed.remove(child);
                manuallyExpanded.add(child); //This means expanding a node will expand all its children down to the leaves
            } else {
                child.setAttribute(KEY_INVISIBLE_NODE, Boolean.TRUE); //This makes the child INVISIBLE
                manuallyExpanded.remove(child); //if parent is being manually collapsed, remove its kids from this list.
                manuallyCollapsed.add(child);
            }

            setCladeVisibility(child, isExpandingNode);
        }
    }

    /**
     * Set a label in the collapsed subtree options that relies on the size of the manual lists
     */
    private void manualListChanged() {
        if (collapsedNodeLabelPainter != null) {
            collapsedNodeLabelPainter.setNumberManualNodes(manuallyExpanded.size() + manuallyCollapsed.size());
        }
    }

    /**
     * @param selectedNode
     * @return List of nodes in order from the selected node up to the root
     */
    private List<Node> getParentsToRoot(Node selectedNode) {
        ArrayList<Node> parentNodes = new ArrayList<Node>();
        Node currentParent = tree.getParent(selectedNode);
        while (currentParent != null) {
            parentNodes.add(currentParent);
            currentParent = tree.getParent(currentParent);
        }
        return parentNodes;
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


    /*
    Getters and setters for the label painters. When a painter is set we:
    - add this TreePane as a listener to it
    - collect the controls panel for the controlPalette
    - calibrate and repaint
     */

    public void setTaxonLabelPainter(BasicLabelPainter taxonLabelPainter) {
        setTaxonLabelPainter((Painter<Node>) taxonLabelPainter);
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

    public void setScaleBarPainter(ScaleBarPainter scaleBarPainter) {
        if (this.scaleBarPainter != null) {
            this.scaleBarPainter.removePainterListener(this);
        }
        this.scaleBarPainter = scaleBarPainter;
        if (this.scaleBarPainter != null) {
            this.scaleBarPainter.addPainterListener(this);
            scaleBarPainter.setEnabled(!transformCheck.isSelected());
        }
        controlPalette.fireControlsChanged();
        calibrated = false;
        repaint();
    }

    public Painter<Node> getCollapsedNodeLabelPainter() {
        return collapsedNodeLabelPainter;
    }

    public void setCollapsedNodeLabelPainter(CollapsedNodeLabelPainter collapsedNodeLabelPainter) {
        if (this.collapsedNodeLabelPainter != null) {
            this.collapsedNodeLabelPainter.removePainterListener(this);
        }
        this.collapsedNodeLabelPainter = collapsedNodeLabelPainter;
        if (this.collapsedNodeLabelPainter != null) {
            this.collapsedNodeLabelPainter.addPainterListener(this);
        }
        controlPalette.fireControlsChanged();
        calibrated = false;
        repaint();
    }

    public ScaleBarPainter getScaleBarPainter() {
        return scaleBarPainter;
    }

    public void setBranchDecorator(BranchDecorator branchDecorator) {
        this.branchDecorator = branchDecorator;
        calibrated = false;
        repaint();
    }

    public BranchDecorator getBranchDecorator() {
        return branchDecorator;
    }

    private boolean setBranchLineWeightValues(float weight) {
        if( ((BasicStroke)branchLineStroke).getLineWidth() != weight ) {
            branchLineStroke = new BasicStroke(weight);
            getPrefs().putFloat(branchWeightPREFSkey, weight);
            return true;
        }
        return false;
    }

    public void setBranchLineWeight(float weight) {
        if( setBranchLineWeightValues(weight) ) {
          repaint();
        }
    }

    public float getBranchLineWeight() {
        if(branchLineStroke instanceof BasicStroke)
            return ((BasicStroke)branchLineStroke).getLineWidth();
        return 1.0f;
    }

    public void setPreferredSize(Dimension dimension) {
        if (treeLayout.maintainAspectRatio()) {
            super.setPreferredSize(new Dimension(dimension.width, dimension.height));
        } else {
            super.setPreferredSize(dimension);
        }
        //taxonLabelPainter.resetFontSizes(false); //triggers a resize of the label fonts
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


    final private int circDiameter = 9;

    /**
     * Return the node from the candidate list that is at the given point or null if no node is at the point
     */
    Node getNodeAt(Point point, Graphics2D g2) {
        if(point == null) {
            return null;
        }

        List<Node> nodes = new ArrayList<Node>();
        nodes.addAll(tree.getExternalNodes());
        for (Node node : nodesInOrder) {
            if (!tree.isExternal(node)) {
                nodes.add(node);
            }
        }
        nodes.add(tree.getRootNode());

        if(flipTree) {
            point = new Point(getWidth()-point.x, point.y);
        }

        if (g2 == null) g2 = (Graphics2D)getGraphics();


        Rectangle rect = new Rectangle(point.x - 1, point.y - 1, 3, 3);

        for (int i = nodes.size() - 1; i >= 0; i--) {
            Node node = nodes.get(i);
            if(  isNodeVisible(node) && !hideNode(node) && checkNodeIntersects(node, point)) {
                return node;
            }
        }

        for( TreeDrawableElement e : treeElements ) {
            if (e instanceof TreeDrawableElementNodeLabel) {
                TreeDrawableElementNodeLabel nodeLabel = (TreeDrawableElementNodeLabel) e;
                if (!"branch".equals(nodeLabel.dtype)) {
                    Node node = e.getNode();
                    if( node != null ) {
                        if( e.hit(g2, rect) ) {
                           return node;
                        }
                    }
                }
            }
        }
        return null;
    }

    private Shape getNodeMarker(Node node, int d) {
        final Point2D.Double coord = nodeCoord(node);
        final int r = (d-1)/2;

        return new Ellipse2D.Float((float)coord.x - r, (float)coord.y - r, d, d);
    }

    private boolean checkNodeIntersects(Node node, Point point){
        Shape nodeMarker = getNodeMarker(node, circDiameter+6);
        return nodeMarker.contains(point);
    }

    /**
     * This is used for calculating which nodes are selected by dragging
     * (with a selection rectangle)
     * @param g2
     * @param rect
     * @return
     */
    Set<Node> getNodesAt(Graphics2D g2, Rectangle rect) {

        Set<Node> nodes = new HashSet<Node>();

        for( TreeDrawableElement e : treeElements ) {
            Node node = e.getNode();
            if( node != null ) {
                if( e.hit(g2, rect) ) {
                    nodes.add(node);
                }
            }
        }

        Node[] allNodes = tree.getNodes().toArray(new Node[0]);
        for(int i=allNodes.length-1; i >= 0; i--){
            if(rect.contains(transform.transform(treeLayout.getNodePoint(allNodes[i]),null))){
                nodes.add(allNodes[i]);
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
        Rectangle2D newDragRect = dragRectangle;

        if(flipTree && dragRectangle != null) {
            AffineTransform tr = new AffineTransform();
            tr.scale(-1,1);
            tr.translate(-getWidth(),0);
            newDragRect = tr.createTransformedShape(dragRectangle).getBounds2D();
        }
        this.dragRectangle = newDragRect;
        repaint();
    }

    /**
     *
     * @deprecated TreePane doesn't support rulers anymore, calling this method does nothing.
     */
    @Deprecated
    public void setRuler(double rulerHeight) {}

    public void scrollPointToVisible(Point point) {
        scrollRectToVisible(new Rectangle(point.x, point.y, 0, 0));
    }

    public void setControlPalette(ControlPalette controlPalette) {
        this.controlPalette = controlPalette;
    }

    private ControlPalette controlPalette = null;

    private JCheckBox autoEx;

    public List<Controls> getControls(boolean detachPrimaryCheckbox) {

        List<Controls> controlsList = new ArrayList<Controls>();

        controlsList.addAll(treeLayout.getControls(detachPrimaryCheckbox));

        if (controls == null) {
            Preferences prefs = getPrefs();
            OptionsPanel optionsPanel = new OptionsPanel();

            flipCheck = new JCheckBox("Flip the tree horizontally");
            optionsPanel.addComponent(flipCheck);

            transformCheck = new JCheckBox("Transform branches");
            optionsPanel.addComponent(transformCheck);

            flipTree = prefs.getBoolean(flipTreePREFSkey, flipTree);
            flipCheck.setSelected(flipTree);

            flipCheck.addChangeListener(new ChangeListener(){
                public void stateChanged(ChangeEvent e) {
                    flipTree = flipCheck.isSelected();

                    getPrefs().putBoolean(flipTreePREFSkey, flipTree);
                    repaint();
                }
            });

            transformBranches = prefs.getBoolean(transformBanchesPREFSkey, transformBranches);

            transformCheck.setSelected(transformBranches);
            if (!originalTree.hasLengths()) {
                transformCheck.setEnabled(false);
                transformCheck.setSelected(false);
                transformBranches = false;
            }

            final JComboBox combo1 = new JComboBox(TransformedRootedTree.Transform.values()) {
                @Override
                public void setSelectedIndex(int anIndex) {
                    //GEN-7724: Java 6 under mac os: if a JComboBox contains items that are not Strings it will deselect if you click
                    //the already selected index, meaning null is selected and index -1 is selected. We prevent this here because you
                    //shouldn't have a combo box with nothing selected
                    if (anIndex == -1 && !isEditable()) {
                        System.err.println("Prevented index -1 from being selected in GComboBox");
                        return;
                    }
                    super.setSelectedIndex(anIndex);
                }
            };
            combo1.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent itemEvent) {
                    getPrefs().putInt(branchTransformTypePREFSkey, combo1.getSelectedIndex());
                    final TransformedRootedTree.Transform transform = (TransformedRootedTree.Transform) combo1.getSelectedItem();
                    setBranchTransform(transformCheck.isSelected(), transform);
                }
            });
            int index = prefs.getInt(branchTransformTypePREFSkey, 0);
            if(index >= combo1.getItemCount()) { //added to stop dodgy values from preferences causing crashes
                index = combo1.getItemCount()-1;
            }
            combo1.setSelectedIndex(index);
            branchTransform = (TransformedRootedTree.Transform) combo1.getSelectedItem();
            final JLabel label1 = optionsPanel.addComponentWithLabel("Transform:", combo1);
            label1.setEnabled(transformCheck.isSelected());
            combo1.setEnabled(transformCheck.isSelected());

            transformCheck.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    final boolean selected = transformCheck.isSelected();
                    // only on a real change
                    label1.setEnabled(selected);
                    combo1.setEnabled(selected);
                    scaleBarPainter.setEnabled(!selected);

                    setBranchTransform(selected, (TransformedRootedTree.Transform) combo1.getSelectedItem());
                }
            });

            final JCheckBox checkBox2 = new JCheckBox("Order branches");
            optionsPanel.addComponent(checkBox2);

            orderBranches = prefs.getBoolean(orderBranchesPREFSkey, orderBranches);
            checkBox2.setSelected(orderBranches);

            final JComboBox combo2 = new JComboBox(SortedRootedTree.BranchOrdering.values()) {
                @Override
                public void setSelectedIndex(int anIndex) {
                    //GEN-7724: Java 6 under mac os: if a JComboBox contains items that are not Strings it will deselect if you click
                    //the already selected index, meaning null is selected and index -1 is selected. We prevent this here because you
                    //shouldn't have a combo box with nothing selected
                    if (anIndex == -1 && !isEditable()) {
                        System.err.println("Prevented index -1 from being selected in GComboBox");
                        return;
                    }
                    super.setSelectedIndex(anIndex);
                }
            };
            combo2.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent itemEvent) {
                    if (orderBranches) {
                        setBranchOrdering(true, (SortedRootedTree.BranchOrdering) combo2.getSelectedItem());
                        getPrefs().putInt(branchOrderingPREFSkey, combo2.getSelectedIndex());
                    }
                }
            });
            combo2.setSelectedIndex(prefs.getInt(branchOrderingPREFSkey, 0));

            final JLabel label2 = optionsPanel.addComponentWithLabel("Ordering:", combo2);
            label2.setEnabled(checkBox2.isSelected());
            combo2.setEnabled(checkBox2.isSelected());

            checkBox2.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    label2.setEnabled(checkBox2.isSelected());
                    combo2.setEnabled(checkBox2.isSelected());

                    setBranchOrdering(checkBox2.isSelected(),
                            (SortedRootedTree.BranchOrdering) combo2.getSelectedItem());
                    getPrefs().putBoolean(orderBranchesPREFSkey, orderBranches);
                }
            });

            if( ! tree.conceptuallyUnrooted() ) {
                final JCheckBox checkBox3 = new JCheckBox("Show Root Branch");
                optionsPanel.addComponent(checkBox3);

                showingRootBranch = prefs.getBoolean(showRootPREFSkey, isShowingRootBranch());
                checkBox3.setSelected(showingRootBranch);
                checkBox3.addChangeListener(new ChangeListener() {
                    public void stateChanged(ChangeEvent changeEvent) {
                        setShowingRootBranch(checkBox3.isSelected());
                    }
                });
            } else {
                // no root for unrooted
                showingRootBranch = false;
            }

            final JSpinner spinner = new JSpinner(new SpinnerNumberModel(new Float(1.0), new Float(0.01), new Float(48), new Float(1.0)));

            final float weight = prefs.getFloat(branchWeightPREFSkey, 1.0F);
            setBranchLineWeightValues(weight);
            spinner.setValue(weight);

            spinner.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    setBranchLineWeight((Float)spinner.getValue());
                }
            });
            optionsPanel.addComponentWithLabel("Line Weight:", spinner);

            final JCheckBox subTreeShowJB = new JCheckBox("Show selected subtree only");
            subTreeShowJB.setToolTipText("Only the selected part of the tree is shown");
            viewSubtree = prefs.getBoolean(viewSubtreePREFSkey, false);
            subTreeShowJB.setSelected(viewSubtree);
            subTreeShowJB.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    final boolean b = subTreeShowJB.isSelected();
                    getPrefs().putBoolean(viewSubtreePREFSkey, subTreeShowJB.isSelected());
                    if( viewSubtree != b ) {
                        viewSubtree = b;
                        calibrated = false;
                        repaint();
                    }
                }
            });

            optionsPanel.addComponent(subTreeShowJB);

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

        if (getCollapsedNodeLabelPainter() != null) {
            controlsList.addAll(getCollapsedNodeLabelPainter().getControls(detachPrimaryCheckbox));
        }

        if (getScaleBarPainter() != null) {
            controlsList.addAll(getScaleBarPainter().getControls(detachPrimaryCheckbox));
        }

        setupTree();
        return controlsList;
    }

    public void setSettings(ControlsSettings settings) {
        transformCheck.setSelected((Boolean) settings.getSetting("Transformed"));
    }

    public void getSettings(ControlsSettings settings) {
        settings.putSetting("Transformed", transformCheck.isSelected());
    }

    private JCheckBox transformCheck;

    private JCheckBox flipCheck;

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
        resetNodeVisibilities();
    }

    public void paint(Graphics graphics) {
        if (tree == null) return;

        final Graphics2D g2 = (Graphics2D) graphics;
        if (!calibrated) calibrate(g2, getWidth(), getHeight());

        final Paint oldPaint = g2.getPaint();
        final Stroke oldStroke = g2.getStroke();

        long start = System.currentTimeMillis();
        drawTree(g2, true, true, true, getWidth(), getHeight());
//        System.err.println("tree draw " + (System.currentTimeMillis() - start) + "ms");

        if (dragRectangle != null) {
            g2.setPaint(new Color(128, 128, 128, 128));
            g2.fill(dragRectangle);

            g2.setStroke(new BasicStroke(2.0F));
            g2.setPaint(new Color(255, 255, 255, 128));
            g2.draw(dragRectangle);

            g2.setPaint(oldPaint);
            g2.setStroke(oldStroke);
        }
        treePainted.set(true);
    }

    private AtomicBoolean treePainted = new AtomicBoolean(false);

    /**
     * Introduced for testing purposes. Returns true after the tree has finished painting.
     * Call {@link TreePane#setTreePainted(boolean)} before painting the frame you want to measure for this to
     * be accurate.
     * @return true when the tree has finished painting
     */
    public boolean isTreePainted() {
        return treePainted.get();
    }

    /**
     * @see TreePane#isTreePainted()
     *
     * @param treePainted
     */
    public void setTreePainted(boolean treePainted) {
        this.treePainted.set(treePainted);
    }

    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {

        if (tree == null || pageIndex > 0) return NO_SUCH_PAGE;

        Graphics2D g2 = (Graphics2D) graphics;
        g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

        calibrated = false;
        setDoubleBuffered(false);

        drawTree(g2, false, false, true, true, pageFormat.getImageableWidth(), pageFormat.getImageableHeight(), true);

        setDoubleBuffered(true);
        calibrated = false;

        return PAGE_EXISTS;
    }

    private Point2D.Double nodeCoord(final Node node) {
        final Point2D nodePoint = treeLayout.getNodePoint(node);
        final Point2D.Double result = new Point2D.Double();
        transform.transform(nodePoint, result);
        return result;
    }

    /**
     * Draws the node's marker
     * @param g2 the graphics
     * @param node the node
     * @param alwaysDrawNodeMarkers whether or not to always draw node markers
     * @param forceAntiAliasing whether or not to force anti-aliasing
     */
    private void nodeMarker(Graphics2D g2, Node node, boolean alwaysDrawNodeMarkers, boolean forceAntiAliasing) {
        final boolean isSelected = selectedNodes.contains(node);
        final Paint color = g2.getPaint();
        final boolean antiAliasingWasOff = !g2.getRenderingHints().containsValue(RenderingHints.VALUE_ANTIALIAS_ON);
        final boolean wantAntiAliasing = numberOfTaxa <= BIG_TREE_TAXA_THRESHOLD;

        if(forceAntiAliasing || (antiAliasingWasOff && wantAntiAliasing)) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        } else if(!wantAntiAliasing) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        }

        Shape nodeMarker = getNodeMarker(node, circDiameter);

        //we want to draw node markers either if they are selected, or if the mouse is close to them
        if(!alwaysDrawNodeMarkers && !isSelected)
            return;

        //we want them to have the selected colour either if they are selected, or if the mouse is over them
        Paint c = isSelected ? selectionPaint :  Color.LIGHT_GRAY;
        g2.setPaint(c);

        g2.fill(nodeMarker);
        g2.setColor(Color.black);
        g2.draw(nodeMarker);
        g2.setPaint(color);
        if(antiAliasingWasOff) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        } else {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
    }

    boolean viewSubtree;

    private boolean hideNode(Node node) {
        return viewSubtree && selectedNodes.size() > 0 && !selectedNodes.contains(node);
    }

    /**
     *
     * @param g2 the graphics to draw on to
     * @param drawNodes prints circles at nodes if true
     * @param clipOffscreenShapes only draws elements which fall in the current viewport if true (should always be false for printing)
     * @param width the width of the tree
     * @param height the height of the tree
     */
    public void drawTree(Graphics2D g2, boolean drawNodes, boolean clipOffscreenShapes, double width, double height) {
        drawTree(g2, drawNodes, clipOffscreenShapes, false, width, height);
    }

    /**
     *
     * @param g2 the graphics to draw on to
     * @param drawNodes prints circles at nodes and tips if true
     * @param clipOffscreenShapes only draws elements which fall in the current viewport if true (should always be false for printing)
     * @param drawOnlyVisibleElements tries to stop elements overlapping by not drawing some of them if true
     * @param width the width of the tree
     * @param height the height of the tree
     */
    public void drawTree(Graphics2D g2, boolean drawNodes, boolean clipOffscreenShapes, boolean drawOnlyVisibleElements, double width, double height) {
        drawTree(g2, drawNodes, clipOffscreenShapes, drawOnlyVisibleElements, false, width, height, false);
    }

    public Rectangle2D getScaleBarBounds() {
        return scaleBarBounds;
    }

    /**
     * Draws all node markers, branches, and treeElements (labels) in the tree. Called whenever paint() or repaint() is called.
     *
     * @param g2 the graphics to draw on to
     * @param drawNodes prints circles at nodes and tips if true
     * @param clipOffscreenShapes only draws elements which fall in the current viewport if true (should always be false for printing)
     * @param drawOnlyVisibleElements tries to stop elements overlapping by not drawing some of them if true
     * @param width the width of the tree
     * @param height the height of the tree
     * @param isPrinting if drawTree is called in order to print the tree
     */
    public void drawTree(Graphics2D g2, boolean drawNodes, boolean clipOffscreenShapes, boolean drawOnlyVisibleElements, boolean drawAllNodeMarkers, double width, double height, boolean isPrinting) {

        if(flipTree) {
            g2.scale(-1.0,1);
            g2.translate(-getWidth(),0);
        }
        if(nodeLabelPainter != null)
            nodeLabelPainter.setPaintAsMirrorImage(flipTree);

        if(taxonLabelPainter != null)
            taxonLabelPainter.setPaintAsMirrorImage(flipTree);

        if(branchLabelPainter != null)
            branchLabelPainter.setPaintAsMirrorImage(flipTree);

        if(scaleBarPainter != null)
            scaleBarPainter.setPaintAsMirrorImage(flipTree);

        if(collapsedNodeLabelPainter != null)
            collapsedNodeLabelPainter.setPaintAsMirrorImage(flipTree);

        // this is a problem since paint draws some stuff before which print does not
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, (int)width, (int)height);

        final RenderingHints rhints = g2.getRenderingHints();
        final boolean antialiasOn = rhints.containsValue(RenderingHints.VALUE_ANTIALIAS_ON);
        // anti-aliasing slows things down a lot on non-rectilinear layouts.
        // using clipOffScreenShapes in the hopes the isPrinting parameter can be deprecated in future
        // seeing as clipOffScreenShapes is always false when printing
        final boolean wantAntiAliasing = !clipOffscreenShapes || treeLayout instanceof RectilinearTreeLayout || numberOfTaxa <= BIG_TREE_TAXA_THRESHOLD;

        if( ! antialiasOn && wantAntiAliasing) {
          g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }

        if (!calibrated) calibrate(g2, width, height);

         // save graphics state which draw changes so that upon exit it can be restored

        final AffineTransform oldTransform = g2.getTransform();
        final Paint oldPaint = g2.getPaint();
        final Stroke oldStroke = g2.getStroke();
        final Font oldFont = g2.getFont();

        final Set<Node> externalNodes = tree.getExternalNodes();
        final boolean showingTaxonLables = taxonLabelPainter != null && taxonLabelPainter.isVisible();

        /**
         * Loops through external nodes, drawing their branches and node markers if they are visible
         */
        for (Node node : externalNodes) {
            if( !isNodeVisible(node) ) continue;
            if( hideNode(node) ) continue;

            final Shape branchPath = transform.createTransformedShape(treeLayout.getBranchPath(node));

            if (clipOffscreenShapes && !branchPath.intersects(g2.getClipBounds())) {
                continue;
            }

            Paint paint = (branchDecorator != null) ? branchDecorator.getBranchPaint(tree, node) : Color.BLACK;

            //If there is any search/filter text, draw all the branches as gray
            if (isFiltering) {
                paint = Color.LIGHT_GRAY;
            }

            g2.setPaint(paint);

            g2.setStroke(branchLineStroke);
            if (!treeLayout.shouldAntialiasBranchPath()) {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            }
            g2.draw(branchPath);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if(drawNodes)
                nodeMarker(g2, node, drawAllNodeMarkers, !clipOffscreenShapes);
        }

        final Node rootNode = tree.getRootNode();
        final boolean nodesLables = nodeLabelPainter != null && nodeLabelPainter.isVisible();
        final boolean branchLables = branchLabelPainter != null && branchLabelPainter.isVisible();

        /**
         * Loop through all the internal nodes, drawing node markers and branches
         */
        for(int nn = nodesInOrder.size()-1; nn >= 0; --nn) {
            final Node node = nodesInOrder.get(nn);

            if (showingRootBranch || node != rootNode) {
                if( !isNodeVisible(node) ) continue;
                if( hideNode(node) ) continue;
                //If a node is collapsed and it's parent is collapsed, don't draw any branches or nodes. The top collapsed node in a subtree has its branch drawn
                if ( !tree.isRoot(node) && isNodeCollapsed(node) && isNodeCollapsed(tree.getParent(node))) continue;
                if( !tree.isExternal(node) ) {
                    final Shape branchPath = transform.createTransformedShape(treeLayout.getBranchPath(node));
                    g2.setStroke(branchLineStroke);

                    Paint paint =
                            branchDecorator != null ? branchDecorator.getBranchPaint(tree, node) : Color.BLACK;

                    //If there is any search text, draw branches as gray
                    if (isFiltering) {
                        paint = Color.LIGHT_GRAY;
                    }

                    g2.setPaint(paint);

                    //although this fix is only an if != null check, this is ok because the missing node is a root node, which should not be drawn on an unrooted view anyway
                    if(branchPath == null)
                        continue;

                    if (clipOffscreenShapes && !branchPath.intersects(g2.getClipBounds())) {
                        continue;
                    }

                    if (!treeLayout.shouldAntialiasBranchPath()) {
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                    }
                    g2.draw(branchPath);
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    if(drawNodes)
                        nodeMarker(g2, node, drawAllNodeMarkers, !clipOffscreenShapes);
                }
            }
        }

        drawLabelElements(g2, clipOffscreenShapes, drawOnlyVisibleElements);

        if( ! hideNode(rootNode) && drawNodes) {
            g2.setStroke(branchLineStroke);
            nodeMarker(g2, rootNode, drawAllNodeMarkers, !clipOffscreenShapes);
        }

        if (mouseLocation != null) {
            Node nodeAt = getNodeAt(mouseLocation, g2);
            if (nodeAt != null) {
                nodeMarker(g2, nodeAt, true, !clipOffscreenShapes);
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

    /**
     * Draws all the taxa, branch and node labels.
     */
    private void drawLabelElements(Graphics2D g2, boolean clipOffscreenShapes, boolean drawOnlyVisibleElements) {
        boolean antiAliasingWasOn = g2.getRenderingHints().containsValue(RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Rectangle viewRect = clipOffscreenShapes ? viewport.getViewRect() : null;
        if(flipTree && viewRect != null) {
            viewRect.translate(getWidth()-2*viewRect.x-viewRect.width,0);
        }
        if (isShowLabels(drawOnlyVisibleElements, viewRect)) {
            showTooManyLabelsWarning(false);
            for( TreeDrawableElement label : treeElements ) {
                if((label.isVisible() || !drawOnlyVisibleElements)){
                    if (showingTaxonCallouts) {
                        final Shape calloutPath = transform.createTransformedShape(treeLayout.getCalloutPath(label.getNode()));
                        if (calloutPath != null) {
                            g2.setStroke(branchLineStroke);
                            g2.setColor(new Color(220,220,220));
                            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
                            g2.draw(calloutPath);
                            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        }
                    }
                    label.draw(g2, viewRect);
                }
            }
        } else {
            showTooManyLabelsWarning(true);
        }
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, (antiAliasingWasOn) ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    private boolean isShowLabels(boolean drawOnlyVisibleElements, Rectangle viewRect) {
        // check if there are too many labels to be shown effectively
        int count = 0;
        final int maxCount = 1000;
        if (viewRect != null) {
            for( TreeDrawableElement label : treeElements ) {
                if((label.isVisible() || !drawOnlyVisibleElements)) {
                    if (label.getBounds().intersects(viewRect)) {
                        if (++count == maxCount) {
                            break;
                        }
                    }
                }
            }
        }
        return count < maxCount;
    }

    private void showTooManyLabelsWarning(boolean showWarning) {
        for (Painter painter : new Painter[]{taxonLabelPainter, nodeLabelPainter, branchLabelPainter, collapsedNodeLabelPainter}) {
            if (painter instanceof BasicLabelPainter) {
                ((BasicLabelPainter) painter).showTooManyLabelsWarning(showWarning);
            }
        }
    }

    /**
     * Uses the state of the viewer to set the bounds objects of the tree, the branches,
     * and the labels that need to be drawn. Calculates the affine transform that needs to be applied to the
     * label bounds before saving them. Then creates all the labels (treeElements) that need to be painted
     * on the tree.
     *
     * @param g2
     * @param width
     * @param height
     */
    public void calibrate(Graphics2D g2, double width, double height) {
        long start = System.currentTimeMillis();

        // First of all get the bounds for the unscaled tree
        Rectangle2D treeBounds = null;
        Set<Node> externalNodes = tree.getExternalNodes();
        final Node rootNode = tree.getRootNode();
        checkAndSetNewAutoCollapseVariables();

        // bounds on branches
        for (Node node : tree.getNodes()) {
            if( hideNode(node) )  continue;
            // no root branch for unrooted trees
            if( !(tree.conceptuallyUnrooted() && (node == rootNode)) ) {
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

        if(treeBounds == null){
            assert false;
            if(calibrated)
                return;
            treeBounds = new Rectangle2D.Double(0,0,100,100); //this is here so the treeViewer draws somethihng...
        }

        // oldScaleCode too
        final Rectangle2D bounds = treeBounds.getBounds2D(); // (JH) same as (Rectangle2D) treeBounds.clone();

        double scaleHeight = 0;
        if (scaleBarPainter != null && scaleBarPainter.isVisible()) {
            scaleBarPainter.calibrate(g2);
            scaleHeight = scaleBarPainter.getPreferredHeight(g2, this);
        }

        // area available for drawing
        final double availableW = width - (insets.left + insets.right);
        final double availableH = height - (insets.top + insets.bottom + scaleHeight);

        TreeBoundsHelper treeBoundsHelper =
                new TreeBoundsHelper(externalNodes.size() + 2*tree.getNodes().size(), availableW, availableH,
                        treeBounds);
        calibrateTreeBoundsForLabels(g2, treeBoundsHelper);
        double xScale = scaleTreeReturningXScale(width, height, availableW, availableH, scaleHeight, treeBounds, treeBoundsHelper);
        createTreeDrawableElements(g2, externalNodes, xScale);
        List<TreeDrawableElement> taxonLabels = getTaxonLabels(g2, externalNodes);

        calibrateForScaleBarPainter(g2, height, treeBounds);

//        long now = System.currentTimeMillis();
        /**
         * Checks for label collisions and sets visibility based on that. Very complex right now -
         * returns w/o doing anything if given a large list of elements
         */
        TreeDrawableElement.setOverlappingVisiblitiy(treeElements, g2);
        TreeDrawableElement.setOverlappingVisiblitiy(taxonLabels, g2);
//        System.err.println("Clash " + (System.currentTimeMillis() - now));

        //this block of code makes sure that all labels are the same size
        //so that users don't think that some labels are more important than others
        float size = Float.MAX_VALUE;
        for(TreeDrawableElement element : taxonLabels){
            if(element.getCurrentSize() < size){
                size = element.getCurrentSize();
                //taxonLabelPainter.setFontSize(size, false);
                //calibrated = false;
            }
        }
        for(TreeDrawableElement element : taxonLabels){
            element.setSize((int)size,g2);
        }

        treeElements.addAll(taxonLabels);

        checkIfPaintingLabelsAndReScaleTreeIfNot(width, height, treeBounds, externalNodes, scaleHeight, availableW, availableH);

        calibrated = true;
//        System.err.println("Calibrate " + (System.currentTimeMillis() - start));
    }

    /**
     * Goes through each label that could be drawn on to the tree, getting their size and adding their bounds
     * to the treeBoundsHelper
     *
     * @param g2
     * @param treeBoundsHelper
     */
    private void calibrateTreeBoundsForLabels(Graphics2D g2, TreeBoundsHelper treeBoundsHelper) {
        Set<Node> externalNodes = tree.getExternalNodes();

        if (taxonLabelPainter != null && taxonLabelPainter.isVisible()) {

            for (Node node : externalNodes) {
                if( hideNode(node) || !isNodeVisible(node)) continue;
                taxonLabelPainter.calibrate(g2);
                taxonLabelWidth = taxonLabelPainter.getWidth(g2, node);

                // Get the line that represents the orientation for the taxon label
                final Line2D taxonPath = treeLayout.getTaxonLabelPath(node);
                double labelHeight = taxonLabelPainter.getPreferredHeight(g2, node);

                //System.out.println("For " + tree.getTaxon(node).getName())
                treeBoundsHelper.addBounds(taxonPath, labelHeight, labelXOffset + taxonLabelWidth, false);
            }
        }

        if (nodeLabelPainter != null && nodeLabelPainter.isVisible()) {

            for( Node node : tree.getNodes() ) {
                if( hideNode(node) || !isNodeVisible(node)) continue;

                // Get the line that represents the label orientation
                final Line2D labelPath = treeLayout.getNodeLabelPath(node);

                if (labelPath != null) {
                    nodeLabelPainter.calibrate(g2);
                    final double labelHeight = nodeLabelPainter.getPreferredHeight(g2, node);
                    final double labelWidth = nodeLabelPainter.getWidth(g2, node);

                    treeBoundsHelper.addBounds(labelPath, labelHeight, labelXOffset + labelWidth, false);
                }
            }
        }

        if (branchLabelPainter != null && branchLabelPainter.isVisible()) {
            // Iterate though the nodes
            for (Node node : tree.getNodes()) {
                if( hideNode(node) || !isNodeVisible(node)) continue;

                // Get the line that represents the path for the branch label
                final Line2D labelPath = treeLayout.getBranchLabelPath(node);

                if (labelPath != null) {
                    branchLabelPainter.calibrate(g2);
                    final double labelHeight = branchLabelPainter.getHeightBound(g2, node);
                    final double labelWidth = branchLabelPainter.getWidth(g2, node);

                    treeBoundsHelper.addBounds(labelPath, labelHeight, labelXOffset + labelWidth, true);
                }
            }
        }

        if (collapsedNodeLabelPainter != null && collapsedNodeLabelPainter.isVisible()) {
            collapsedNodeLabelPainter.calibrate(g2);

            for (Node node : tree.getInternalNodes()) {
                if(hideNode(node) || !isNodeCollapsed(node)) continue;

                final double labelWidth = collapsedNodeLabelPainter.getWidth(g2, node);
                double labelHeight = collapsedNodeLabelPainter.getPreferredHeight(g2, node);

                // Get the line that represents the orientation for the collapsed node label
                final Line2D labelPath = treeLayout.getNodeLabelPath(node);

                //System.out.println("For " + tree.getTaxon(node).getName())
                treeBoundsHelper.addBounds(labelPath, labelHeight, labelXOffset + labelWidth, false);
            }
        }
    }

    private void calibrateForScaleBarPainter(Graphics2D g2, double height, Rectangle2D treeBounds) {
        if (scaleBarPainter != null && scaleBarPainter.isVisible()) {
            scaleBarPainter.calibrate(g2);
            final double h1 = scaleBarPainter.getPreferredHeight(g2, this);
            final double xl = transform.getTranslateX() + transform.getScaleX() * treeBounds.getX();
            final double xh = transform.getTranslateX() + transform.getScaleX() * treeBounds.getMaxX();
            final double wid = xh - xl;
            scaleBarBounds = new Rectangle2D.Double(xl, height - h1, wid, h1);
        }
    }

    /**
     * Now we make all of the branch, node, and collapsed node labels, adding them to treeElements to be drawn
     * later in the drawTree method.
     */
    private List<TreeDrawableElement> createTreeDrawableElements(Graphics2D g2, Set<Node> externalNodes, double xScale) {
        treeElements.clear();

        if (collapsedNodeLabelPainter != null && collapsedNodeLabelPainter.isVisible()) {
            for (Node node : tree.getNodes()) {
                if (hideNode(node) || !isNodeVisible(node)) {
                    continue;
                }
                //We draw a label for this node if it is the top of a collapsed subtree (i.e. it is collapsed but its parent isn't)
                if (isNodeCollapsed(node) && (tree.isRoot(node) || !isNodeCollapsed(tree.getParent(node)))) {
                    final double labelHeight = collapsedNodeLabelPainter.getPreferredHeight(g2, node);

                    // Get the line that represents the orientation of node label
                    final Line2D labelPath = treeLayout.getNodeLabelPath(node);

                    if (labelPath != null) {
                        final double labelWidth = collapsedNodeLabelPainter.getWidth(g2, node);
                        final Rectangle2D labelBounds = new Rectangle2D.Double(0.0, 0.0, labelWidth, labelHeight);

                        // Work out how it is rotated and create a transform that matches that
                        AffineTransform labelTransform = calculateTransform(transform, labelPath, labelWidth, labelHeight, true);

                        Painter.Justification justification =
                                (labelPath.getX1() < labelPath.getX2()) ? Painter.Justification.LEFT : Painter.Justification.RIGHT;

                        //Paint this node label grey if we ARE using a filter and this node is NOT in the list of nodes above filter nodes
                        boolean paintAsGray = isFiltering && !internalNodesAboveFilterNodes.contains(node);
                        final TreeDrawableElementNodeLabel e =
                                new TreeDrawableElementNodeLabel(tree, node, justification, labelBounds, labelTransform, 8,
                                        null, ((BasicLabelPainter) collapsedNodeLabelPainter), "node", paintAsGray);

                        Color nodeColor = Color.black; //Collapsed nodes default to black right now, but overwritten with nodeColor
                        Object colorAttr = node.getAttribute(TreeViewerUtilities.KEY_NODE_COLOR);
                        if(colorAttr != null){
                            nodeColor = RgbBranchDecorator.getColorFromString(colorAttr.toString()).darker();
                        }
                        e.setForeground(nodeColor);
                        treeElements.add(e);
                    }
                }
            }
        }

        /**
         * Now create internal node labels
         */
        // Clear the map of individual node label bounds and transforms
        nodeLabelBounds.clear();
        nodeLabelTransforms.clear();
        nodeLabelJustifications.clear();

        if (nodeLabelPainter != null && nodeLabelPainter.isVisible()) {

            // Iterate though all nodes
            for (Node node : tree.getNodes()) {
                if( hideNode(node) || !isNodeVisible(node) || isNodeCollapsed(node) ) continue;
                final double labelHeight = nodeLabelPainter.getPreferredHeight(g2, node);

                // Get the line that represents the orientation of node label
                final Line2D labelPath = treeLayout.getNodeLabelPath(node);

                if (labelPath != null) {
                     final double labelWidth = nodeLabelPainter.getWidth(g2, node);
                     final Rectangle2D labelBounds = new Rectangle2D.Double(0.0, 0.0, labelWidth, labelHeight);

                    // Work out how it is rotated and create a transform that matches that
                    AffineTransform labelTransform = calculateTransform(transform, labelPath, labelWidth, labelHeight, true);

                    // Store the alignment in the map for use when drawing

                    Painter.Justification justification =
                            (labelPath.getX1() < labelPath.getX2()) ? Painter.Justification.LEFT : Painter.Justification.RIGHT;

                    final TreeDrawableElementNodeLabel e =
                        new TreeDrawableElementNodeLabel(tree, node, justification, labelBounds, labelTransform, 9,
                                                          null, ((BasicLabelPainter) nodeLabelPainter), "node", isFiltering);


                Object colorAttr = node.getAttribute(TreeViewerUtilities.KEY_NODE_COLOR);
                Color nodeColor = Color.black;
                if(colorAttr != null){
                    nodeColor = RgbBranchDecorator.getColorFromString(colorAttr.toString());
                }
                e.setForeground(nodeColor);
                    treeElements.add(e);
                }
            }
        }

        /**
         * Finally create the branch labels
         */
        if (branchLabelPainter != null && branchLabelPainter.isVisible()) {
            branchLabelPainter.calibrate(g2);

            for( Node node : tree.getNodes() ) {
                if( hideNode(node) || !isNodeVisible(node) ) continue;
                final double labelHeight = branchLabelPainter.getPreferredHeight(g2, node);

                // Get the line that represents the path for the branch label
                final Line2D labelPath = treeLayout.getBranchLabelPath(node);

                if (labelPath != null) {
                    final double labelWidth = branchLabelPainter.getWidth(g2, node);
                    final Rectangle2D labelBounds = new Rectangle2D.Double(0.0, 0.0, labelWidth, labelHeight);

                    final double branchLength = labelPath.getP2().distance(labelPath.getP1());

                    final Painter.Justification just = labelPath.getX1() < labelPath.getX2() ? Painter.Justification.LEFT :
                            Painter.Justification.RIGHT;

                    // Work out how it is rotated and create a transform that matches that
                    AffineTransform labelTransform = calculateTransform(transform, labelPath, labelWidth, labelHeight, false);

                    // move to middle of branch - since the move is before the rotation
                    // and center label by moving an extra half width of label
                    final double direction = just == Painter.Justification.RIGHT ? 1 : -1;
                    if (treeLayout instanceof RectilinearTreeLayout && branchLabelPainter instanceof BasicLabelPainter && ((BasicLabelPainter)branchLabelPainter).isConsensusSupportAttribute()) {
                        labelTransform.translate(-labelWidth + -direction * xScale * branchLength, -5 - labelHeight/2);
                    } else {
                        labelTransform.translate(-labelWidth/2 + -direction * xScale * branchLength/2, -5 - labelHeight/2);
                    }
                  //  System.out.println(" -> " + labelTransform);
                  //  if( k == 0 ) continue;

                    final TreeDrawableElementNodeLabel e =
                        new TreeDrawableElementNodeLabel(tree, node, Painter.Justification.CENTER, labelBounds, labelTransform, 8,
                                                          null, ((BasicLabelPainter) branchLabelPainter), "branch", isFiltering);


                    Paint nodeColor = new RgbBranchDecorator().getBranchPaint(tree, node);
                    e.setForeground(nodeColor);
                    treeElements.add(e);
                }
            }
        }
        return treeElements;
    }

    /**
     * Create and return the taxon labels
     * @param g2
     * @param externalNodes
     * @return
     */
    private List<TreeDrawableElement> getTaxonLabels(Graphics2D g2, Set<Node> externalNodes) {
        // Clear previous values of taxon label bounds and transforms
        taxonLabelBounds.clear();
        taxonLabelTransforms.clear();
        taxonLabelJustifications.clear();

        //the contents of taxonLabels are added to treeElements at the end of this method
        List<TreeDrawableElement> taxonLabels = new ArrayList<TreeDrawableElement>();
        if (taxonLabelPainter != null && taxonLabelPainter.isVisible()) {

            // Iterate though the external nodes
            for (Node node : externalNodes) {
                if (hideNode(node) || !isNodeVisible(node)) continue;

                double labelHeight = taxonLabelPainter.getPreferredHeight(g2, node);
                final Taxon taxon = tree.getTaxon(node);
                taxonLabelPainter.calibrate(g2);
                taxonLabelWidth = taxonLabelPainter.getWidth(g2, node);
                Rectangle2D labelBounds = new Rectangle2D.Double(0.0, 0.0, taxonLabelWidth, labelHeight);
                // Get the line that represents the path for the taxon label
                final Line2D taxonPath = treeLayout.getTaxonLabelPath(node);

                // Work out how it is rotated and create a transform that matches that
                AffineTransform taxonTransform = calculateTransform(transform, taxonPath, taxonLabelWidth, labelHeight, true);

                // Store the alignment in the map for use when drawing
                final Painter.Justification just = (taxonPath.getX1() < taxonPath.getX2()) ?
                        Painter.Justification.LEFT : Painter.Justification.RIGHT;

                int priority = 10;
                Color nodeColor = Color.black;
                if (referenceSequenceName!=null && referenceSequenceName.equals(taxon.getName())) {
                    nodeColor = new Color(0, 150,0);
                    priority = 11; // if there are too many labels to show them all, we prefer to show the reference sequence rather than other sequences
                }

                //Paint this node label grey if we ARE using a filter and the node DOES NOT match the filter. Paint normally in every other case
                boolean paintAsGray = isFiltering && !externalNodesThatFitFilter.contains(node);
                final TreeDrawableElementNodeLabel e =
                        new TreeDrawableElementNodeLabel(tree, node, just, labelBounds, taxonTransform, priority,
                                                         null, taxonLabelPainter,
                        null, false, paintAsGray);

                Object colorAttr = node.getAttribute(TreeViewerUtilities.KEY_NODE_COLOR);
                if(colorAttr != null){
                    nodeColor = RgbBranchDecorator.getColorFromString(colorAttr.toString());
                }
                e.setForeground(nodeColor);
                taxonLabels.add(e);
            }
        }
        return taxonLabels;
    }

    private void checkIfPaintingLabelsAndReScaleTreeIfNot(double width, double height, Rectangle2D treeBounds, Set<Node> externalNodes, double scaleHeight, double availableW, double availableH) {
        Rectangle initialViewRect = viewport.getViewRect();
        if (flipTree) {
            initialViewRect.translate(getWidth() - 2 * initialViewRect.x - initialViewRect.width, 0);
        }
        if (!isShowLabels(true, initialViewRect)) { //drawOnlyVisibleElements is only false when printing
            TreeBoundsHelper treeBoundsHelper =
                    new TreeBoundsHelper(externalNodes.size() + 2*tree.getNodes().size(), availableW, availableH,
                            treeBounds);
            scaleTreeReturningXScale(width, height, availableW, availableH, scaleHeight, treeBounds, treeBoundsHelper);
        }
    }

    private double scaleTreeReturningXScale(double width, double height, double availableW, double availableH, double scaleHeight, Rectangle2D treeBounds, TreeBoundsHelper treeBoundsHelper) {
        final double[] doubles = treeBoundsHelper.getOrigionAndScale(false);
        double yorigion = doubles[0];
        double yScale = doubles[1];

        final double[] xdoubles = treeBoundsHelper.getOrigionAndScale(true);
        double xorigion = xdoubles[0];
        double xScale = xdoubles[1];
        double xOffset;
        double yOffset = 0.0;

        if (treeLayout.maintainAspectRatio()) {
            // If the tree is layed out in both dimensions then we
            // need to find out which axis has the least space and scale
            // the tree to that (to keep the aspect ratio.

            if( treeBoundsHelper.getRange(true, xorigion, yScale) <= availableW ) {
                //if( xorigion + yScale * treeBounds.getWidth() <= availableW ) {
                xorigion = treeBoundsHelper.getOrigion(true, yScale);
                treeScale = yScale;
            } else {
                double size;
                int count = 0;
                String oldValues = "";
                //count is here to make sure we don't get an infinite loop if there is no scale that will
                //allow the tree to be contained in the current view
                while((size = treeBoundsHelper.getRange(false, yorigion, xScale)) > availableH && count < 10){
                    xScale *= availableH/size;
                    yorigion = yOffset;
                    count++;
                }
                //todo: this was removed assert treeBoundsHelper.getRange(false, yorigion, xScale) <= availableH : treeBoundsHelper.getRange(false, yorigion, xScale)+" : "+availableH+" : "+oldValues;
                //assert yorigion + xScale * treeBounds.getHeight() <= availableH;
                yorigion = treeBoundsHelper.getOrigion(false, xScale);
                treeScale  = xScale;
            }

            //System.out.println("xs/ys " + xScale + "/" + yScale +  " (" + treeScale + ")" + " xo/yo " + xorigion + "/" + yorigion);
            xScale = yScale = treeScale;

            xOffset = xorigion - treeBounds.getX() * treeScale;
            yOffset = yorigion - treeBounds.getY() * treeScale;
            double xRange = treeBoundsHelper.getRange(true, xorigion, treeScale);
            final double dx = (availableW - xRange)/2;
            xOffset += dx;
            double yRange = treeBoundsHelper.getRange(false, yorigion, treeScale);
            final double dy = (availableH - yRange)/2;
            yOffset += dy; //  > 0 ? dy : 0;
            //System.out.println("xof/yof " + xOffset + "/" + yOffset);

        } else {
            // Otherwise just scale both dimensions
            xOffset = xorigion - treeBounds.getX() * xScale;
            yOffset = yorigion - treeBounds.getY() * yScale;
            treeScale = xScale;
        }

        if(treeScale < 0) {
            treeScale = 0;
        }

//        assert treeScale > 0;

        // Create the overall transform
        transform = new AffineTransform();
        transform.translate(xOffset + insets.left, yOffset + insets.top);
        transform.scale(xScale, yScale);
        return xScale;
    }

    /**
     * This method checks if any of the collapsed subtree settings have changed and
     * can reset the states and visibilities of nodes if needed
     */
    private void checkAndSetNewAutoCollapseVariables() {
        autoCollapseNodes = collapsedNodeLabelPainter.isCollapsed();
        double newThreshold = collapsedNodeLabelPainter.getCollapsedDistanceThreshold();
        double oldThreshold = cladeDistanceThresholdToCollapse;
        cladeDistanceThresholdToCollapse = newThreshold;
        boolean wantToResetNodeStates = collapsedNodeLabelPainter.isResetCollapseState();

        if (wantToResetNodeStates) {
            resetManuallyCollapsedOrExpandedNodes();
        }
        //If the distance has changed, work out what is now visible
        if (wantToResetNodeStates || !(Math.abs(newThreshold - oldThreshold) < 0.0001)) {
            resetNodeVisibilities();
        }
    }

    private void resetManuallyCollapsedOrExpandedNodes() {
        manuallyExpanded.clear();
        for (Node node : manuallyCollapsed) {
            node.removeAttribute(KEY_INVISIBLE_NODE);
        }
        manuallyCollapsed.clear();
        manualListChanged();
    }

    private void resetNodeVisibilities() {
        for (Node node : nodesInOrder) {
            if (tree.isRoot(node)) continue;
            boolean isManuallyExpanded = manuallyExpanded.contains(node) || manuallyExpanded.contains(tree.getParent(node));
            if (!isManuallyExpanded && isBelowCollapseDistanceThreshold(node) && isBelowCollapseDistanceThreshold(tree.getParent(node))) {
                node.setAttribute(KEY_INVISIBLE_NODE_WHEN_AUTOCOLLAPSE, Boolean.TRUE);
            } else {
                node.removeAttribute(KEY_INVISIBLE_NODE_WHEN_AUTOCOLLAPSE);
                if (isManuallyExpanded) node.removeAttribute(KEY_INVISIBLE_NODE);
            }
        }
    }

    /**
     * Sets a qualifier on each internal node that holds the max distance between that node and its
     * furthest child node. O(n) in size of tree
     */
    private void setDistancesToAncestorsForAutoExpansion() {
        Set<Node> alreadyLookedAt = new HashSet<Node>();
        for (Node node : tree.getExternalNodes()) {
            if(!alreadyLookedAt.contains(node)){
                setDistanceAndParentDistances(node);
                alreadyLookedAt.add(node);
            }
        }

        //This will happen if setUpTree() is called anywhere after the initial construction of treePane.
        //Which is good because tree distances could've changed
        if (collapsedNodeLabelPainter != null) {
            collapsedNodeLabelPainter.treeChanged(tree);
        }
    }

    /**
     * Given a tip, sets a variable on its parent representing the Max distance from the parent to any of its descendants.
     * Each internal node will be touched once by each of its children, and after all children are done it is guaranteed
     * to have the longest distance to a descendant set as a qualifier.
     *
     * When the last child of a node is checked and that node is finished, this method goes up a level and checks its parent
     * to do the same. In this way the whole tree is traversed if this method is called for each tip.
     * @param tip
     */
    private void setDistanceAndParentDistances(Node tip) {
        final String childrenMeasuredKey = "numberChildrenDistancesMeasured";
        Node currentChild = tip;
        Node currentParent = tree.getParent(tip);

        while (currentParent != null) {
            double distanceFromChildToParent = originalTree.hasLengths() ? originalTree.getLength(currentChild) : 1.0;

            double distanceBelowCurrentChild = 0.0;
            Object distanceBelowCurrentChildObject = currentChild.getAttribute(KEY_MAX_DISTANCE_TO_DESCENDANT);
            if (distanceBelowCurrentChildObject != null && distanceBelowCurrentChildObject instanceof Double) {
                distanceBelowCurrentChild = (Double) distanceBelowCurrentChildObject;
            }


            double longestDistanceSoFar = 0.0;
            Object longestDistanceSoFarObject = currentParent.getAttribute(KEY_MAX_DISTANCE_TO_DESCENDANT);
            if (longestDistanceSoFarObject != null && longestDistanceSoFarObject instanceof Double) {
                longestDistanceSoFar = (Double) longestDistanceSoFarObject;
            }

            longestDistanceSoFar = Math.max(longestDistanceSoFar, distanceBelowCurrentChild + distanceFromChildToParent);
            currentParent.setAttribute(KEY_MAX_DISTANCE_TO_DESCENDANT, longestDistanceSoFar);

            int childrenDone = 0;
            Object childrenDoneAttribute = currentParent.getAttribute(childrenMeasuredKey);
            if (childrenDoneAttribute != null && childrenDoneAttribute instanceof Integer) {
                childrenDone = (Integer) childrenDoneAttribute;
            }
            currentParent.setAttribute(childrenMeasuredKey, childrenDone + 1);

            //if this is the last child that needs to be processed for this node, go up a level and check there too
            if (childrenDone + 1 == tree.getChildren(currentParent).size()) {
                currentParent.removeAttribute(childrenMeasuredKey);
                currentChild = currentParent;
                currentParent = tree.getParent(currentChild);
            } else {
                break;
            }
        }
    }

    private AffineTransform calculateTransform(AffineTransform globalTransform, Line2D line,
                                               double width, double height, boolean just) {
        final Point2D origin = line.getP1();
        if (globalTransform != null) {
            globalTransform.transform(origin, origin);
        }

        // Work out how it is rotated and create a transform that matches that
        AffineTransform lineTransform = new AffineTransform();

        final double dy = line.getY2() - line.getY1();
        // efficency
        if( dy != 0.0 ) {
            final double dx = line.getX2() - line.getX1();
            final double angle = dx != 0.0 ? Math.atan(dy / dx) : 0.0;
            lineTransform.rotate(angle, origin.getX(), origin.getY());
        }

        // Now add a translate to the transform - if it is on the left then we need
        // to shift it by the entire width of the string.
        final double ty = origin.getY() - (height / 2.0);
        double tx = origin.getX();
        if( just) {
            if (line.getX2() > line.getX1()) {
                tx += labelXOffset;
            } else {
                tx -= (labelXOffset + width);
            }
        }
        lineTransform.translate(tx, ty);
        return lineTransform;
    }

    public void setViewPort(JViewport viewport) {
        this.viewport = viewport;
    }

    public TreeLayout getTreeLayout() {
        return treeLayout;
    }

    /**
     * Set the name of the reference sequence which will be colored green and
     * preferentially drawn over other labels when there are too many labels to display
     * @param referenceSequenceName the name of the reference sequence or null for no reference sequence
     */
    public void setReferenceSequenceName(String referenceSequenceName) {
        this.referenceSequenceName = referenceSequenceName;
    }

    /**
     * Sets the filter string text. Checks the external node labels to see if they match the filter
     * and add them to the externalNodesThatFitFilter set if they do. If filterText != "", labels for nodes
     * that don't match the filter and all branches will be drawn in gray.
     * Also adds all nodes on the way from the filter nodes to the root into a separate list. This is so
     * any collapsed node can signal when a filter node is below it.
     *
     * @param filterText
     */
    public void setFilterText(String filterText) {
        int nodesThatFitFilter = externalNodesThatFitFilter.size();
        Node currentlySelected = nodesThatFitFilter > 0 ? externalNodesThatFitFilter.get(currentFilterNodeIndex) : null;
        this.filterText = filterText;
        this.isFiltering = !"".equals(filterText);
        externalNodesThatFitFilter.clear();
        internalNodesAboveFilterNodes.clear();
        for (Node node : nodesInOrder) {
            if (!tree.isExternal(node)) {
                continue;
            }
            BasicLabelPainter painter = (BasicLabelPainter) taxonLabelPainter;
            if (painter.matchesFilter(node, filterText)) {
                externalNodesThatFitFilter.add(node);
                internalNodesAboveFilterNodes.addAll(getParentsToRoot(node));
            }
        }

        // This method can be called when the filter text hasn't changed (if something like a branch transform calls setupTree every paint).
        // Not skipping this gets the filter locked on a single node in that situation.
        if (nodesThatFitFilter != externalNodesThatFitFilter.size()) {
            currentFilterNodeIndex = externalNodesThatFitFilter.size() - 1;
        } else if (currentlySelected != null) {
            int newIndex = externalNodesThatFitFilter.indexOf(currentlySelected);
            currentFilterNodeIndex = newIndex >= 0 ? newIndex : 0;
        }
        calibrated = false;
        repaint();
    }

    /**
     * When the user hits enter or shift+enter in the filter box, this method is called
     * to cycle through all the nodes that match the filter. It will select the current node
     * and expand down to it if it is part of a collapsed subtree.
     *
     * @param forwards
     */
    public void jumpToNextFilterMatch(boolean forwards) {
        if (externalNodesThatFitFilter.size() == 0) return;

        if (forwards) {
            currentFilterNodeIndex ++;
            if (currentFilterNodeIndex== externalNodesThatFitFilter.size()) {
                currentFilterNodeIndex = 0;
            }
        } else {
            currentFilterNodeIndex --;
            if (currentFilterNodeIndex == -1) {
                currentFilterNodeIndex = externalNodesThatFitFilter.size() - 1;
            }
        }
        Node toJumpTo = externalNodesThatFitFilter.get(currentFilterNodeIndex);
        if (isNodeCollapsed(toJumpTo)) {
            manuallyToggleExpandContract(toJumpTo, true);
            resetNodeVisibilities();
        }
        setSelectedNode(toJumpTo);
        focusViewerOnNode(toJumpTo);
        fireSelectionChanged();
     }

    /**
     * Focuses the viewport on the specified node
     * @param node node to focus on
     */
    private void focusViewerOnNode(Node node) {
        Point2D nodeCoords = nodeCoord(node);
        if(viewport != null) {
            // half the viewport's width and height.
            // Used to get the origin of the rectangle that should be scrolled to in order to centre the viewport on the node
            double halfViewportWidth = viewport.getViewRect().getWidth() / 2.0;
            double halfViewportHeight = viewport.getViewRect().getHeight() / 2.0;
            // x and y coordinates the viewport should move to: node's coordinate - half the viewport's width/height
            double x = nodeCoords.getX() - halfViewportWidth;
            double y = nodeCoords.getY() - halfViewportHeight;
            // scroll the viewport
            scrollRectToVisible(new Rectangle((int)x, (int)y, (int)viewport.getViewRect().getWidth(), (int)viewport.getViewRect().getHeight()));
        }
    }

    private class TreeBoundsHelper {
        private double[] xbounds;
        private double[] ybounds;
        int nv;
        double availableW;
        double availableH;
        Rectangle2D treeBounds;

        public TreeBoundsHelper(int nValues, double availableW, double availableH, Rectangle2D treeBounds) {
            // each value imposes a constraints (3 numbers) + 2 for raw height and width
            nValues = 3 * (nValues + 2);
            xbounds = new double[nValues];
            ybounds = new double[nValues];
            nv = 0;
            this.availableH = availableH;
            this.availableW = availableW;
            this.treeBounds = treeBounds;

            xbounds[nv] = treeBounds.getWidth();
            xbounds[nv+1] = availableW;
            xbounds[nv+2] = 0;
            ybounds[nv] = treeBounds.getHeight();
            ybounds[nv+1] = availableH;
            ybounds[nv+2] = 0;
            nv += 3;
            xbounds[nv] = 0;
            xbounds[nv+1] = availableW;
            xbounds[nv+2] = 0;
            ybounds[nv] = 0;
            ybounds[nv+1] = availableH;
            ybounds[nv+2] = 0;
            nv += 3;
        }

        private int quadrantOf(Line2D line, double[] sincos) {
            Point2D start = line.getP1();
            Point2D end = line.getP2();
            double dy = end.getY() - start.getY();
            double dx = end.getX() - start.getX();
            double r = Math.sqrt(dx * dx + dy * dy);
            sincos[0] = dy / r;
            sincos[1] = dx / r;
            return (dy>=0 ? 0 : 2) + ((dy>=0) == (dx>=0) ? 0 : 1);
        }

        //  v, height - y-extra(max), -y-extra(min)
        void addBounds(Line2D taxonPath, double labelHeight, double labelWidth, boolean centered)  {
            double[] sincos = {0.0, 1.0};

            int quad = quadrantOf(taxonPath, sincos); // sine and cosine of the inclination of the taxonPath vector
            final double yHigh = labelHeight / 2;
            final double xHigh = centered ? labelWidth / 2 : labelWidth;
            final double xLow =  centered ? -xHigh : 0;
            // origin here before rotate is midpoint on left edge for non centered, center for centered
            // order is counter-clockwise from upper right corner, which makes the corner number match the quadrant number
            // for max X. other limits are relative to that.

            double[] pts = {xHigh, yHigh, xLow, yHigh, xLow, -yHigh, xHigh, -yHigh}; // four corners of bounding box
            int ixmax = 2*quad;
            int ixmin = 2*((quad+2) & 0x3);
            int iymax = 2*((quad+3) & 0x3);
            int iymin = 2*((quad+1) & 0x3);

            final double thesin = -sincos[0];
            final double thecos = sincos[1];

            double dx = thecos * pts[ixmax] -  thesin * pts[ixmax + 1];
            if( Double.isNaN(dx) ) {
              assert dx >= 0 : dx + " " + thecos + " " + thesin;
            }

            final Point2D start = taxonPath.getP1();
            final Point2D end = taxonPath.getP2();
            final double xInTreeAbs = centered ? (start.getX() + end.getX()) / 2 : start.getX();
            // yikes - some code determining the paths uses floats, so when used with doubles small discrapencies can make
            // valus small negatives
            double x = (float)xInTreeAbs - (float)treeBounds.getMinX();            assert x >= 0 : x;
            xbounds[nv] = x;
            xbounds[nv+1] = availableW - dx;
            dx = -(thecos * pts[ixmin] - thesin * pts[ixmin + 1]);                 assert dx >= 0 : dx;
            xbounds[nv+2] = dx;

            double dy = -(thesin * pts[iymax] + thecos * pts[iymax+1]);            assert dy >= 0;
            // y0 + scale(y) * y + y-extra <= height
            // y0 + scale(y) * y + y-extra >= 0

            final double yTreeAbs = centered ? (start.getY() + end.getY()) / 2 : start.getY();
            double y = (float) yTreeAbs - (float)treeBounds.getMinY();             assert y >= 0 : y;
            ybounds[nv] = y;
            ybounds[nv+1] = availableH - dy;

            dy = thesin * pts[iymin] + thecos * pts[iymin+1];                      assert dy >= 0;
            ybounds[nv+2] = dy;

            nv += 3;
        }

        double[] getOrigionAndScale(boolean isx) {
            double[] values = isx ? xbounds : ybounds;

            double scale = Double.MAX_VALUE;
            double origin = 0.0;
            double minOrigin = Double.MAX_VALUE;
            for(int k = 0; k < nv; k += 3) {
                if( values[k] == 0.0 ) {
                    origin = Math.max(origin, values[k+2]);
                }
                minOrigin = Math.min(minOrigin, values[k+1]);
            }

            if( origin > minOrigin ) {
                origin = minOrigin/2;
            }

            int nit = 0;
            while( nit < 100 ) {
                ++nit; // safty net, should converge long before that
                double scaleMin = -Double.MAX_VALUE;
                for(int k = 0; k < nv; k += 3) {
                    if( values[k] != 0.0 ) {
                        double lim = Math.abs((values[k+1] - origin) / values[k]);
                        scale = Math.min(scale, lim);
                    }

                    double d = (values[k + 2] - origin);
                    // do limit only if y0 is no suffcient in this case
                    if( d > 0 ) {
                        double lim = d / values[k];
                        scaleMin = Math.max(scaleMin, lim);
                    }
                }

                boolean b = scaleMin <= scale || nit > 10 &&  scaleMin - scale < 1e-5;
                if( origin < minOrigin && b) {
                    break;
                }
                origin = -Double.MAX_VALUE;
                for(int k = 0; k < nv; k += 3) {
                    double l = values[k + 2] - scale * values[k];
                    origin = Math.max(origin, l);
                }
            }
            if(scale < 0) {
                scale = 0;
            }
            if(origin < 0) {
                origin = 0;
            }
            //assert scale > 0 : scale + " " + nit;
            //assert origin >= 0.0 : origin + " " + nit;
            return new double[]{origin, scale};
        }

        double getOrigion(boolean isx, double scale) {
            double origin = -Double.MAX_VALUE;
            double[] values = isx ? xbounds : ybounds;
            for(int k = 0; k < nv; k += 3) {
                double l = values[k + 2] - scale * values[k];
                origin = Math.max(origin, l);
            }
            return origin;
        }

        double getRange(boolean isx, double origin, double scale) {
            double[] values = isx ? xbounds : ybounds;
            double target = isx ? availableW : availableH;

            double mx = -Double.MAX_VALUE, mn = Double.MAX_VALUE;

            for(int k = 0; k < nv; k += 3) {
                final double v = origin + values[k] * scale;
                mx = Math.max(mx, v + (target - values[k+1]));
                mn = Math.min(mn, v - values[k+2]);
            }
            return mx - mn;
        }
    }


    // Overridden methods to recalibrate tree when bounds change
    public void setBounds(int x, int y, int width, int height) {
        // when moving the viewport x/y change
        final Rectangle rectangle = getBounds();
        calibrated = calibrated && width == rectangle.width && height == rectangle.height;
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
        //taxonLabelPainter.resetFontSizes(false); //triggers a resize of the label fonts
        calibrated = false;
        super.setSize(width, height);
    }

    private JViewport viewport = null;

    // Tree passed in
    private RootedTree originalTree = null;
    //  Tree possibly transformed by the viewer
    private RootedTree tree = null;
    private List<Node> nodesInOrder;

    private TreeLayout treeLayout = null;

    private boolean orderBranches = false;
    private SortedRootedTree.BranchOrdering branchOrdering = SortedRootedTree.BranchOrdering.INCREASING_NODE_DENSITY;

    private boolean transformBranches = false;
    private boolean flipTree = false;
    private TransformedRootedTree.Transform branchTransform = TransformedRootedTree.Transform.CLADOGRAM;


    private double treeScale;

    //private Insets margins = new Insets(6, 6, 6, 6);
    private Insets insets = new Insets(6, 6, 6, 6);

    private Set<Node> selectedNodes = new HashSet<Node>();
    private Set<Taxon> selectedTaxa = new HashSet<Taxon>();

    private Rectangle2D dragRectangle = null;

    private BranchDecorator branchDecorator = null;

    private float labelXOffset = 5.0F;
    private Painter<Node> taxonLabelPainter = null;
    private double taxonLabelWidth;
    private Painter<Node> nodeLabelPainter = null;
    private Painter<Node> branchLabelPainter = null;

    private ScaleBarPainter scaleBarPainter = null;
    private Rectangle2D scaleBarBounds = null;

    private CollapsedNodeLabelPainter collapsedNodeLabelPainter;

    private Stroke branchLineStroke = new BasicStroke(1.0F);
    private Stroke collapsedStroke = new BasicStroke(1.5F); //the stroke used to draw collapsed node shapes
    private Paint selectionPaint = Color.BLUE; // new Color(180, 213, 254);
    private boolean calibrated = false;
    private double cladeDistanceThresholdToCollapse = 0.0;

    // Transform which scales the tree from it's own units to pixles and moves it to center of window
    private AffineTransform transform = null;

    private boolean showingRootBranch = true;
    private boolean autoCollapseNodes = false;
    private boolean showingTaxonCallouts = true;

    private Map<Taxon, AffineTransform> taxonLabelTransforms = new HashMap<Taxon, AffineTransform>();
    private Map<Taxon, Shape> taxonLabelBounds = new HashMap<Taxon, Shape>();
    private Map<Taxon, Painter.Justification> taxonLabelJustifications = new HashMap<Taxon, Painter.Justification>();

    private Map<Node, AffineTransform> nodeLabelTransforms = new HashMap<Node, AffineTransform>();
    private Map<Node, Shape> nodeLabelBounds = new HashMap<Node, Shape>();
    private Map<Node, Painter.Justification> nodeLabelJustifications = new HashMap<Node, Painter.Justification>();

    private Map<Node, AffineTransform> branchLabelTransforms = new HashMap<Node, AffineTransform>();
    private Map<Node, Shape> branchLabelBounds = new HashMap<Node, Shape>();

    private List<TreeDrawableElement> treeElements = new ArrayList<TreeDrawableElement>();

    // unused at the moment
    //private Map<Node, Painter.Justification> branchLabelJustifications = new HashMap<Node, Painter.Justification>();

    // unused at the moment
    // private Map<Taxon, Shape> calloutPaths = new HashMap<Taxon, Shape>();

    private String transformBanchesPREFSkey = "transformBranches";
    private String flipTreePREFSkey = "flipTree";
    private String branchTransformTypePREFSkey = "branchTransformType";
    private String branchOrderingPREFSkey = "branchOrdering";
    private String orderBranchesPREFSkey = "orderBranches";
    private String showRootPREFSkey = "showRootBranch";
    private String autoExPREFSkey = "autoCollapseNodes";
    private String viewSubtreePREFSkey = "viewSubtree";
    private String branchWeightPREFSkey = "branchWeight";
    private static Preferences getPrefs() {
        return Preferences.userNodeForPackage(TreePane.class);
    }
}

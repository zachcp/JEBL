package jebl.gui.trees.treeviewer_dev.treelayouts;

import jebl.evolution.graphs.Node;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.Tree;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.*;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public abstract class AbstractTreeLayout implements TreeLayout {
    public void setTree(Tree tree) {
        this.tree = (RootedTree)tree;
        invalidate();
    }

    public void invalidate() {
        invalid = true;
        fireTreeLayoutChanged();
    }

    public Point2D getNodePoint(Node node) {
        checkValidation();
        return nodePoints.get(node);
    }

    public Shape getBranchPath(Node node) {
        checkValidation();
        return branchPaths.get(node);
    }

    public Map<Node, Shape> getBranchPathMap() {
        checkValidation();
        return branchPaths;
    }

    public Shape getCollapsedShape(Node node) {
        checkValidation();
        return collapsedShapes.get(node);
    }

    public Map<Node, Shape> getCollapsedShapeMap() {
        checkValidation();
        return collapsedShapes;
    }

    public Line2D getTipLabelPath(Node node) {
        checkValidation();
        return tipLabelPaths.get(node);
    }

    public Map<Node, Line2D> getTipLabelPathMap() {
        checkValidation();
        return tipLabelPaths;
    }

    public Line2D getBranchLabelPath(Node node) {
        checkValidation();
        return branchLabelPaths.get(node);
    }

    public Map<Node, Line2D> getBranchLabelPathMap() {
        checkValidation();
        return branchLabelPaths;
    }

    public Line2D getNodeLabelPath(Node node) {
        checkValidation();
        return nodeLabelPaths.get(node);
    }

    public Map<Node, Line2D> getNodeLabelPathMap() {
        checkValidation();
        return nodeLabelPaths;
    }

    public Line2D getNodeBarPath(Node node) {
        checkValidation();
        return nodeBarPaths.get(node);
    }

    public Map<Node, Line2D> getNodeBarPathMap() {
        checkValidation();
        return nodeBarPaths;
    }

    public Shape getCalloutPath(Node node) {
        checkValidation();
        return calloutPaths.get(node);
    }

    public Map<Node, Shape> getCalloutPathMap() {
        checkValidation();
        return calloutPaths;
    }

    private void checkValidation() {
        if (invalid) {
            validate();
            invalid = false;
        }
    }

    public void addTreeLayoutListener(TreeLayoutListener listener) {
        listeners.add(listener);
    }

    public void removeTreeLayoutListener(TreeLayoutListener listener) {
        listeners.remove(listener);
    }

    protected void fireTreeLayoutChanged() {
        for (TreeLayoutListener listener : listeners) {
            listener.treeLayoutChanged();
        }
    }

    protected abstract void validate();

    public String getBranchColouringAttribute() {
        return branchColouringAttribute;
    }

    public void setBranchColouringAttribute(String branchColouringAttribute) {
        this.branchColouringAttribute = branchColouringAttribute;
        invalidate();
    }

    public boolean isShowingColouring() {
        return branchColouringAttribute != null;
    }

    public String getCollapseAttributeName() {
        return collapseAttributeName;
    }

    public void setCollapseAttributeName(String collapseAttributeName) {
        this.collapseAttributeName = collapseAttributeName;
        invalidate();
    }

    public boolean isShowingCollapsedTipLabels() {
        return showingCollapsedTipLabels;
    }

    public void setShowingCollapsedTipLabels(boolean showingCollapsedTipLabels) {
        this.showingCollapsedTipLabels = showingCollapsedTipLabels;
        invalidate();
    }

    private boolean invalid = true;
    protected RootedTree tree = null;
    protected Map<Node, Point2D> nodePoints = new HashMap<Node, Point2D>();
    protected Map<Node, Shape> branchPaths = new HashMap<Node, Shape>();
    protected Map<Node, Shape> collapsedShapes = new HashMap<Node, Shape>();
    protected Map<Node, Line2D> tipLabelPaths = new HashMap<Node, Line2D>();
    protected Map<Node, Line2D> branchLabelPaths = new HashMap<Node, Line2D>();
    protected Map<Node, Line2D> nodeLabelPaths = new HashMap<Node, Line2D>();
    protected Map<Node, Line2D> nodeBarPaths = new HashMap<Node, Line2D>();
    protected Map<Node, Shape> calloutPaths = new HashMap<Node, Shape>();

    private Set<TreeLayoutListener> listeners = new HashSet<TreeLayoutListener>();
    protected String branchColouringAttribute = null;
    protected String collapseAttributeName = null;
    protected boolean showingCollapsedTipLabels = true;
}

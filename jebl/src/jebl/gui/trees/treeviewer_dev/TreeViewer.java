package jebl.gui.trees.treeviewer_dev;

import jebl.evolution.graphs.Node;
import jebl.evolution.trees.*;
import jebl.gui.trees.treeviewer_dev.decorators.Decorator;
import jebl.gui.trees.treeviewer_dev.painters.*;
import jebl.gui.trees.treeviewer_dev.treelayouts.TreeLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.print.Printable;
import java.util.Collection;
import java.util.Set;

import org.virion.jam.panels.StatusProvider;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public abstract class TreeViewer extends JPanel implements Printable {

    public abstract void setTrees(Collection<? extends Tree> trees);

    public abstract java.util.List<Tree> getTrees();

    public abstract Tree getCurrentTree();

    public abstract int getCurrentTreeIndex();

    public abstract int getTreeCount();

    public abstract StatusProvider getStatusProvider();

    public abstract void showTree(int index);

	public abstract void setTreeLayout(TreeLayout treeLayout);

	public abstract void setZoom(double zoom);

	public abstract void setVerticalExpansion(double verticalExpansion);

	public abstract boolean verticalExpansionAllowed();

	public abstract void setTimeScale(TimeScale timeScale);

    public abstract boolean hasSelection();

	public abstract Set<Node> getSelectedNodes();

	public abstract Set<Node> getSelectedTips();

    public abstract void selectTaxa(SearchType searchType, String searchString, boolean caseSensitive);

	public abstract void selectNodes(String attribute, SearchType searchType, String searchString, boolean caseSensitive);

	public abstract void collapseSelectedNodes();

    public abstract void annotateSelectedNodes(String name, Object value);

    public abstract void annotateSelectedTips(String name, Object value);

    public abstract void selectAll();

	public abstract void clearSelectedTaxa();

    public abstract void addTreeSelectionListener(TreeSelectionListener treeSelectionListener);

    public abstract void removeTreeSelectionListener(TreeSelectionListener treeSelectionListener);


    public abstract void setSelectionMode(TreePaneSelector.SelectionMode selectionMode);

	public abstract void setDragMode(TreePaneSelector.DragMode dragMode);

    public abstract void setTipLabelPainter(LabelPainter<Node> tipLabelPainter);

    public abstract void setNodeLabelPainter(LabelPainter<Node> nodeLabelPainter);

    public abstract void setNodeBarPainter(NodeBarPainter nodeBarPainter);

    public abstract void setBranchLabelPainter(LabelPainter<Node> branchLabelPainter);

    public abstract void addScalePainter(ScalePainter scalePainter);

	public abstract void removeScalePainter(ScalePainter scalePainter);

    public abstract void setScaleGridPainter(ScaleGridPainter scaleGridPainter);

    public abstract void setBranchDecorator(Decorator branchDecorator);

    public abstract void setBranchColouringDecorator(String branchColouringAttribute, Decorator branchColouringDecorator);

    public abstract void setSelectionPaint(Paint selectionPane);

    public abstract Paint getSelectionPaint();

    public abstract void setBranchStroke(BasicStroke branchStroke);


    public abstract boolean isTransformBranchesOn();

    public abstract TransformedRootedTree.Transform getBranchTransform();

    public abstract void setTransformBranchesOn(boolean transformBranchesOn);

    public abstract void setBranchTransform(TransformedRootedTree.Transform transform);


    public abstract boolean isOrderBranchesOn();

    public abstract SortedRootedTree.BranchOrdering getBranchOrdering();

    public abstract void setOrderBranchesOn(boolean orderBranchesOn);

    public abstract void setBranchOrdering(SortedRootedTree.BranchOrdering branchOrdering);

    public abstract boolean isRootingOn();

    public abstract TreePane.RootingType getRootingType();

    public abstract void setRootingOn(boolean rootingOn);

    public abstract void setRootingType(TreePane.RootingType rootingType);


    public abstract JComponent getContentPane();

	public abstract void addTreeViewerListener(TreeViewerListener listener);

	public abstract void removeTreeViewerListener(TreeViewerListener listener);


	public enum SearchType {
	    CONTAINS("Contains"),
	    STARTS_WITH("Starts with"),
	    ENDS_WITH("Ends with"),
	    MATCHES("Matches");

	    SearchType(String name) {
	        this.name = name;
	    }

	    public String toString() {
	        return name;
	    }

	    private final String name;
	}
}

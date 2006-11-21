package jebl.gui.trees.treeviewer_dev;

import jebl.gui.trees.treeviewer_dev.treelayouts.TreeLayout;

import javax.swing.*;
import java.awt.print.Printable;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public interface TreeViewer extends Printable {

	void setTreeLayout(TreeLayout treeLayout);

	void setZoom(double zoom);

	void setVerticalExpansion(double verticalExpansion);

	boolean verticalExpansionAllowed();

	void selectTaxa(SearchType searchType, String searchString, boolean caseSensitive);

	void selectNodes(String attribute, SearchType searchType, String searchString, boolean caseSensitive);

	void collapseSelected();

	void selectAll();

	void clearSelectedTaxa();

	void setSelectionMode(TreePaneSelector.SelectionMode selectionMode);

	void setDragMode(TreePaneSelector.DragMode dragMode);

	JComponent getExportableComponent();

	void addTreeViewerListener(TreeViewerListener listener);

	void removeTreeViewerListener(TreeViewerListener listener);

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

package jebl.gui.trees.treeviewer_dev.treelayouts;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public abstract class AbstractTreeLayout implements TreeLayout {
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

    public String getBranchColouringAttributeName() {
        return branchColouringAttribute;
    }

    public void setBranchColouringAttributeName(String branchColouringAttribute) {
        this.branchColouringAttribute = branchColouringAttribute;
        fireTreeLayoutChanged();
    }

    public boolean isShowingColouring() {
        return branchColouringAttribute != null;
    }

    public String getCollapseAttributeName() {
        return collapseAttributeName;
    }

    public void setCollapseAttributeName(String collapseAttributeName) {
        this.collapseAttributeName = collapseAttributeName;
        fireTreeLayoutChanged();
    }

    public boolean isShowingCollapsedTipLabels() {
        return showingCollapsedTipLabels;
    }

    public void setShowingCollapsedTipLabels(boolean showingCollapsedTipLabels) {
        this.showingCollapsedTipLabels = showingCollapsedTipLabels;
        fireTreeLayoutChanged();
    }

    private Set<TreeLayoutListener> listeners = new HashSet<TreeLayoutListener>();
    protected String branchColouringAttribute = null;
    protected String collapseAttributeName = null;
    protected boolean showingCollapsedTipLabels = true;
}

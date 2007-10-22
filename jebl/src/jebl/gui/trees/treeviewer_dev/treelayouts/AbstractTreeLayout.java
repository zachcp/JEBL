package jebl.gui.trees.treeviewer_dev.treelayouts;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public abstract class AbstractTreeLayout implements TreeLayout {
	private double rootLength = 0.0;

	public double getRootLength() {
		return rootLength;
	}

	public void setRootLength(double rootLength) {
		this.rootLength = rootLength;
		fireTreeLayoutChanged();
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

    public String getCartoonAttributeName() {
        return cartoonAttributeName;
    }

    public void setCartoonAttributeName(String cartoonAttributeName) {
        this.cartoonAttributeName = cartoonAttributeName;
        fireTreeLayoutChanged();
    }

    public boolean isShowingCartoonTipLabels() {
        return showingCartoonTipLabels;
    }

    public void setShowingCartoonTipLabels(boolean showingCartoonTipLabels) {
        this.showingCartoonTipLabels = showingCartoonTipLabels;
        fireTreeLayoutChanged();
    }

	public String getCollapsedAttributeName() {
		return collapsedAttributeName;
	}

	public void setCollapsedAttributeName(String collapsedAttributeName) {
		this.collapsedAttributeName = collapsedAttributeName;
		fireTreeLayoutChanged();
	}

    private Set<TreeLayoutListener> listeners = new HashSet<TreeLayoutListener>();
    protected String branchColouringAttribute = null;
    protected String cartoonAttributeName = null;
    protected boolean showingCartoonTipLabels = true;

	protected String collapsedAttributeName = null;
}

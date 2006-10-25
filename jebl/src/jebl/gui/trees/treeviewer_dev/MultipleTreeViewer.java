package jebl.gui.trees.treeviewer_dev;

import jebl.evolution.trees.Tree;

import java.util.*;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class MultipleTreeViewer extends TreeViewer {

	public MultipleTreeViewer() {
	    super();
	}

    public MultipleTreeViewer(TreePane treePane) {
        super(treePane);
    }

    public void setTree(Tree tree) {
        this.trees = new ArrayList<Tree>();
        trees.add(tree);
        setCurrentTree(0);
    }

    public void setTrees(Collection<? extends Tree> trees) {
        this.trees = new ArrayList<Tree>(trees);
	    setCurrentTree(0);
    }

	public List<Tree> getTrees() {
		return trees;
	}

    public void setCurrentTree(int index) {
        super.setTree(trees.get(index));
	    currentTreeIndex = index;
    }

	public void showNextTree() {
		if (currentTreeIndex < trees.size() - 1) {
			setCurrentTree(currentTreeIndex + 1);
		}
	}

	public void showPreviousTree() {
		if (currentTreeIndex > 0) {
			setCurrentTree(currentTreeIndex - 1);
		}
	}


    private List<Tree> trees = null;
	private int currentTreeIndex = 0;
}

package jebl.gui.trees.treeviewer_dev;

import jebl.evolution.trees.Tree;

import java.util.*;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class MultipleTreeViewer extends TreeViewer {

    public MultipleTreeViewer(TreePane treePane) {
        super(treePane);
    }

    public void setTree(Tree tree) {
        this.trees = new ArrayList<Tree>();
        trees.add(tree);
        setCurrentTree(tree);
    }

    public void setTrees(Collection<? extends Tree> trees) {
        this.trees = new ArrayList<Tree>(trees);
        super.setTree(this.trees.get(0));
    }

	public List<Tree> getTrees() {
		return trees;
	}

    public void setCurrentTree(Tree tree) {
        super.setTree(tree);
    }

    private List<Tree> trees = null;
}

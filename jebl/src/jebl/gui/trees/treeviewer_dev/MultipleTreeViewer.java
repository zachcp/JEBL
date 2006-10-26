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
        addTree(tree);
        showTree(0);
    }

    public void setTrees(Collection<? extends Tree> trees) {
        for (Tree tree : trees) {
            addTree(tree);
        }
        showTree(0);
    }

    public void addTrees(Collection<? extends Tree> trees) {
        int count = getTreeCount();
        for (Tree tree : trees) {
            addTree(tree);
        }
        showTree(count);
    }

    protected void addTree(Tree tree) {
        this.trees.add(tree);
    }

    public List<Tree> getTrees() {
        return trees;
    }

    public int getCurrentTreeIndex() {
        return currentTreeIndex;
    }

    public int getTreeCount() {
        if (trees == null) return 0;
        return trees.size();
    }

    public void showTree(int index) {
        super.setTree(trees.get(index));
        currentTreeIndex = index;
        fireTreeChanged();
    }

    public void showNextTree() {
        if (currentTreeIndex < trees.size() - 1) {
            showTree(currentTreeIndex + 1);
        }
    }

    public void showPreviousTree() {
        if (currentTreeIndex > 0) {
            showTree(currentTreeIndex - 1);
        }
    }


    private List<Tree> trees = new ArrayList<Tree>();
    private int currentTreeIndex = 0;
}

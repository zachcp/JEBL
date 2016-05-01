package jebl.gui.trees.treeviewer.decorators;

import jebl.evolution.graphs.Node;
import jebl.evolution.trees.Tree;

import java.awt.*;

/**
 * @author Andrew Rambaut
 */
public interface BranchDecorator {
    Paint getBranchPaint(Tree tree, Node node);
}

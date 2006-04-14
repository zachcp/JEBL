package jebl.gui.trees.treeviewer_dev.decorators;

import jebl.evolution.graphs.Node;
import jebl.evolution.trees.Tree;

import java.awt.*;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public interface BranchDecorator {
    Paint getBranchPaint(Tree tree, Node node);
}

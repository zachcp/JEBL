package jebl.gui.trees.treeviewer_dev.decorators;

import jebl.evolution.graphs.Node;
import jebl.evolution.trees.Tree;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class AttributeBranchDecorator implements BranchDecorator {
    public AttributeBranchDecorator(String attributeName) {
        this.attributeName = attributeName;
    }

    public Paint getBranchPaint(Tree tree, Node node) {
        Paint paint = getPaint(node.getAttribute(attributeName));
        if (paint == null) return Color.BLACK;
        return paint;
    }

    protected Paint getPaint(Object value) {
        if (value != null) {
            return Color.decode(value.toString());
        }
        return null;
    }

    protected final String attributeName;
}

package jebl.gui.trees.treeviewer.decorators;

import jebl.evolution.trees.Tree;
import jebl.evolution.graphs.Node;

import java.awt.*;
import java.util.Map;
import java.util.HashMap;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id$
 */
public class AttributeBranchDecorator implements BranchDecorator {
    public AttributeBranchDecorator(String attributeName, Map<Object, Paint> paintMap) {
        this.attributeName = attributeName;
        this.paintMap = paintMap;
    }

    public Paint getBranchPaint(Tree tree, Node node) {
        Paint paint = getPaint(node.getAttribute(attributeName));
        if (paint == null) return Color.BLACK;
        return paint;
    }

    protected Paint getPaint(Object value) {
        if (value != null) {
            return paintMap.get(value);
        }
        return null;
    }

    protected final String attributeName;

    protected Map<Object, Paint> paintMap = new HashMap<Object, Paint>();
}

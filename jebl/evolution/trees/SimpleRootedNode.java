package jebl.evolution.trees;

import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;
import jebl.util.AttributableHelper;

import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;


/**
 * A simple implementation of a rooted Node that is used by SimpleRootedTree.
 *
 * This class is package private.
 *
 * @author Andrew Rambaut
 * @author Joseph Heled
 * @version $Id$
 *
 */
class SimpleRootedNode extends Node {
    public SimpleRootedNode(Taxon taxon) {
        this.children = Collections.emptySet();
        this.taxon = taxon;
    }

    public SimpleRootedNode(Set<Node> children) {
        this.children = Collections.unmodifiableSet(children);
        this.taxon = null;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public Set<Node> getChildren() {
        return children;
    }

    public double getHeight() {
        return height;
    }

    // height above latest tip
    public void setHeight(double height) {
        this.height = height;
    }

    // length of branch to parent
    public double getLength() {
        return length >= 0 ? length : 1.0;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public int getDegree() {
        return children.size() + 1;
    }

    /**
     * For a rooted tree, getting the adjacencies is not the most efficient
     * operation as it makes a new set containing the children and the parent.
     * @return the adjacaencies
     */
    public Set<Node> getAdjacencies() {
        Set<Node> adjacencies = new HashSet<Node>();
        if (children != null) adjacencies.addAll(children);
        if (parent != null) adjacencies.add(parent);
        return adjacencies;
    }

    public Taxon getTaxon() {
        return taxon;
    }

    public void setAttribute(String name, Object value) {
        if (helper == null) {
            helper = new AttributableHelper();
        }
        helper.setAttribute(name, value);
    }

    public Object getAttribute(String name) {
        if (helper == null) {
            return null;
        }
        return helper.getAttribute(name);
    }

    public Set<String> getAttributeNames() {
        if (helper == null) {
            return Collections.emptySet();
        }
        return helper.getAttributeNames();
    }

    public Map<String, Object> getAttributeMap() {
        if (helper == null) {
            return Collections.emptyMap();
        }
        return helper.getAttributeMap();
    }

    // PRIVATE members

    private AttributableHelper helper = null;

    private final Set<Node> children;
    private final Taxon taxon;

    private Node parent;
    private double height;
    private double length;

}

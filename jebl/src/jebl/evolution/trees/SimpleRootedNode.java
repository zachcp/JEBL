package jebl.evolution.trees;

import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;

import java.util.*;


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
class SimpleRootedNode extends BaseNode {
    public SimpleRootedNode(Taxon taxon) {
        this.children = Collections.unmodifiableList(new ArrayList<Node>());
        this.taxon = taxon;
    }

    public SimpleRootedNode(List<Node> children) {
        this.children = Collections.unmodifiableList(children);
        this.taxon = null;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public List<Node> getChildren() {
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
    public List<Node> getAdjacencies() {
        List<Node> adjacencies = new ArrayList<Node>();
        if (children != null) adjacencies.addAll(children);
        if (parent != null) adjacencies.add(parent);
        return adjacencies;
    }

    public Taxon getTaxon() {
        return taxon;
    }

    private final List<Node> children;
    private final Taxon taxon;

    private Node parent;
    private double height;
    private double length;
}
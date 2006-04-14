package jebl.evolution.trees;

import jebl.evolution.graphs.Node;
import jebl.evolution.graphs.Edge;
import jebl.evolution.taxa.Taxon;

import java.util.*;

/**
 * A simple implementation of Node that is used by SimpleTree.
 *
 * This class is package private.
 *
 * @author Joseph Heled
 * @author Andrew Rambaut
 * @version $Id$
 *
 */
class SimpleNode extends BaseNode {

    /**
     * A tip having a taxon
     * @param taxon
     */
    public SimpleNode(Taxon taxon) {
        this.adjacencies = Collections.unmodifiableList(new ArrayList<Node>());
        this.taxon = taxon;
    }

    /**
     * An internal node.
     * @param adjacencies set of adjacent noeds
     */
    public SimpleNode(List<Node> adjacencies) {
        this.adjacencies = Collections.unmodifiableList(adjacencies);
        this.taxon = null;
    }

    /**
     * Add an adjacency.
     * @param node
     */
    public void addAdacency(Node node) {
        List<Node> a = new ArrayList<Node>(adjacencies);
        a.add(node);
        adjacencies = Collections.unmodifiableList(a);
    }

    public Taxon getTaxon() {
        return taxon;
    }

    public int getDegree() {
        return (adjacencies == null ? 0 : adjacencies.size());
    }

    public List<Node> getAdjacencies() {
        return adjacencies;
    }

    // PRIVATE members
    private List<Node> adjacencies;
    private final Taxon taxon;
}
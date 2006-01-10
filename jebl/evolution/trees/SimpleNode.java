package jebl.evolution.trees;

import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
        this.adjacencies = Collections.emptySet();
        this.taxon = taxon;
    }

    /**
     * An internal node.
     * @param adjacencies set of adjacent noeds
     */
    public SimpleNode(Set<Node> adjacencies) {
        this.adjacencies = Collections.unmodifiableSet(adjacencies);
        this.taxon = null;
    }

    /**
     * Add an adjacency.
     * @param node
     */
    public void addAdacency(Node node) {
        Set<Node> a = new HashSet<Node>(adjacencies);
        a.add(node);
        adjacencies = a;
    }

    public Taxon getTaxon() {
        return taxon;
    }

    public int getDegree() {
        return (adjacencies == null ? 0 : adjacencies.size());
    }

    public Set<Node> getAdjacencies() {
        return adjacencies;
    }

    // PRIVATE members
    private Set<Node> adjacencies;
    private final Taxon taxon;
}
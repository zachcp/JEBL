package jebl.evolution.trees;

import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;
import jebl.util.AttributableHelper;

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
class SimpleNode extends Node {

    public SimpleNode(Taxon taxon) {
        this.adjacencies = Collections.emptySet();
        this.taxon = taxon;
    }

    public SimpleNode(Set<Node> adjacencies) {
        this.adjacencies = Collections.unmodifiableSet(adjacencies);
        this.taxon = null;
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

    // Attributable IMPLEMENTATION

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
    private final Set<Node> adjacencies;
    private final Taxon taxon;

}
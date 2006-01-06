package jebl.evolution.trees;

import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;
import jebl.util.AttributableHelper;

import java.util.Collections;
import java.util.Set;
import java.util.Map;
import java.util.HashSet;

/**
 * Created by IntelliJ IDEA.
 * User: joseph
 * Date: 6/01/2006
 * Time: 11:16:07
 *
 * @author joseph
 * @version $Id$
 *
 */
class BaseNode extends Node {

    public BaseNode(Taxon taxon) {
        this.adjacencies = Collections.emptySet();
        this.taxon = taxon;
    }

    public BaseNode(Set<Node> adjacencies) {
        this.adjacencies = adjacencies;
        this.taxon = null;
    }

    public Taxon getTaxon() {
        return taxon;
    }

    public int getDegree() {
        return (adjacencies == null ? 0 : adjacencies.size());
    }

    public Set<Node> getAdjacencies() {
        return new HashSet<Node>(adjacencies);
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
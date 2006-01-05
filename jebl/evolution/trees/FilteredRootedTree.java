package jebl.evolution.trees;

import jebl.evolution.taxa.Taxon;
import jebl.evolution.graphs.Node;

import java.util.*;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id$
 */
public abstract class FilteredRootedTree implements RootedTree {

    public FilteredRootedTree(final RootedTree source) {
        this.source = source;
    }

	public RootedTree getSource() {
		return source;
	}

	public List<Node> getChildren(Node node) {
	    return source.getChildren(node);
    }

    public boolean hasHeights() {
        return source.hasHeights();
    }

    public double getHeight(Node node) {
        return source.getHeight(node);
    }

    public boolean hasLengths() {
        return source.hasLengths();
    }

    public double getLength(Node node) {
        return source.getLength(node);
    }

    public Node getParent(Node node) {
        return source.getParent(node);
    }

    public Node getRootNode() {
        return source.getRootNode();
    }

    public Set<Node> getExternalNodes() {
        return source.getExternalNodes();
    }

    public Set<Node> getInternalNodes() {
        return source.getInternalNodes();
    }

    public Node getNode(Taxon taxon) {
        return source.getNode(taxon);
    }

    public Set<Taxon> getTaxa() {
        return source.getTaxa();
    }

    public Taxon getTaxon(Node node) {
        return source.getTaxon(node);
    }

    public boolean isExternal(Node node) {
        return source.isExternal(node);
    }

    public Set<Node> getAdjacencies(Node node) {
        return source.getAdjacencies(node);
    }

    public double getEdgeLength(Node node1, Node node2) throws NoEdgeException {
        return source.getEdgeLength(node1, node2);
    }

    public Set<Node> getNodes() {
        return source.getNodes();
    }

    public Set<Node> getNodes(int degree) {
        return source.getNodes(degree);
    }

	// Attributable IMPLEMENTATION

	public void setAttribute(String name, Object value) {
		source.setAttribute(name, value);
	}

	public Object getAttribute(String name) {
		return source.getAttribute(name);
	}

	public Set<String> getAttributeNames() {
		return source.getAttributeNames();
	}

	public Map<String, Object> getAttributeMap() {
		return source.getAttributeMap();
	}

	// PRIVATE members

	protected final RootedTree source;
}

package jebl.evolution.trees;

import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;
import jebl.util.AttributableHelper;

import java.util.*;

/**
 * A basic implementation on an unrooted tree.
 *
 * @author Joseph Heled
 * @version $Id$
 *
 */

public class SimpleTree implements Tree {

    public SimpleTree() {}

    /**
     * Creates a new external node with the given taxon. See createInternalNode
     * for a description of how to use these methods.
     * @param taxon the taxon associated with this node
     * @return the created node reference
     */
    public Node createExternalNode(Taxon taxon) {
        SimpleNode node = new SimpleNode(taxon);
        externalNodes.put(taxon, node);
        return node;
    }

    /**
     * Once a SimpleTree has been created, the node stucture can be created by
     * calling createExternalNode and createInternalNode. First of all createExternalNode
     * is called giving Taxon objects for the external nodes. Then these are put into
     * sets and passed to createInternalNode to create new internal nodes.
     *
     * It is the caller responsibility to insure no cycles are created.
     *
     * @param children the child nodes of this nodes
     * @return the created node.
     */
    public Node createInternalNode(List<Node> children) {
        SimpleNode node = new SimpleNode(children);

        internalNodes.add(node);

        for( Node c : children ) {
            ((SimpleNode)c).addAdacency(node);
        }
        return node;
    }

    /**
     * A mapping between edges and edge length.
     */
    Map<HashPair, Double> edges = new HashMap<HashPair, Double>();

    /**
     * Set edge distance between two nodes.
     * @param node1
     * @param node2
     * @param distance
     */
    public void setEdge(Node node1, Node node2, double distance) {
        assert getAdjacencies(node1).contains(node2) && getAdjacencies(node2).contains(node1) && distance >= 0;

        edges.put(new HashPair<Node>(node1, node2), distance);
        edges.put(new HashPair<Node>(node2, node1), distance);
    }

    /**
     * Add a new edge between two existing nodes.
     * @param node1
     * @param node2
     * @param distance
     */
    public void addEdge(Node node1, Node node2, double distance) {
        assert !getAdjacencies(node1).contains(node2);

        ((SimpleNode)node1).addAdacency(node2);
        ((SimpleNode)node2).addAdacency(node1);
        setEdge(node1, node2, distance);
    }

    /* Graph IMPLEMENTATION */

    /**
     * @param node
     * @return the set of nodes that are attached by edges to the given node.
     */
    public List<Node> getAdjacencies(Node node) {
        return ((SimpleNode)node).getAdjacencies();
    }

    /**
     * @return a set of all nodes that have degree 1.
     *         These nodes are often refered to as 'tips'.
     */
    public Set<Node> getExternalNodes() {
        return new HashSet<Node>(externalNodes.values());
    }

    /**
     * @return a set of all nodes that have degree 2 or more.
     *         These nodes are often refered to as internal nodes.
     */
    public Set<Node> getInternalNodes() {
        return new HashSet<Node>(internalNodes);
    }

    /**
     * @return the set of taxa associated with the external
     *         nodes of this tree. The size of this set should be the
     *         same as the size of the external nodes set.
     */
    public Set<Taxon> getTaxa() {
        return new HashSet<Taxon>(externalNodes.keySet());
    }
    /**
     * @param node the node whose associated taxon is being requested.
     * @return the taxon object associated with the given node, or null
     *         if the node is an internal node.
     */
    public Taxon getTaxon(Node node) {
        return ((SimpleNode)node).getTaxon();
    }

    /**
     * @param node the node
     * @return true if the node is of degree 1.
     */
    public boolean isExternal(Node node) {
        return ((SimpleNode)node).getDegree() == 1;
    }

    /**
     * @param taxon the taxon
     * @return the external node associated with the given taxon, or null
     *         if the taxon is not a member of the taxa set associated with this tree.
     */
    public Node getNode(Taxon taxon) {
        return externalNodes.get(taxon);
    }

    /**
     * @param node1
     * @param node2
     * @return the length of the edge connecting node1 and node2.
     * @throws NoEdgeException if the nodes are not directly connected by an edge.
     */
    public double getEdgeLength(Node node1, Node node2) throws NoEdgeException {
        Double e = edges.get(new HashPair<Node>(node1, node2));
        if( e == null ) {
            // not connected
            throw new NoEdgeException();
        }
        return e;
    }

    /**
     * @return the set of all nodes in this graph.
     */
    public Set<Node> getNodes() {
        Set<Node> nodes = new HashSet<Node>(internalNodes);
        nodes.addAll(externalNodes.values());
        return nodes;
    }

    /**
     * @param degree the number of edges connected to a node
     * @return a set containing all nodes in this graph of the given degree.
     */
    public Set<Node> getNodes(int degree) {
        Set<Node> nodes = new HashSet<Node>();
        for (Node node : getNodes()) {
            if (((SimpleNode)node).getDegree() == degree) nodes.add(node);
        }
        return nodes;
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

    public void removeAttribute(String name) {
        if( helper != null ) {
            helper.removeAttribute(name);
        }
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
    private final Set<Node> internalNodes = new HashSet<Node>();
    private final Map<Taxon, Node> externalNodes = new HashMap<Taxon, Node>();
}
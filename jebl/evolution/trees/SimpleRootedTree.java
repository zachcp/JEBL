package jebl.evolution.trees;

import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;

import java.util.*;

/**
 * A simple, and initially immutable rooted tree implementation. All returned collections
 * are defensively copied. The implementation of Node is private. A number of methods are
 * provided that can be used to construct a tree (createExternalNode & createInternalNode).
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id$
 */
public class SimpleRootedTree implements RootedTree {

    public SimpleRootedTree() {
    }

    /**
     * Creates a new external node with the given taxon and height. See createInternalNode
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
     * Once a SimpleRootedTree has been created, the node stucture can be created by
     * calling createExternalNode and createInternalNode. First of all createExternalNode
     * is called giving Taxon objects for the external nodes. Then these are put into
     * sets and passed to createInternalNode to create a parent of these nodes. The
     * last node created using createInternalNode is automatically the root so when
     * all the nodes are created, the tree is complete.
     *
     * @param children the child nodes of this nodes
     * @return the created node reference
     */
    public Node createInternalNode(Set<Node> children) {
        SimpleNode node = new SimpleNode(children);

        for (Node child : children) {
            ((SimpleNode)child).setParent(node);
        }

        internalNodes.add(node);

        rootNode = node;
        return node;
    }


    /**
     * @param node the node whose height is being set
     * @param height the height
     */
    public void setHeight(Node node, double height) {
        lengthsKnown = false;
        heightsKnown = true;
        ((SimpleNode)node).setHeight(height);
    }

    /**
     * @param node the node whose branch length (to its parent) is being set
     * @param length the length
     */
    public void setLength(Node node, double length) {
        heightsKnown = false;
        lengthsKnown = true;
        ((SimpleNode)node).setLength(length);
    }

    /**
     * @param node the node whose children are being requested.
     * @return the set of nodes that are the children of the given node.
     *         The set may be empty for a terminal node (a tip).
     */
    public Set<Node> getChildren(Node node) {
        return new HashSet<Node>(((SimpleNode)node).getChildren());
    }

    /**
     * @param node the node whose height is being requested.
     * @return the height of the given node. The height will be
     *         less than the parent's height and greater than it children's heights.
     */
    public double getHeight(Node node) {
        if (!heightsKnown) calculateNodeHeights();
        return ((SimpleNode)node).getHeight();
    }

    /**
     * @param node the node whose branch length (to its parent) is being requested.
     * @return the length of the branch to the parent node (0.0 if the node is the root).
     */
    public double getLength(Node node) {
        if (!lengthsKnown) calculateBranchLengths();
        return ((SimpleNode)node).getLength();
    }

    /**
     * @param node the node whose parent is requested
     * @return the parent node of the given node, or null
     *         if the node is the root node.
     */
    public Node getParent(Node node) {
        return ((SimpleNode)node).getParent();
    }

    /**
     * The root of the tree has the largest node height of
     * all nodes in the tree.
     *
     * @return the root of the tree.
     */
    public Node getRootNode() {
        return rootNode;
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
        return ((SimpleNode)node).getChildren().size() == 0;
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
     * @param node
     * @return the set of nodes that are attached by edges to the given node.
     */
    public Set<Node> getAdjacencies(Node node) {
        return ((SimpleNode)node).getAdjacencies();
    }

    /**
     * @param node1
     * @param node2
     * @return the length of the edge connecting node1 and node2.
     * @throws jebl.evolution.graphs.Graph.NoEdgeException
     *          if the nodes are not directly connected by an edge.
     */
    public double getEdgeLength(Node node1, Node node2) throws NoEdgeException {
        if (((SimpleNode)node1).getParent() == node2) {

            return ((SimpleNode)node2).getHeight() - ((SimpleNode)node1).getHeight();
        } else if (((SimpleNode)node2).getParent() == node1) {

            return ((SimpleNode)node1).getHeight() - ((SimpleNode)node2).getHeight();

        } else {
            throw new NoEdgeException();
        }
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

    /**
     * Set the node heights from the current branch lengths.
     */
    private void calculateNodeHeights() {

        if (!lengthsKnown) {
            throw new IllegalArgumentException("Can't calculate node heights because branch lengths not known");
        }

        nodeLengthsToHeights(rootNode, 0.0);

        double maxHeight = 0.0;
        SimpleNode node;
        for (Node externalNode : getExternalNodes()) {
            if (((SimpleNode)externalNode).getHeight() > maxHeight) {
                maxHeight = ((SimpleNode)externalNode).getHeight();
            }
        }

        for (Node node1 : getNodes()) {
            ((SimpleNode)node1).setHeight(maxHeight - ((SimpleNode)node1).getHeight());
        }

        heightsKnown = true;
    }

    /**
     * Set the node heights from the current node branch lengths. Actually
     * sets distance from root so the heights then need to be reversed.
     */
    private void nodeLengthsToHeights(SimpleNode node, double height) {

        double newHeight = height;

        if (node.getLength() > 0.0) {
            newHeight += node.getLength();
        }

        node.setHeight(newHeight);

        for (Node child : node.getChildren()) {
            nodeLengthsToHeights((SimpleNode)child, newHeight);
        }
    }

    /**
     * Calculate branch lengths from the current node heights.
     */
    protected void calculateBranchLengths() {

        if (!lengthsKnown) {
            throw new IllegalArgumentException("Can't calculate branch lengths because node heights not known");
        }

        nodeHeightsToLengths(rootNode, getHeight(rootNode));

        lengthsKnown = true;
    }

    /**
     * Calculate branch lengths from the current node heights.
     */
    private void nodeHeightsToLengths(SimpleNode node, double height) {

        node.setLength(height - node.getHeight());

        for (Node child : node.getChildren()) {
            nodeHeightsToLengths((SimpleNode)child, node.getHeight());
        }

    }

    private SimpleNode rootNode = null;
    private final Set<Node> internalNodes = new HashSet<Node>();
    private final Map<Taxon, Node> externalNodes = new HashMap<Taxon, Node>();

    private boolean heightsKnown = false;
    private boolean lengthsKnown = false;

    private class SimpleNode extends Node {

        public SimpleNode(Taxon taxon) {
            this.children = Collections.emptySet();
            this.taxon = taxon;
        }

        public SimpleNode(Set<Node> children) {
            this.children = children;
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

        public Taxon getTaxon() {
            return taxon;
        }

        public double getHeight() {
            return height;
        }

        public void setHeight(double height) {
            this.height = height;
        }

        public double getLength() {
            return length;
        }

        public void setLength(double length) {
            this.length = length;
        }

        public int getDegree() {
            int degree = (parent == null ? 0 : 1);
            degree += (children == null ? 0 : children.size());
            return degree;
        }

        public Set<Node> getAdjacencies() {
            Set<Node> adjacencies = new HashSet<Node>();
            if (children != null) adjacencies.addAll(children);
            if (parent != null) adjacencies.add(parent);
            return adjacencies;
        }

        private Node parent;
        private final Set<Node> children;
        private final Taxon taxon;
        private double height;
        private double length;

    }
}

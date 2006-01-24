package jebl.evolution.trees;

import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;
import jebl.util.AttributableHelper;

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
        SimpleRootedNode node = new SimpleRootedNode(taxon);
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
        SimpleRootedNode node = new SimpleRootedNode(children);

        for (Node child : children) {
            ((SimpleRootedNode)child).setParent(node);
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

        // If a single height of a single node is set then
        // assume that all nodes have heights and by extension,
        // branch lengths as well as these will be calculated
        // from the heights
        hasLengths = true;
        hasHeights = true;

        ((SimpleRootedNode)node).setHeight(height);
    }

    /**
     * @param node the node whose branch length (to its parent) is being set
     * @param length the length
     */
    public void setLength(Node node, double length) {
        heightsKnown = false;
        lengthsKnown = true;

        // If a single length of a single branch is set then
        // assume that all branch have lengths and by extension,
        // node heights as well as these will be calculated
        // from the lengths
        hasLengths = true;
        hasHeights = true;

        ((SimpleRootedNode)node).setLength(length);
    }

    /**
     * @param node the node whose children are being requested.
     * @return the list of nodes that are the children of the given node.
     *         The list may be empty for a terminal node (a tip).
     */
    public List<Node> getChildren(Node node) {
        return new ArrayList<Node>(((SimpleRootedNode)node).getChildren());
    }

    /**
     * @return Whether this tree has node heights available
     */
    public boolean hasHeights() {
        return hasHeights;
    }

    /**
     * @param node the node whose height is being requested.
     * @return the height of the given node. The height will be
     *         less than the parent's height and greater than it children's heights.
     */
    public double getHeight(Node node) {
        if (!hasHeights) throw new IllegalArgumentException("This tree has no node heights");
        if (!heightsKnown) calculateNodeHeights();
        return ((SimpleRootedNode)node).getHeight();
    }

    /**
     * @return Whether this tree has branch lengths available
     */
    public boolean hasLengths() {
        return hasLengths;
    }

    /**
     * @param node the node whose branch length (to its parent) is being requested.
     * @return the length of the branch to the parent node (0.0 if the node is the root).
     */
    public double getLength(Node node) {
        if (!hasLengths) throw new IllegalArgumentException("This tree has no branch lengths");
        if (!lengthsKnown) calculateBranchLengths();
        return ((SimpleRootedNode)node).getLength();
    }

    /**
     * @param node the node whose parent is requested
     * @return the parent node of the given node, or null
     *         if the node is the root node.
     */
    public Node getParent(Node node) {
        return ((SimpleRootedNode)node).getParent();
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
        return ((SimpleRootedNode)node).getTaxon();
    }

    /**
     * @param node the node
     * @return true if the node is of degree 1.
     */
    public boolean isExternal(Node node) {
        return ((SimpleRootedNode)node).getChildren().size() == 0;
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
        return ((SimpleRootedNode)node).getAdjacencies();
    }

    /**
     * @param node1
     * @param node2
     * @return the length of the edge connecting node1 and node2.
     * @throws jebl.evolution.graphs.Graph.NoEdgeException
     *          if the nodes are not directly connected by an edge.
     */
    public double getEdgeLength(Node node1, Node node2) throws NoEdgeException {
        if (((SimpleRootedNode)node1).getParent() == node2) {
	        if (heightsKnown) {
		        return ((SimpleRootedNode)node2).getHeight() - ((SimpleRootedNode)node1).getHeight();
	        } else {
		        return ((SimpleRootedNode)node1).getLength();
	        }
        } else if (((SimpleRootedNode)node2).getParent() == node1) {
            if (heightsKnown) {
	            return ((SimpleRootedNode)node1).getHeight() - ((SimpleRootedNode)node2).getHeight();
            } else {
	            return ((SimpleRootedNode)node2).getLength();
            }
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
            // Account for no anncesstor of root, assumed by default in getDegree
            final int deg = ((SimpleRootedNode)node).getDegree() - ((node == rootNode) ? 1 : 0);
            if (deg == degree) nodes.add(node);
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
        for (Node externalNode : getExternalNodes()) {
            if (((SimpleRootedNode)externalNode).getHeight() > maxHeight) {
                maxHeight = ((SimpleRootedNode)externalNode).getHeight();
            }
        }

        for (Node node : getNodes()) {
            ((SimpleRootedNode)node).setHeight(maxHeight - ((SimpleRootedNode)node).getHeight());
        }

        heightsKnown = true;
    }

    /**
     * Set the node heights from the current node branch lengths. Actually
     * sets distance from root so the heights then need to be reversed.
     */
    private void nodeLengthsToHeights(SimpleRootedNode node, double height) {

        double newHeight = height;

        if (node.getLength() > 0.0) {
            newHeight += node.getLength();
        }

        node.setHeight(newHeight);

        for (Node child : node.getChildren()) {
            nodeLengthsToHeights((SimpleRootedNode)child, newHeight);
        }
    }

    /**
     * Calculate branch lengths from the current node heights.
     */
    protected void calculateBranchLengths() {

        if (!hasLengths) {
            throw new IllegalArgumentException("Can't calculate branch lengths because node heights not known");
        }

        nodeHeightsToLengths(rootNode, getHeight(rootNode));

        lengthsKnown = true;
    }

    /**
     * Calculate branch lengths from the current node heights.
     */
    private void nodeHeightsToLengths(SimpleRootedNode node, double height) {
        final double h = node.getHeight();
        node.setLength(h >= 0 ? height - h : 1);

        for (Node child : node.getChildren()) {
            nodeHeightsToLengths((SimpleRootedNode)child, node.getHeight());
        }

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

    private SimpleRootedNode rootNode = null;
    private final Set<Node> internalNodes = new HashSet<Node>();
    private final Map<Taxon, Node> externalNodes = new HashMap<Taxon, Node>();

    private boolean heightsKnown = false;
    private boolean lengthsKnown = false;

    private boolean hasHeights = false;
    private boolean hasLengths = false;
}
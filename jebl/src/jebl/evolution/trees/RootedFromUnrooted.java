package jebl.evolution.trees;

import jebl.evolution.graphs.Edge;
import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;

import java.util.*;

/**
 * Roots a tree. This class works as a wrapper over any tree to root it without modifying it. Despite the name, this
 * works to re-root a rooted tree too. There are two
 * constructors, one which roots the tree at any internal node, the other roots the tree between any two
 * internal nodes. Be aware that rooting between nodes where one of them has less than 3 adjacencies may
 * be problematic when converting back from the Newick format.
 *
 * @author Joseph Heled
 * @version $Id$
 *
 */

public class RootedFromUnrooted implements RootedTree {
	/**
	 * The unrooted tree
	 */
	private Tree source;

	/**
	 * Root of rooted tree. Either an existing internal node or a new "synthetic" node.
	 */
	private Node root;
	/**
	 * Maps each nodes to its parent.
	 */
	private Map<Node, Node> parents;

	/**
	 *  Children of the synthetic root (when rooted between nodes)
	 */
	private Node topLeft, topRight;
	/**
	 * branch lengths from synthetic root to its children (when rooted between nodes)
	 */
	private double rootToLeft, rootToRight;
	private double rootHeight;
	private boolean intentUnrooted;

	/**
	 * Maps each node to the length of the path from that node to the root
	 */
	private Map<Node, Double> lengthsFromNodeToRoot;

	/**
	 * Set <arg>parent</arg> as parent of <arg>node</arg>, and recursivly set parents for node subtree
	 * (whose root is parent)
	 * @param node
	 * @param parent
	 */
    private void setParent(Node node, Node parent) {
		parents.put(node, parent);
		for( Node adj : source.getAdjacencies(node) ) {
			if( adj != parent && ! (node == topLeft && adj == topRight) && !(node == topRight && adj == topLeft) ) {
                setParent(adj, node);
			}
		}
	}

	/**
	 * Root tree at some internal node.
	 *
	 * @param source tree to root
	 * @param root  internal node to root at
	 * @param intentUnrooted
	 */
	public RootedFromUnrooted(Tree source, Node root, boolean intentUnrooted) {
		this.source = source;
		this.root = root;
		this.intentUnrooted = intentUnrooted;
		topLeft = topRight  = null;
		rootToLeft = rootToRight = 0.0;
		parents = new LinkedHashMap<Node, Node>();
		for( Node adj : source.getAdjacencies(root) ) {
            setParent(adj, root);
		}
		populateLengthsFromNodeToRoot();
	}

	/**
	 * Root source by creating a new internal node whose children are (the adjacent) left and right.
	 * @param source
	 * @param left
	 * @param right
	 * @param fromLeft branch from new root to left node.
	 */
	public RootedFromUnrooted(Tree source, Node left, Node right, double fromLeft) {
		this.source = source;
		intentUnrooted = false;
		topLeft = left;
		topRight = right;
		rootToLeft = fromLeft;
		try {
			rootToRight = source.getEdgeLength(left, right) - rootToLeft;
		} catch (NoEdgeException e) {
			// bug
		}
		parents = new LinkedHashMap<Node, Node>();

		// This is just a handle used to refer to the root so create the simplest possible implementation...
        root = new BaseNode() { public int getDegree() { return 2; } };

		parents.put(root, null);
        setParent(left, root);
		setParent(right, root);
		populateLengthsFromNodeToRoot();
    }

	/**
	 * Populates the lengthsFromNodeToRoot map. Steps down along branches from the root, saving the
	 * distances to each node. Does an iterative pre-order traversal of the tree.
	 */
	private void populateLengthsFromNodeToRoot() {
		lengthsFromNodeToRoot = new HashMap<Node, Double>();
		lengthsFromNodeToRoot.put(root, 0.0);
		double maxHeight = 0.0;
		Stack<Node> stack = new Stack<Node>();
		for (Node rootChild : this.getChildren(root)) {
			stack.push(rootChild);
		}

		while (!stack.empty()) {
			Node current = stack.peek();
			Double currentDistance = lengthsFromNodeToRoot.get(current);
			if (currentDistance == null) { //no distance worked out for this node yet. Its parent is guaranteed to have a distance in the map
				Double parentDistance = lengthsFromNodeToRoot.get(this.getParent(current));
				if (parentDistance == null) {
					throw new IllegalStateException("Each parent should have a height already :O");
				}
				currentDistance = parentDistance + getLength(current);
				lengthsFromNodeToRoot.put(current, currentDistance);

				//If is external, we keep track of the max height in the tree
				if (this.isExternal(current)) {
					if (currentDistance > maxHeight) {
						maxHeight = currentDistance;
					}
				} else {
					//Else we add the children to the stack to be processed
					for (Node child : this.getChildren(current)) {
						stack.push(child);
					}
				}
			} else { //revisiting a node in the stack with a distance means its children are all done. Can remove it now
				stack.pop();
			}
		}
		rootHeight = maxHeight;
	}

	public List<Node> getChildren(Node node) {
		ArrayList<Node> s = new ArrayList<Node>(getAdjacencies(node));
		if( node != root ) {
			s.remove(getParent(node));
		}
		return s;
	}

	public boolean hasHeights() {
		return true;
	}

	/**
	 * Calculates height as getHeight(root) - getPathLength(root, node)
	 * @param node the node whose height is being requested.
	 * @return
	 */
	public double getHeight(Node node) {
		if( node == root ) {
			return rootHeight;
		}

		Double toRoot = lengthsFromNodeToRoot.get(node);
		if (toRoot == null) {
			throw new IllegalStateException("Shouldn't have a node with no lengthToRoot calculated!");
		}

		return rootHeight - toRoot;
	}

	public boolean hasLengths() {
		return true;
	}

	public double getLength(Node node) {
		if( node == root ) return 0.0;
		if( node == topLeft ) return rootToLeft;
		if( node == topRight ) return rootToRight;
		double l = 0.0;
		try {
			l = source.getEdgeLength(node, getParent(node));
		} catch (NoEdgeException e) {
			// bug, should not happen
		}
		return l;
	}

	public Node getParent(Node node) {
		return parents.get(node);
	}

    public Node getRootNode() {
        return root;
    }

	public boolean conceptuallyUnrooted() {
		return intentUnrooted;
	}

	public Set<Node> getExternalNodes() {
		return source.getExternalNodes();
	}

	public Set<Node> getInternalNodes() {
		HashSet<Node> s = new LinkedHashSet<Node>(source.getInternalNodes());
		s.add(root);
		return s;
	}

	public Set<Taxon> getTaxa() {
		return source.getTaxa();
	}

	public Taxon getTaxon(Node node) {
		if( node == root ) return null;
		return source.getTaxon(node);
	}

	public boolean isExternal(Node node) {
		return node != root && source.isExternal(node);
	}

	public Node getNode(Taxon taxon) {
		return source.getNode(taxon);
	}

	public void renameTaxa(Taxon from, Taxon to) {
		source.renameTaxa(from, to);
	}

	/**
	 * Returns a list of edges connected to this node
	 *
	 * @param node
	 * @return the set of nodes that are attached by edges to the given node.
	 */
	public List<Edge> getEdges(Node node) {
		return source.getEdges(node);
	}

	public Node[] getNodes(Edge edge) {
		return source.getNodes(edge);
	}

	public List<Node> getAdjacencies(Node node) {
		// special case when syntetic root
		if( topLeft != null ) {
			if( node == root ) {
				Node[] d = {topLeft, topRight};
				return Arrays.asList(d);
			}
			if( node == topLeft || node == topRight ) {
				List<Node> s = new ArrayList<Node>(source.getAdjacencies(node));
				s.remove(node == topLeft ? topRight : topLeft);
				s.add(root);
				return s;
			}
		}
		return source.getAdjacencies(node);
	}

	public double getEdgeLength(Node node1, Node node2) throws NoEdgeException {
		// special case when syntetic root
		if( topLeft != null ) {
			if( node2 == root ) {
				Node tmp = node1;
				node1 = node2;
				node2 = tmp;
			}
			if( node1 == root ) {
				if( ! (node2 == topLeft || node2 == topRight) ) {
					throw new NoEdgeException();
				}
				return node2 == topLeft ? rootToLeft : rootToRight;
			}
		}
		return source.getEdgeLength(node1, node2);
	}

	public Edge getEdge(Node node1, Node node2) throws NoEdgeException {
		return source.getEdge(node1, node2);
	}

	public Set<Node> getNodes() {
		Set<Node> nodes = new LinkedHashSet<Node>(getInternalNodes());
		nodes.addAll(getExternalNodes());
		if( topLeft != null ) {
			nodes.add(root);
		}
		return nodes;
	}

	/**
	 * @return the set of all edges in this graph.
	 */
	public Set<Edge> getEdges() {
		return source.getEdges();
	}

	/**
	 * The set of external edges.
	 * @return the set of external edges.
	 */
	public Set<Edge> getExternalEdges() {
		return source.getExternalEdges();
	}

	/**
	 * The set of internal edges.
	 * @return the set of internal edges.
	 */
	public Set<Edge> getInternalEdges() {
		return source.getInternalEdges();
	}

	public Set<Node> getNodes(int degree) {
		Set<Node> nodes = source.getNodes(degree);
		if( degree == 2 ) {
			nodes.add(root);
		}
		return nodes;
	}

	public boolean isRoot(Node node) {
		return node == root;
	}

	// Attributable IMPLEMENTATION

	public void setAttribute(String name, Object value) {
		source.setAttribute(name, value);
	}

	public Object getAttribute(String name) {
		return source.getAttribute(name);
	}

	public void removeAttribute(String name) {
		source.removeAttribute(name);
	}

	public Set<String> getAttributeNames() {
		return source.getAttributeNames();
	}

	public Map<String, Object> getAttributeMap() {
		return source.getAttributeMap();
	}
}
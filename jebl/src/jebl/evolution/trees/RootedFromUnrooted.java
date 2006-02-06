package jebl.evolution.trees;

import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;

import java.util.*;

/**
 * Root an unrooted tree. This class works as a wrapper over any tree to root it. There are two
 * constructors, one which roots the tree at any internal node, the other roots the tree between any two
 * internal nodes. Be aware that rooting between nodes where one of them has less than 3 adjacencies may
 * be problematic when converting back from the Newick format.
 *
 * @author Joseph Heled
 *
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
     * Maps each nodes to it's parent.
     */
    private Map<Node, Node> parents;

    /**
     *  Childern of the synthetic root (when rooted between nodes)
     */
    private Node topLeft, topRight;
    /**
     * branch lengths from synthetic root to it's children (when rooted between nodes)
     */
    private double rootToLeft, rootToRight;

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
     * Root tree source at root.
     * @param source
     * @param root
     */
    public RootedFromUnrooted(Tree source, Node root) {
       this.source = source;
       this.root = root;
       topLeft = topRight  = null;
       rootToLeft = rootToRight = 0.0;
       parents = new HashMap<Node, Node>();
       for( Node adj : source.getAdjacencies(root) ) {
           setParent(adj, root);
       }
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
        topLeft = left;
        topRight = right;
        rootToLeft = fromLeft;
        try {
            rootToRight = source.getEdgeLength(left, right) - rootToLeft;
        } catch (NoEdgeException e) {
            // bug
        }
        parents = new HashMap<Node, Node>();
        root = new SimpleNode(new ArrayList<Node>());
        parents.put(root, null);
        setParent(left, root);
        setParent(right, root);
    }

    public List<Node> getChildren(Node node) {
        ArrayList<Node> s = new ArrayList<Node>(getAdjacencies(node));
        if( node != root ) {
            s.remove(getParent(node));
        }
        return s;
    }

    public boolean hasHeights() {
        return false;
    }

    public double getHeight(Node node) {
        return 0;
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

    public Set<Node> getExternalNodes() {
        return source.getExternalNodes();
    }

    public Set<Node> getInternalNodes() {
        HashSet<Node> s = new HashSet<Node>(source.getInternalNodes());
        s.add(root);
        return s;
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

    public Node getNode(Taxon taxon) {
        return source.getNode(taxon);
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

    public Set<Node> getNodes() {
       Set<Node> nodes = new HashSet<Node>(getInternalNodes());
       nodes.addAll(getExternalNodes());
        if( topLeft != null ) {
            nodes.add(root);
        }
       return nodes;
    }

    public Set<Node> getNodes(int degree) {
        Set<Node> nodes = source.getNodes(degree);
        if( degree == 2 ) {
            nodes.add(root);
        }
        return nodes;
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
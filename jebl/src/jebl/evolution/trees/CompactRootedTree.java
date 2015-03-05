package jebl.evolution.trees;

import jebl.evolution.graphs.Edge;
import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;
import jebl.util.Attributable;

import java.util.*;

/**
 * A memory efficient rooted tree.
 *
 *   - Uses a compact representation for the tree structure based primarily on indices instead of pointers
 *     and objects
 *   - Minimize penalty for unused features. Trees not using attributes or edges do not require additional
 *     per node/edge memory.
 *
 * Limitations:
 *    - Some of the accessors are slower, typically the ones getting all nodes, all edges, all internal
 *      nodes etc. Traversing the tree and handling attributes speed should be fine (compared to SimpkeRootedTree)
 *
 * @author Joseph Heled
 * @version $Id$
 *
 */
public class CompactRootedTree extends AttributableImp implements RootedTree {
    private static final int MASK = Integer.MIN_VALUE;
    /**
     * Array of all nodes.
     *
     * Ordered by levels. i.e. for the tree (a, ((e,f), d) ) the layout is
     *
     *   0    1  2 3 4 5 6
     *  root  a  b c d e f
     *
     * where c = (e,f) and b = ((e,f), d)
     *
     * The major advantage is that all decendents of a node are grouped together.
     * In subsequent comments nodes will be reffered to by their index in the array above.
     */
    SimpleRootedNode[] nodes;

    /**
     *  Index of parent node of x is parent[x].
     *  For the example tree, this would be (- 0 0 2 2 3 3)
     */
    int[] parent;

    /**
     *  Decendents of node x start at sons[x]
     *  For the example tree, this would be (1 - 3 5 - - -)
     */
    int[] sons;

    /**
     * Number of Decendents of node x is noSons[x]
     * For the example tree, this would be (2 0 2 2 0 0 0)
     *
     * Actually the above is true only for internal nodes. External nodes contain an index into
     * the taxa array indicating where the taxon for this node is stored. A 1 bit is added at the most
     * significant place to separate internal from external nodes. So in fact the array would look like
     * that (2 MASK|0 2 2 MASK|1 MASK|2 MASK|3), where taxa[0] holds a's taxon etc.
     */
    int[] noSons;

    /** Tree has node heights information */
    boolean hasHeights;

    /** Tree has branch length information */
    boolean hasLengths;

    /**
     * Height of node x is heights[x]
     *
     * Keep only heights since it is easy to compute length given easy access to
     * parent height. (If tree has only lengths, those are stored insted).
     */
    double[] heights;

    /**
     * Taxon for all external nodes.
     *
     * Indices linking nodes to taxa are stored in noSons.
     */
    Taxon[] taxa;

    /**
     * Graph edges.
     *
     * Empty until referenced. edges are indexed according to node. edges[x] is the
     * edge between x and it's parent (x != root)
     */
    SimpleRootedEdge[] edges;

    private boolean conceptuallyUnrooted = false;

    /**
     * Attributes for tree, nodes and edges.
     *
     * attributs for node x are in all.get(x), for tree in all.get(#nodes), and for edges
     * in all.get(#nodes + edge index) (note that edge index always > 0)
     */
    Map<Integer, Map<String, Object> > all = null;

    /**
     * Test if attribute map exists for index
     * @param index
     * @return true if map exists
     */
    private boolean hasAttributeMap(int index) {
        return all != null && all.get(index) != null;
    }

    /**
     * Attribute map for index (node, tree or edge).
     *
     * @param index
     * @return attribute map
     */
    private Map<String, Object> aMap(int index) {
        if( all == null ) {
            all = new LinkedHashMap<Integer, Map<String, Object>>();
        }

        Map<String, Object> map = all.get(index);
        if( map == null ) {
            map = new LinkedHashMap<String, Object>();
            all.put(index, map);
        }
        return map;
    }

    /**
     * A minimal rooted node.
     */
    private class SimpleRootedNode extends AttributableImp implements Node {
        // Index of node in tree nodes array.
        private int index;

        SimpleRootedNode(int index) {
            this.index = index;
        }

        public int getDegree() {
            return nSons(index) + (this==getRootNode()?0:1);
        }

        Map<String, Object> getExistingMap() {
            if( hasAttributeMap(index) ) {
                return aMap(index);
            }
            return null;
        }

        Map<String, Object> getMap() {
            return aMap(index);
        }
    }

    // Number of decendents.
    private int nSons(int index) {
        // Take care of external node bit.
        final int n = noSons[index];
        if( (n & MASK) == 0) {
          return n;
        }
        return 0;
    }

    /** Minimal edge object */
    private class SimpleRootedEdge extends AttributableImp implements Edge {
       /** edge is between node 'index' and it's parent
        *
        * As a consequence, index > 0 always.
        */
       private int index;

        SimpleRootedEdge(int index) {
            this.index = index;
        }

        Map<String, Object> getExistingMap() {
            final int i = nodes.length + index;
            if( hasAttributeMap(i) ) {
                return aMap(i);
            }
            return null;
        }

        Map<String, Object> getMap() {
            return aMap(nodes.length + index);
        }

        public double getLength() {
            return heights[parent[index]] - heights[index];
        }
    }

    /**
     *  Do all the hard work.
     *
     * @param t
     */
    public CompactRootedTree(RootedTree t) {
        conceptuallyUnrooted = t.conceptuallyUnrooted();

        final int nNodes = t.getNodes().size();
        nodes = new SimpleRootedNode[nNodes];
        parent = new int[nNodes];
        sons = new int[nNodes];
        noSons = new int[nNodes];
        heights = new double[nNodes];
        hasHeights = t.hasHeights();
        hasLengths = t.hasLengths();
        taxa = new Taxon[t.getTaxa().size()];
        edges = null;

        final Node rootNode = t.getRootNode();
        // nodes to be inserted to the tree, all the same distance from root.
        List<Node> level = new ArrayList<Node>();

        // nodes to be inserted in next iteration (decendents of nodes in 'level')
        List<Node> nlevel = new ArrayList<Node>();

        // start with roo node
        level.add(rootNode);
        // where next inserted node goes
        int iNode = 0;
        // where decendents (if any) of next node goes
        int decendentslStart = 1;
        // where taxa (if external) of next node goes
        int nTax = 0;

        while( level.size() > 0 ) {
            nlevel.clear();
            for( Node n : level ) {
                int ns =  t.getChildren(n).size();

                if( hasHeights ) {
                    heights[iNode] = t.getHeight(n);
                } else if( hasLengths ) {
                    heights[iNode] = ((iNode == 0) ? 0.0 : t.getLength(n));
                }

                nodes[iNode] = new SimpleRootedNode(iNode);
                sons[iNode] = ns > 0 ? decendentslStart : 0;
                // descendentslStart = first child of node; l = currentChildNodes --> iterating over the whole group
                // (eg. n = 2, dStart = 3, n has 2 children --> parent[(3+0)]=2; parent[(3+1)]=2
                for(int l = 0; l < ns; ++l) {
                    if (decendentslStart + l+1 >= parent.length) {
                        String message = "This tree has more descendants than expected. \n" +
                                "If there are two operations being performed on this tree simultaneously, this might be what caused the error.\n" +
                                "If that wasn't the case and to help us resolve this issue, please send the file to us using Help > Contact Support from the menu.\n\n" +
                                "Trying to assign " + ns + " children to node " + decendentslStart + ", but this tree is supposed to have only " + nNodes + "(="+parent.length+ ") nodes in total.\n" +
                                "The tree has "+ t.getExternalNodes().size() + " external nodes and " + t.getInternalNodes().size() + " internal nodes\n" +
                                "Current node " + decendentslStart + " is an " + (t.isExternal(n)?"external":"internal") + " node and has " + ns + " children.\n" +
                                "Tried to add child " + (l+1) + " (node " + (decendentslStart+l) +"), which is " + (t.isExternal(t.getChildren(n).get(l))?"external":"internal");
                        for (int node : parent){
                            message += "\nParent/Child: " + parent[node] + "/" + node;
                        }
                        throw new IllegalStateException(message);
                    }
                    parent[decendentslStart + l] = iNode;
                }

                // dStart gets set to next 'first' childNode
                decendentslStart += ns;

                if( ns == 0 ) {
                    // external, set taxon and mark it.
                    assert t.isExternal(n);
                    taxa[nTax] = t.getTaxon(n);
                    ns = MASK | nTax;
                    ++nTax;
                }
                noSons[iNode] = ns;

                // set node attributes
                final Map<String, Object> map = n.getAttributeMap();
                if( map.size() > 0 ) {
                   nodes[iNode].getMap().putAll(map);
                }

                // add decendents for next round
                for( Node s : t.getChildren(n) ) {
                    nlevel.add(s);
                }

                ++iNode;
            }
            // setup for next level
            level.clear();
            level.addAll(nlevel);
        }

        // add tree attributes
        final Map<String, Object> map = t.getAttributeMap();
        if( map.size() > 0 ) {
           getMap().putAll(map);
        }
    }

    public List<Node> getChildren(Node node) {
        final int index = ((SimpleRootedNode) node).index;
        final int nSon = nSons(index);
        final ArrayList<Node> clist = new ArrayList<Node>(nSon);
        for(int k = sons[index]; k < sons[index] + nSon; ++k) {
          clist.add(nodes[k]);
        }
        return clist;
    }

    public boolean hasHeights() {
        return hasHeights;
    }

    public double getHeight(Node node) {
        assert hasHeights;

        return heights[((SimpleRootedNode)node).index];
    }

    public boolean hasLengths() {
        return hasLengths;
    }

    public double getLength(Node node) {
        assert hasLengths;

        final int index = ((SimpleRootedNode) node).index;
        if( hasHeights ) {
           if( index == 0 ) return 0;
           return heights[parent[index]] - heights[index];
        }
        return heights[index];
    }

    public Node getParent(Node node) {
        final int index = ((SimpleRootedNode) node).index;
        return index == 0 ? null : nodes[parent[index]];
    }

	public Edge getParentEdge(Node node) {
		throw new UnsupportedOperationException("getParentEdge not implemented in CompactRootedTree");
	}

    public Node getRootNode() {
        return nodes[0];
    }

    public boolean conceptuallyUnrooted() {
        return conceptuallyUnrooted;
    }

    public void setConceptuallyUnrooted(boolean conceptuallyUnrooted) {
        this.conceptuallyUnrooted = conceptuallyUnrooted;
    }

    public boolean isRoot(Node node) {
        return ((SimpleRootedNode)node).index == 0;
    }

    // O(number of nodes)
    public Set<Node> getExternalNodes() {
        Set<Node> n = new LinkedHashSet<Node>();
        for(int i = 0; i < nodes.length; ++i) {
            if( (noSons[i] & MASK) != 0 ) {
                n.add(nodes[i]);
            }
        }
        return n;
    }

    // O(number of nodes)
    public Set<Node> getInternalNodes() {
        Set<Node> n = new LinkedHashSet<Node>();
        for(int i = 0; i < nodes.length; ++i) {
            if( (noSons[i] & MASK) == 0 ) {
                n.add(nodes[i]);
            }
        }
        return n;
    }

    public Set<Edge> getExternalEdges() {
        Set<Edge> edges = new LinkedHashSet<Edge>();
        for (Node node : getExternalNodes()) {
            edges.add( establishEdge( ((SimpleRootedNode)node).index) ) ;
        }
        return edges;
    }

    public Set<Edge> getInternalEdges() {
        Set<Edge> edges = new LinkedHashSet<Edge>();
        for (Node node : getInternalNodes()) {
            if (node != getRootNode()) {
                edges.add( establishEdge( ((SimpleRootedNode)node).index) );
            }
        }
        return edges;
    }

    public Set<Taxon> getTaxa() {
        return new LinkedHashSet<Taxon>(Arrays.asList(taxa));
    }

    public Taxon getTaxon(Node node) {
        final int index = ((SimpleRootedNode) node).index;
        if( (noSons[index] & MASK) != 0 ) {
            return taxa[noSons[index] & MASK - 1];
        }
        return null;
    }

    public boolean isExternal(Node node) {
        return nSons(((SimpleRootedNode)node).index) == 0;
    }

    // O(number of nodes)
    public Node getNode(Taxon taxon) {
        int i = Arrays.asList(taxa).indexOf(taxon);
        for(int k = 0; k < nodes.length; ++k) {
            if( noSons[k] == (MASK | i) ) {
                return nodes[k];
            }
        }
        return null;
    }

    public void renameTaxa(Taxon from, Taxon to) {
        for(int n = 0; n < taxa.length; ++n) {
            if( from.equals(taxa[n]) ) {
                taxa[n] = to;
                break;
            }
        }
    }

    public List<Edge> getEdges(Node node) {
        List<Edge> e = new ArrayList<Edge>();
        final int index = ((SimpleRootedNode) node).index;
        if( index != 0 ) {
            e.add(establishEdge(index));
        }
        for(int n = 0; n < nSons(index); ++n) {
            final int sindex = sons[index] + n;
            e.add(establishEdge(sindex));
        }
        return e;
    }

    public List<Node> getAdjacencies(Node node) {
        List<Node> adjacencies = new ArrayList<Node>();
        final int index = ((SimpleRootedNode) node).index;
        final int nSon = nSons(index);
        final int sonStart = sons[index];
        for(int n = 0; n <  nSon; ++n) {
            adjacencies.add( nodes[sonStart + n] );
        }
        if( index != 0 ) {
            adjacencies.add( nodes[parent[index]] );
        }
        return adjacencies;
    }

    private Edge establishEdge(int index) {
        if( edges == null ) {
            edges = new SimpleRootedEdge[nodes.length];
        }
        if( edges[index] == null ) {
          edges[index] = new SimpleRootedEdge(index);
        }
        return  edges[index];
    }

    public Edge getEdge(Node node1, Node node2) throws NoEdgeException {
        int index1 = ((SimpleRootedNode) node1).index;
        int index2 = ((SimpleRootedNode) node2).index;
        // make index1 the parent of index2
        if( parent[index1] == index2 ) {
            index2 = index1;
        } else if( parent[index2] != index1 ) {
          throw new NoEdgeException();
        }
        // from this point on index1 is invalid

        return establishEdge(index2);
    }

    public double getEdgeLength(Node node1, Node node2) throws NoEdgeException {
        final int index1 = ((SimpleRootedNode) node1).index;
        final int index2 = ((SimpleRootedNode) node2).index;
        if( ! (parent[index1] == index2 || parent[index2] == index1) ) {
            throw new NoEdgeException();
        }
        return Math.abs(heights[index1] - heights[index2]);
    }

    public Node[] getNodes(Edge edge) {
        Node[] ns = new Node[2];
        final int index = ((SimpleRootedEdge) edge).index;
        ns[0] = nodes[index];
        ns[1] = nodes[parent[index]];

        return ns;
    }

    public Set<Node> getNodes() {
        return new LinkedHashSet<Node>(Arrays.asList(nodes));
    }

    public Set<Edge> getEdges() {
        for(int k = 1; k < nodes.length; ++k) {
            establishEdge(k);
        }
        return new LinkedHashSet<Edge>( Arrays.asList(edges));
    }

    public Set<Node> getNodes(int degree) {
        Set<Node> ns = new LinkedHashSet<Node>();
        // check non root nodes
        for(int k = 1; k < nodes.length; ++k) {
            if( degree == nSons(k) + 1 )
              ns.add(nodes[k]);
        }
        // check root
        if( nSons(0) == degree ) {
           ns.add(nodes[0]);
        }
        return ns;
    }

    Map<String, Object> getExistingMap() {
        final int index =  nodes.length;
        if( hasAttributeMap(index) ) {
            return aMap(index);
        }
        return null;
    }

    Map<String, Object> getMap() {
        final int index = nodes.length;
        return aMap(index);
    }
}

/**
 * Helper in attribute handling for tree, nodes and edges.
 *
 * The object provides the map via the abstract methods.
 */
abstract class AttributableImp implements Attributable {
    /**
     * Get attribute map for object.
     * @return the map
     */
    abstract Map<String, Object>  getMap();

    /**
     * Used to avoid creating an attribute object when only querying for map elements.
     *
     * @return Attribute map for object if a none-empty one exists for object, null otherwise
     */
    abstract Map<String, Object> getExistingMap();


    public void setAttribute(String name, Object value) {
        getMap().put(name, value);
    }

    public Object getAttribute(String name) {
        return getMap().get(name);
    }

    public void removeAttribute(String name) {
        getMap().remove(name);
    }

    public Set<String> getAttributeNames() {
        Map<String, Object> map = getExistingMap();
        if( map != null ) {
            return map.keySet();
        }
        return Collections.emptySet();
    }

    public Map<String, Object> getAttributeMap() {
        Map<String, Object> map = getExistingMap();
        if( map != null ) {
            return map;
        }
        return Collections.emptyMap();
    }
}
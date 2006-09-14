package jebl.evolution.trees;

import jebl.evolution.graphs.Node;
import jebl.evolution.graphs.Edge;
import jebl.evolution.taxa.Taxon;
import jebl.util.Attributable;

import java.util.*;

/**
 * A memory efficient rooted tree.
 *
 *   - Uses A compact representation for the tree structure based primarily on indices instead of pointers
 *     and objects
 *   - Minimize penalty for unused features. Trees not using attributes or edges do not require additional
 *     per node/edge memory.
 *
 * Limitations: Maximun of 2^16 nodes and 2^15 external nodes. This should not be a problem with the current
 * sizes of phlogenetic trees we currently handle.
 *
 * @author Joseph Heled
 * @version $Id$
 *
 */
public class CompactRootedTree extends AttributableImp implements RootedTree {
    /**
     * Array of all nodes.
     *
     * Orederd by levels. i.e. for the tree (a, ((e,f), d) ) the layout is
     *
     *   0    1  2 3 4 5 6
     *  root  a  b c d e f
     *
     * where c = (e,f) and b = ((e,f), d)
     *
     * The major advantage is that all decendents of a node are grouped together.
     * Nodes will be reffered to by their index in the array in the comments.
     */
    SimpleRootedNode[] nodes;

    /**
     *  Index of parent node of x is parent[x].
     *  For the example tree, this would be (- 0 0 2 2 3 3)
     */
    short[] parent;

    /**
     *  Decendents of node x start at sons[x]
     *  For the example tree, this would be (1 - 3 5 - - -)
     */
    short[] sons;
    /**
     * Number of Decendents of node x is noSons[x]
     * For the example tree, this would be (2 0 2 2 0 0 0)
     *
     * Actually the above is true only for internal nodes. External nodes contain an index into
     * the taxa array indicating where the taxon for this node is stored. A 1 bit is added at the most
     * significant place to separate internal from external nodes. So in fact the array would look like
     * that (2 0x8000|0 2 2 0x8000|1 0x8000|2 0x8000|3), where taxa[0] holds a taxon etc.
     */
    short[] noSons;

    /**
     * Height of node x is heights[x]
     *
     * Keep only heights since it is easy to compute length given easy access to
     * parent height.
     */
    double[] heights;

    boolean hasHeights;
    boolean hasLengths;

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
    Map<Short, Map<String, Object> > all = null;

    /**
     * Test if attribute map exists for index
     * @param index
     * @return true if map exists
     */
    private boolean hasAttributeMap(short index) {
        return all != null && all.get(index) != null;
    }

    /**
     * Attribute map for index.
     *
     * @param index
     * @return attribute map
     */
    private Map<String, Object> aMap(short index) {
        if( all == null ) {
            all = new HashMap<Short, Map<String, Object>>();
        }

        Map<String, Object> map = all.get(index);
        if( map == null ) {
            map = new HashMap<String, Object>();
            all.put(index, map);
        }
        return map;
    }

    /**
     * A minimal rooted node.
     */
    private class SimpleRootedNode extends AttributableImp implements Node {
        // Index of node in tree nodes array.
        private short index;

        SimpleRootedNode(short index) {
            this.index = index;
        }

        public int getDegree() {
            return nSons(index) + 1;
        }

        Map<String, Object>  getExistingMap() {
            if( hasAttributeMap(index) ) {
                return aMap(index);
            }
            return null;
        }

        Map<String, Object>  getMap() {
            return aMap(index);
        }
    }

    // Number of decendents.
    private int nSons(int index) {
        // Take care of external node bit.
        final short n = noSons[index];
        if( (n & 0x8000) == 0) {
          return n;
        }
        return 0;
    }

    private class SimpleRootedEdge extends AttributableImp implements Edge {
       // edge is between node 'index' and it's parent
       private short index;

        SimpleRootedEdge(short index) {
            this.index = index;
        }

        Map<String, Object>  getExistingMap() {
            final short i = (short)(nodes.length + index);
            if( hasAttributeMap(i) ) {
                return aMap(i);
            }
            return null;
        }

        Map<String, Object>  getMap() {
            return aMap((short)(nodes.length + index));
        }

        public double getLength() {
            return heights[parent[index]] - heights[index];
        }
    }

    public CompactRootedTree(RootedTree t) {
        conceptuallyUnrooted = t.conceptuallyUnrooted();

        final int nNodes = t.getNodes().size();
        nodes = new SimpleRootedNode[nNodes];
        parent = new short[nNodes];
        sons = new short[nNodes];
        noSons = new short[nNodes];
        heights = new double[nNodes];
        hasHeights = t.hasHeights();
        hasLengths = t.hasLengths();
        taxa = new Taxon[t.getTaxa().size()];
        edges = null;

        final Node rootNode = t.getRootNode();
        List<Node> level = new ArrayList<Node>();
        List<Node> nlevel = new ArrayList<Node>();
        level.add(rootNode);
        int k = 0;
        int nextLevelStart = 1;
        int nTax = 0;

        while( level.size() > 0 ) {
            nlevel.clear();
            for( Node n : level ) {
                short ns = (short) (n.getDegree() - 1);

                if( hasHeights ) {
                    heights[k] = t.getHeight(n);
                } else if( hasLengths ) {
                    heights[k] = ((k == 0) ? 0.0 : t.getLength(n));
                }

                nodes[k] = new SimpleRootedNode((short)k);
                sons[k] = ns  > 0 ? (short)nextLevelStart : 0;
                for(int l = 0; l < ns; ++l) {
                    parent[nextLevelStart + l] = (short)k;
                }

                nextLevelStart += ns;

                if( ns == 0 ) {
                    assert t.isExternal(n);
                    taxa[nTax] = t.getTaxon(n);
                    ns = (short)(0x8000 | nTax);
                    ++nTax;
                }
                noSons[k] = ns;

                for( Node s : t.getChildren(n) ) {
                    nlevel.add(s);
                }

                ++k;
            }
            level.clear();
            level.addAll(nlevel);
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

    public Node getRootNode() {
        return nodes[0];
    }

    public boolean conceptuallyUnrooted() {
        return conceptuallyUnrooted;
    }

    public boolean isRoot(Node node) {
        return ((SimpleRootedNode)node).index == 0;
    }

    // O(number of nodes)
    public Set<Node> getExternalNodes() {
        Set<Node> n = new HashSet<Node>();
        for(int i = 0; i < nodes.length; ++i) {
            if( (noSons[i] & 0x8000) != 0 ) {
                n.add(nodes[i]);
            }
        }
        return n;
    }

    // O(number of nodes)
    public Set<Node> getInternalNodes() {
        Set<Node> n = new HashSet<Node>();
        for(int i = 0; i < nodes.length; ++i) {
            if( (noSons[i] & 0x8000) == 0 ) {
                n.add(nodes[i]);
            }
        }
        return n;
    }

    public Set<Edge> getExternalEdges() {
        Set<Edge> edges = new HashSet<Edge>();
        for (Node node : getExternalNodes()) {
            edges.add( establishEdge( ((SimpleRootedNode)node).index) ) ;
        }
        return edges;
    }

    public Set<Edge> getInternalEdges() {
        Set<Edge> edges = new HashSet<Edge>();
        for (Node node : getInternalNodes()) {
            if (node != getRootNode()) {
                edges.add( establishEdge( ((SimpleRootedNode)node).index) );
            }
        }
        return edges;
    }

    public Set<Taxon> getTaxa() {
        return new HashSet<Taxon>(Arrays.asList(taxa));
    }

    public Taxon getTaxon(Node node) {
        final short index = ((SimpleRootedNode) node).index;
        if( (noSons[index] & 0x8000) != 0 ) {
            return taxa[noSons[index] & 0x7FFF];
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
            if( noSons[k] == (0x8000 | i) ) {
                return nodes[k];
            }
        }
        return null;
    }

    public List<Edge> getEdges(Node node) {
        List<Edge> e = new ArrayList<Edge>();
        final short index = ((SimpleRootedNode) node).index;
        if( index != 0 ) {
            e.add(establishEdge(index));
        }
        for(int n = 0; n < nSons(index); ++n) {
            final short sindex = (short) (sons[index] + n);
            e.add(establishEdge(sindex));
        }
        return e;
    }

    public List<Node> getAdjacencies(Node node) {
        List<Node> adjacencies = new ArrayList<Node>();
        final short index = ((SimpleRootedNode) node).index;
        final int nSon = nSons(index);
        final short sonStart = sons[index];
        for(int n = 0; n <  nSon; ++n) {
            adjacencies.add( nodes[sonStart + n] );
        }
        if( index != 0 ) {
            adjacencies.add( nodes[parent[index]] );
        }
        return adjacencies;
    }

    private Edge establishEdge(short index) {
        if( edges == null ) {
            edges = new SimpleRootedEdge[nodes.length];
        }
        if( edges[index] == null ) {
          edges[index] = new SimpleRootedEdge(index);
        }
        return  edges[index];
    }

    public Edge getEdge(Node node1, Node node2) throws NoEdgeException {
        short index1 = ((SimpleRootedNode) node1).index;
        short index2 = ((SimpleRootedNode) node2).index;
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
        final short index1 = ((SimpleRootedNode) node1).index;
        final short index2 = ((SimpleRootedNode) node2).index;
        if( ! (parent[index1] == index2 || parent[index2] == index1) ) {
            throw new NoEdgeException();
        }
        return Math.abs(heights[index1] - heights[index2]);
    }

    public Node[] getNodes(Edge edge) {
        Node[] ns = new Node[2];
        final short index = ((SimpleRootedEdge) edge).index;
        ns[0] = nodes[index];
        ns[1] = nodes[parent[index]];

        return ns;
    }

    public Set<Node> getNodes() {
        return new HashSet<Node>(Arrays.asList(nodes));
    }

    public Set<Edge> getEdges() {
        for(int k = 1; k < nodes.length; ++k) {
            establishEdge((short)k);
        }
        return new HashSet<Edge>( Arrays.asList(edges));
    }

    public Set<Node> getNodes(int degree) {
        Set<Node> ns = new HashSet<Node>();
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
        final short index = (short) nodes.length;
        if( hasAttributeMap(index) ) {
            return aMap(index);
        }
        return null;
    }

    Map<String, Object>  getMap() {
        final short index = (short) nodes.length;
        return aMap(index);
    }
}


abstract class AttributableImp implements Attributable {
    abstract Map<String, Object>  getExistingMap();
    abstract Map<String, Object>  getMap();

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


    /*
    public void setAttribute(String name, Object value) {
        aMap((short)-1).put(name, value);
    }

    public Object getAttribute(String name) {
        return aMap((short)-1).get(name);
    }

    public void removeAttribute(String name) {
       aMap((short)-1).remove(name);
    }

    public Set<String> getAttributeNames() {
        if( hasAttributeMap((short)-1) ) {
            return aMap((short)-1).keySet();
        }
        return Collections.emptySet();
    }

    public Map<String, Object> getAttributeMap() {
        if( hasAttributeMap((short)-1) ) {
            return aMap((short)-1);
        }
        return Collections.emptyMap();
    }
    */


/*
    private abstract class AttributableImp implements Attributable {
         abstract short getIndex();

         public void setAttribute(String name, Object value) {
            aMap(getIndex()).put(name, value);
        }

        public Object getAttribute(String name) {
            return aMap(getIndex()).get(name);
        }

        public void removeAttribute(String name) {
            aMap(getIndex()).remove(name);
        }

        public Set<String> getAttributeNames() {
            final short index = getIndex();
            if( hasAttributeMap(index) ) {
              return aMap(index).keySet();
            }
            return Collections.emptySet();
        }

        public Map<String, Object> getAttributeMap() {
            final short index = getIndex();
            if( hasAttributeMap(index) ) {
               return aMap(index);
            }
            return Collections.emptyMap();
        }
    }
      */
/*
    public Node createExternalNode(Taxon taxon) {
        nodes.
        return null;
    }

    public void setConceptuallyUnrooted(boolean unrooted) {
    }

    public void setLength(Node branch, double length) {
        //To change body of created methods use File | Settings | File Templates.
    }

    public Node createInternalNode(List<Node> children) {
        return null;  //To change body of created methods use File | Settings | File Templates.
    }
    */

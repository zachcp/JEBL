package jebl.evolution.trees;

import jebl.evolution.taxa.Taxon;
import jebl.evolution.graphs.Node;
import jebl.evolution.graphs.Graph;
import jebl.util.FixedBitSet;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: joseph
 * Date: 5/02/2006
 * Time: 15:12:48
 *
 * @author Joseph Heled
 * @version $Id$
 *
 * Builds consensus tree given a set of unrooted trees.
 *
 * The implementation is relativly simple. Each tree is scanned, and support for each clade is collected in one table.
 * The clade is represented by a bitset, which always contains the (arbitrary) first node. The scan is made by going
 * over all internal branches ordered in such a way that the subtree of exactly one edge node has been completly scanned,
 * so the node "knows" the set of tips of that subtree.
 *
 * After collection an initial tree is constructed with one root and all tips as children. The support set is scanned
 * in order of decreasing support, and each supported clade refines the tree by splitting the node currently containing
 * the clade into two nodes. This process continues until only clades with support lower that the threashold are left.
 * This threshold is traditionally 50%, but is given as an user option. Lower values will show less supported clades
 * but a better resolved tree.
 *
 * The length of the consensus tree branches are computed from the average over all trees containing the clade. The
 * lenghts of tip branches are computed from the average over all trees.
 */

public class UnRootedConsensusTreeBuilder extends ConsensusTreeBuilder {
    /** Set of trees */
    Tree[] trees;

    /** Outgroup, if any. Currently used only for display purposes. */
    Taxon outGroup;

    /** Consensus contains only clades having at least that amount of support in set. Traditionally 50% */
    double supportThreshold;

    UnRootedConsensusTreeBuilder(Tree[] trees, Taxon outGroup, double supportThreshold) {
        super(trees);
        this.trees = trees;
        this.outGroup = outGroup;
        this.supportThreshold = supportThreshold;
    }

    /**
     * One clade support.
     */
    class Support {
        /** number of trees containing this clade */
        private int nTreesWithClade;
        /** Sum of branch length separating clade from the rest of taxa (in trees containing the clade) */
        private double sumBranches;

        Support() {
            sumBranches = 0.0;
            nTreesWithClade = 0;
        }

        public void add(double branch) {
            sumBranches += branch;
            ++nTreesWithClade;
        }
    }

    private boolean debug = false;
    // debug
    private String subTreeRep(Tree t, Node n, Node root) {

        if( t.isExternal(n) ) {
            return t.getTaxon(n).getName();
        }
        StringBuffer b = new StringBuffer();
        for( Node x : t.getAdjacencies(n) ) {
            if( x == root ) continue;
            if( b.length() > 0 ) b.append(",");
            b.append( subTreeRep(t,x,n) );
        }
        return '(' + b.toString() + ')';
    }


    // loop on all trees t
    //   establish support :
    //    for e : external nodes of t - add adjacencies of e to scan set and add e to done set
    //
    //    while n in scan set:
    //       if all adj of n done - remove n from scan. add n to done. continue
    //       if all adj of n sans one are done:
    //          get set of subtree tips from done nodes
    //          add new split with branch length to support set
    //          add not done adj to scan list
    //          remove n from scan and add it to done list

    public Tree build() {

        try {

            Map<FixedBitSet, Support> support = new  HashMap<FixedBitSet, Support>();
            double[] sumBranchesOfExternal = new double[taxons.size()];

            for( Tree tree : trees ) {
                int initialCapacity = tree.getNodes().size();
                Set<Node> scanSet = new LinkedHashSet<Node>(initialCapacity);
                Map<Node,FixedBitSet> doneSet = new HashMap<Node,FixedBitSet>(initialCapacity);

                for( Node n : tree.getExternalNodes() ) {
                    FixedBitSet b = new FixedBitSet(nExternalNodes);
                    final int position = taxons.indexOf(tree.getTaxon(n));
                    b.set( position );

                    if( debug) System.out.print( taxons.indexOf(tree.getTaxon(n)) + ":" + tree.getTaxon(n).getName() + " ");
                    doneSet.put(n, b);
                    for( Node a : tree.getAdjacencies(n) ) {
                        scanSet.add(a);
                    }
                    sumBranchesOfExternal[position] += tree.getEdgeLength(n, tree.getAdjacencies(n).get(0));
                }

                int nInternalEdges = nExternalNodes - 3;

                List<Node> intr = new ArrayList<Node>(tree.getInternalNodes());

                if( debug ) System.out.println("\ntree " + Utils.toNewick(Utils.rootTheTree(tree)));

                while( scanSet.size() > 0 ) {
                    Set<Node> nextScanSet = new LinkedHashSet<Node>(initialCapacity);

                    for( Node n : scanSet ) {
                        if( debug) System.out.println("scan " + intr.indexOf(n));
                        int nDone = 0;
                        List<Node> adjacencies = tree.getAdjacencies(n);
                        for( Node a : adjacencies ) {
                            if( doneSet.containsKey(a) ) ++nDone;
                        }

                        if( nDone + 1 < adjacencies.size() ) {
                            if( debug) System.out.println("add to next " + intr.indexOf(n));
                            nextScanSet.add(n);
                            continue;
                        }

                        if( nDone < adjacencies.size() ) {

                            FixedBitSet b = new FixedBitSet(nExternalNodes);
                            Node notDone = null;
                            for( Node a : adjacencies ) {
                                if( doneSet.containsKey(a) ) {
                                    FixedBitSet subSet = doneSet.get(a);
                                    if( subSet == null ) {
                                        if( debug)  System.out.println(a +  " " + subTreeRep(tree, n, notDone));
                                        assert( false) ;
                                    }
                                    b.union(subSet);
                                } else {
                                    notDone = a;
                                }
                            }
                            final double branch;

                            branch = tree.getEdgeLength(n, notDone);

                            doneSet.put(n, new FixedBitSet(b));
                            // in case it has been added by a previous node
                            nextScanSet.remove(n);
                            // support keys always contains the (arbitray) tip 0
                            if( ! b.contains(0) ) {
                                b.complement();
                            }
                            Support s = support.get(b);
                            if( s == null ) {
                                s = new Support();
                                support.put(b, s);
                            }
                            if( debug)  System.out.println("add " + b + "<" + subTreeRep(tree,n, notDone) + ">" + " " + s.nTreesWithClade + "/" + s.sumBranches + " " + branch);
                            s.add(branch);
                            --nInternalEdges;

                            if( debug)  System.out.println("add to next " + intr.indexOf(notDone) );
                            nextScanSet.add(notDone);
                        } else {
                            if( debug ) {
                                for( Node x : tree.getAdjacencies(n) ) {
                                    System.out.println(subTreeRep(tree,x, n) + " is done " + intr.indexOf(n));
                                }
                            }
                            doneSet.put(n, null);
                        }
                    }

                    scanSet = nextScanSet;
                }
                if( debug)  System.out.println(nInternalEdges);
            }

            // sorts support from largest to smallest
            final Comparator<Map.Entry<FixedBitSet, Support>> comparator = new Comparator<Map.Entry<FixedBitSet, Support>>() {
                public int compare(Map.Entry<FixedBitSet, Support> o1, Map.Entry<FixedBitSet, Support> o2) {
                    return o2.getValue().nTreesWithClade - o1.getValue().nTreesWithClade;
                }
            };

            PriorityQueue< Map.Entry<FixedBitSet,Support> > queue =
                    new PriorityQueue< Map.Entry<FixedBitSet,Support> >(support.size(), comparator);

            for( Map.Entry<FixedBitSet,Support> s : support.entrySet() ) {
                queue.add(s);
            }

            MutableRootedTree consTree = new MutableRootedTree();

            Node[] subs = new Node[nExternalNodes];
            FixedBitSet[] tips = new FixedBitSet[nExternalNodes];              assert taxons.size() == nExternalNodes;
            tips[0] = new FixedBitSet(nExternalNodes);
            for(int nt = 0; nt < taxons.size(); ++nt) {
                subs[nt] = consTree.createExternalNode( taxons.get(nt) );
                tips[0].set(nt);
            }

            subs[0] = consTree.createInternalNode(Arrays.asList(subs));
            Arrays.fill(subs,1,subs.length, null);

            int nSubsTrees = 1;

            while( queue.peek() != null ) {
                Map.Entry<FixedBitSet,Support> e = queue.poll();
                Support s = e.getValue();

                if( (1.0*s.nTreesWithClade) / trees.length < supportThreshold ) {
                    break;
                }

                final FixedBitSet splitTips = e.getKey();
                int nLoop = nSubsTrees;
                for(int nsub = 0; nsub < nLoop; ++nsub) {
                    int nSplit = tips[nsub].intersectCardinality(splitTips);
                    // with th < 50%, some supports splits 2 child nodes - skip
                    if( nSplit > 0 && nSplit < tips[nsub].cardinality() && tips[nsub].cardinality() > 2) {
                        int[] split = new int[nSplit];
                        int ns = 0;

                        int l = 0;
                        for(int k = 0; k < nExternalNodes; ++k) {
                            if( tips[nsub].contains(k) ) {
                                if( splitTips.contains(k) ) {
                                    split[ns] = l;
                                    ++ns;
                                }
                                ++l;
                            }
                        }

                        Node n = subs[nsub];
                        consTree.refineNode(n, split);

                        final List<Node> children = consTree.getChildren(n);
                        boolean anyTips = consTree.isExternal(children.get(0)) || consTree.isExternal(children.get(1));
                        final double length = (anyTips ? 1.0 : 0.5) * s.sumBranches / s.nTreesWithClade;
                        final double psupport = (100.0 * s.nTreesWithClade) / trees.length;
                        for( Node child : children ) {
                            if( ! consTree.isExternal(child) ) {
                                consTree.setLength(child, length);

                                child.setAttribute(supportAttributeName, psupport);
                            }
                        }

                        subs[nsub] = children.get(0);
                        subs[nSubsTrees] = children.get(1);
                        tips[nSubsTrees] = new FixedBitSet(tips[nsub]);
                        tips[nSubsTrees].setMinus(splitTips);
                        tips[nsub].intersect(splitTips);

                        ++nSubsTrees;
                    }
                }
            }

            for(int nt = 0; nt < taxons.size(); ++nt) {
                final Node n = consTree.getNode( taxons.get(nt) );
                consTree.setLength(n, sumBranchesOfExternal[nt]/ trees.length);
            }

            if( outGroup != null ) {
                Node out = consTree.getNode(outGroup);
                Set<String> a = new HashSet<String>();
                a.add(supportAttributeName);
                consTree.reRootWithOutgroup(out, a);
            }

            return consTree;
        } catch (Graph.NoEdgeException e) {
            // bug
        }
        return null;
    }
}

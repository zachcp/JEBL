package jebl.evolution.trees;

import jebl.evolution.graphs.Node;
import jebl.util.FixedBitSet;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: joseph
 * Date: 5/03/2006
 * Time: 09:40:18
 *
 * @author Joseph Heled
 * @version $Id$
 *
 * Implementation shares some code with GreedyUnrootedConsensusTreeBuilder (which preceded it), and perhaps I will
 * find a way to merge the two at a later stage when I have the time.
 */
public class GreedyRootedConsensusTreeBuilder extends ConsensusTreeBuilder {
    /** Set of trees. */
    private final RootedTree[] rtrees;


    /** Consensus contains only clades having at least that amount of support in set. Traditionally 50% */
    private final double supportThreshold;

    GreedyRootedConsensusTreeBuilder(RootedTree[] trees, double supportThreshold) {
        super(trees);
        this.rtrees = trees;
        this.supportThreshold = supportThreshold;
    }

    /**
     * One clade support.
     */
    static final class Support {
        /** number of trees containing the clade. */
        private int nTreesWithClade;
        /** Sum of node heights of trees containing the clade. */
        private double sumBranches;

        Support() {
            sumBranches = 0.0;
            nTreesWithClade = 0;
        }

        public final void add(double height) {
            sumBranches += height;
            ++nTreesWithClade;
        }
    }

    private final boolean debug = false;
    // debug
    private String subTreeRep(RootedTree t, Node n) {

        if( t.isExternal(n) ) {
            return t.getTaxon(n).getName();
        }
        StringBuffer b = new StringBuffer();
        for( Node x : t.getChildren(n) ) {
            if( b.length() > 0 ) b.append(",");
            b.append( subTreeRep(t,x) );
        }
        return '(' + b.toString() + ')';
    }

    private String tipsAsText(FixedBitSet b) {
        String names = "(";
        for(int i = b.nextOnBit(0); i >= 0; i = b.nextOnBit(i+1)) {
            names = names + taxons.get(i).getName() + ",";
        }
        return names + ")";
    }

    private FixedBitSet rootedSupport(RootedTree tree, Node node, Map<FixedBitSet, Support> support) {
        FixedBitSet clade = new FixedBitSet(nExternalNodes);
        if( tree.isExternal(node) ) {
            clade.set(taxons.indexOf(tree.getTaxon(node)));
        } else {
            for( Node n : tree.getChildren(node) ) {
                FixedBitSet childClade = rootedSupport(tree, n, support);
                clade.union(childClade);
            }
        }

        Support s = support.get(clade);
        if( s == null ) {
            s = new Support();
            support.put(clade, s);
        }
        s.add(tree.getHeight(node));
        return clade;
    }

    /**
     * Make sure subtree belowe node has consistent heights, i.e. node height is higher than it's descendants
     * @param tree
     * @param node
     * @return height of node
     */
    private double insureConsistency(SimpleRootedTree tree, Node node) {
        double hieght = tree.getHeight(node);
        if( tree.isExternal(node) ) {
            return hieght;
        } else {
            for( Node n : tree.getChildren(node) ) {
               final double childHeight = insureConsistency(tree, n);
               hieght = Math.max(hieght, childHeight);
            }
        }

        tree.setHeight(node, hieght);
        return hieght;
    }


    public final Tree build() {

        // establish support
        Map<FixedBitSet, Support> support = new  HashMap<FixedBitSet, Support>();
        for( RootedTree tree : rtrees ) {
            if( debug ) {
                System.out.println("Tree: " + subTreeRep(tree, tree.getRootNode()));
            }
            rootedSupport(tree, tree.getRootNode(), support);
        }

        final int nTrees = rtrees.length;

        MutableRootedTree consTree = new MutableRootedTree();

        // Contains all internal nodes in the tree so far, ordered so descendants
        // appear later than ancestors
        List<Node> internalNodes = new ArrayList<Node>(nExternalNodes);

        // For each internal node, a bit-set with the complete set of tips for it's clade
        List<FixedBitSet> internalNodesTips = new ArrayList<FixedBitSet>(nExternalNodes);              assert taxons.size() == nExternalNodes;

        // establish a tree with one root having all tips as descendants
        internalNodesTips.add(new FixedBitSet(nExternalNodes));
        FixedBitSet rooNode = internalNodesTips.get(0);
        Node[] nodes = new Node[nExternalNodes];
        for(int nt = 0; nt < taxons.size(); ++nt) {
            nodes[nt] = consTree.createExternalNode( taxons.get(nt) );
            rooNode.set(nt);
        }

        internalNodes.add(consTree.createInternalNode(Arrays.asList(nodes)));

        // sorts support from largest to smallest
        final Comparator<Map.Entry<FixedBitSet, Support>> comparator = new Comparator<Map.Entry<FixedBitSet, Support>>() {
            public int compare(Map.Entry<FixedBitSet, Support> o1, Map.Entry<FixedBitSet, Support> o2) {
                return o2.getValue().nTreesWithClade - o1.getValue().nTreesWithClade;
            }
        };

        // add everything to queue
        PriorityQueue< Map.Entry<FixedBitSet,Support> > queue =
                new PriorityQueue< Map.Entry<FixedBitSet,Support> >(support.size(), comparator);

        for( Map.Entry<FixedBitSet,Support> se : support.entrySet() ) {
            Support s = se.getValue();
            FixedBitSet clade = se.getKey();
            final int cladeSize = clade.cardinality();
            if( cladeSize == nExternalNodes ) {
                // root
                consTree.setHeight(consTree.getRootNode(), s.sumBranches / nTrees);
                continue;
            }

            if( s.nTreesWithClade == nTrees && cladeSize == 1 ) {
                // leaf/external node
                final int nt = clade.nextOnBit(0);
                final Node leaf = consTree.getNode( taxons.get(nt) );
                consTree.setHeight(leaf, s.sumBranches / nTrees);
            } else {
                queue.add(se);
            }
        }

        while( queue.peek() != null ) {
            Map.Entry<FixedBitSet,Support> e = queue.poll();
            final Support s = e.getValue();

            final double psupport = (1.0 * s.nTreesWithClade) / nTrees;
            if( psupport < supportThreshold ) {
                break;
            }

            final FixedBitSet cladeTips = e.getKey();

            if( debug ) {
                System.out.println(100.0*psupport + " Split: " + cladeTips + " " + tipsAsText(cladeTips));
            }

            boolean found = false;

            // locate the node containing the clade. going in reverse order insures the lowest one is hit first
            for(int nsub = internalNodesTips.size()-1; nsub >= 0; --nsub) {

                FixedBitSet allNodeTips = internalNodesTips.get(nsub);

                // size of intersection between tips & split
                final int nSplit = allNodeTips.intersectCardinality(cladeTips);

                if( nSplit == cladeTips.cardinality() ) {
                    // node contains all of clade

                    // Locate node descendants containing the split
                    found = true;
                    List<Integer> split = new ArrayList<Integer>();

                    Node n = internalNodes.get(nsub);
                    int l = 0;
                    List<Node> children = consTree.getChildren(n);
                    for( Node ch : children ) {
                        if( consTree.isExternal(ch) ) {
                            if( cladeTips.contains(taxons.indexOf(consTree.getTaxon(ch))) ) {
                                split.add(l);
                            }
                        } else {
                            // internal
                            final int o = internalNodes.indexOf(ch);
                            final int i = internalNodesTips.get(o).intersectCardinality(cladeTips);
                            if( i == internalNodesTips.get(o).cardinality() ) {
                                split.add(l);
                            } else if( i > 0 ) {
                                // Non compatible
                                found = false;
                                break;
                            }
                        }
                        ++l;
                    }


                    if( ! ( found && split.size() < children.size() )  ) {
                        found = false;
                        break;
                    }

                    if( split.size() == 0 ) {
                        System.out.println("Bug??");
                        assert(false);
                    }

                    final Node detached = consTree.detachChildren(n, split);
                    final double height = s.sumBranches / s.nTreesWithClade;
                    consTree.setHeight(detached, height);

                    detached.setAttribute(supportAttributeName, 100*psupport);

                    if( debug ) {
                        System.out.println("detached:" + subTreeRep(consTree,detached) + " len " + height + " sup " + psupport);
                        System.out.println("tree: " + Utils.toNewick(consTree));
                    }

                    // insert just after parent, so before any descendants
                    internalNodes.add(nsub+1, detached);
                    internalNodesTips.add(nsub+1, new FixedBitSet(cladeTips));

                    break;
                }
            }

            if( psupport >= .5 && ! found ) {
                System.out.println("Bug??");
                assert(false);
            }
        }

        insureConsistency(consTree, consTree.getRootNode());
        return consTree;
    }
}
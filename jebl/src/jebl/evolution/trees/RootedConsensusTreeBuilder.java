package jebl.evolution.trees;

import jebl.evolution.graphs.Node;
import jebl.util.FixedBitSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: joseph
 * Date: 5/02/2006
 * Time: 15:00:10
 *
 * @author Joseph Heled
 * @version $Id$
 *
 * Construct a consensus tree for a set of rooted trees. The construction is done via clustering. For any two
 * clusters/clades, their distance is the height of their most recent common ancesstor (computed as an average over all
 * trees). This seem natural as it connectes clades in reverse time order.
 */

public class RootedConsensusTreeBuilder extends ConsensusTreeBuilder {
    RootedTree[] trees;
    private double supportThreshold;
    private List<FixedBitSet> subTreeTips;

    private TreeInfo[] info;

    private boolean debug = false;

    RootedConsensusTreeBuilder(Tree[] trees, double supportThreshold) {
        super(trees);
        this.trees = new RootedTree[trees.length];
        for(int i = 0; i < trees.length; ++i) {
            this.trees[i] = (RootedTree)trees[i];
        }
        this.supportThreshold = supportThreshold;

        info = new TreeInfo[trees.length];
        for(int iTree = 0; iTree < trees.length; ++iTree) {
            info[iTree] = new TreeInfo(this.trees[iTree]);
        }
    }

    public Tree build() {
        return earliestCommonAnncesstorClustering(supportThreshold);
    }


    class TreeInfo {
        // for each tree, establish a postorder order, and in each internal node the subsets of decendentants
        FixedBitSet[] nodesTipSet;

        int[] postorder;
        int[][] nodeChildren;

        private Node[] allNodes;
        TreeInfo(RootedTree tree) {

            allNodes = new Node[tree.getNodes().size()];

            for( Node node : tree.getExternalNodes() ) {
                int i = taxons.indexOf( tree.getTaxon(node) );
                allNodes[i] = node;
            }

            int k = nExternalNodes;
            for( Node node : tree.getInternalNodes() ) {
                allNodes[k] = node;
                ++k;
            }

            postorder = new int[allNodes.length - nExternalNodes];
            nodesTipSet = new FixedBitSet[allNodes.length];
            nodeChildren = new int [allNodes.length][];

            inPostorder(tree, tree.getRootNode(), 0);
        }

        private int inPostorder(RootedTree tree, Node node, int nPost) {
            List<Node> all = Arrays.asList(allNodes);
            FixedBitSet b = new FixedBitSet(allNodes.length);
            final int c = all.indexOf(node);
            if( tree.isExternal(node) ) {
                b.set(c);
            } else {

                List<Node> children = tree.getChildren(node);
                nodeChildren[c] = new int[children.size()];
                int nc = 0;
                for( Node child : children ) {
                    nPost = inPostorder(tree, child, nPost);
                    int c1 = all.indexOf(child);
                    nodeChildren[c][nc] = c1;
                    ++nc;
                    b.union(nodesTipSet[c1]);
                }
                postorder[nPost] = c;
                ++nPost;
            }
            nodesTipSet[c] = b;
            return nPost;
        }
    }

    double[][] height;

    void setupPairs() {
        height = new double [nExternalNodes][nExternalNodes];

        if( debug )  {
            for(int k = 0; k < taxons.size(); ++k) {
                System.out.print(taxons.get(k).getName() + ":" + k + ", ");
            }
            System.out.println();
        }

        for(int iTree = 0; iTree < trees.length; ++iTree) {
            if( debug ) System.out.println("tree " + Utils.toNewick(trees[iTree]));

            TreeInfo info = this.info[iTree];
            for (final int nodeIndex : info.postorder) {
                final int leftChildIndex = info.nodeChildren[nodeIndex][0];
                final int rightChildIndex = info.nodeChildren[nodeIndex][1];
                final FixedBitSet left = info.nodesTipSet[leftChildIndex];
                final FixedBitSet right = info.nodesTipSet[rightChildIndex];

                for (int lTip = left.nextSetBit(0); lTip >= 0; lTip = left.nextSetBit(lTip + 1)) {
                    for (int rTip = right.nextSetBit(0); rTip >= 0; rTip = right.nextSetBit(rTip + 1)) {

                        final double h = (trees[iTree]).getHeight(info.allNodes[nodeIndex]);
                        height[Math.min(lTip, rTip)][Math.max(lTip, rTip)] += h;
                    }
                }
            }
        }

        for(int d = 0; d < nExternalNodes; ++d) {
            for(int d1 = d+1; d1 < nExternalNodes; ++d1) {
                height[d][d1] /= trees.length;
            }
        }
    }

    private double[] updateHeights(int i, int j) {
        final int nUpdates = subTreeTips.size();
        double[] distances = new double[nUpdates+1];

        FixedBitSet joined = new FixedBitSet(subTreeTips.get(i));
        joined.union(subTreeTips.get(j));
        double totWeight = 0.0;
        boolean firstUpdate = true;

        if( nUpdates == 2 ) {
            for (Tree t : trees) {
                RootedTree tree = (RootedTree) t;
                distances[1] += tree.getHeight(tree.getRootNode());
            }
            distances[2] = 1;
        }   else {
            for(int l = 0; l < nUpdates; ++l) {
                if( (l == i || l == j) ) {
                    continue;
                }

                FixedBitSet joinedk = new FixedBitSet(joined);
                joinedk.union(subTreeTips.get(l));

                for(int iTree = 0; iTree < trees.length; ++iTree) {
                    RootedTree tree = trees[iTree];
                    TreeInfo info = this.info[iTree];
                    boolean commonAnncestor = !firstUpdate;
                    for (int nodeIndex : info.postorder) {
                        FixedBitSet nodeBS = info.nodesTipSet[nodeIndex];

                        if (!commonAnncestor && joined.containedIn(nodeBS)) {

                            final int tipsInSubtree = nodeBS.cardinality();
                            final int tipsInClusters = joined.cardinality();
                            final double w = (tipsInSubtree == tipsInClusters) ? 1 : 0;

                            totWeight += w;
                            commonAnncestor = true;
                        }

                        if (joinedk.containedIn(nodeBS)) {
                            final double h = tree.getHeight(info.allNodes[nodeIndex]);
                            distances[l] += h;
                            break;
                        }
                    }

                }
                firstUpdate = false;
            }
        }

        for(int l = 0; l < nUpdates; ++l) {
            distances[l] /= trees.length;
        }

        distances[nUpdates] = totWeight / trees.length;
        return distances;
    }

    private Tree earliestCommonAnncesstorClustering(double supportThreshold) {

        MutableRootedTree consensus = new MutableRootedTree();
        List<Node> subTrees = new ArrayList<Node>(nExternalNodes);

        double[] tipHeights = new double[taxons.size()];
        for (Tree tree1 : trees) {
            RootedTree tree = (RootedTree) tree1;
            for (Node e : tree.getExternalNodes()) {
                final int i = taxons.indexOf(tree.getTaxon(e));
                tipHeights[i] += tree.getHeight(e);
            }
        }

        subTreeTips = new ArrayList<FixedBitSet>(nExternalNodes);
        for(int k = 0; k < nExternalNodes; ++k) {
            FixedBitSet b = new FixedBitSet(nExternalNodes);
            b.set(k);
            subTreeTips.add(b);
            final Node externalNode = consensus.createExternalNode(taxons.get(k));
            consensus.setHeight(externalNode, tipHeights[k]/trees.length);
            subTrees.add(externalNode);
        }

        setupPairs();

        for(int nClusters = nExternalNodes; nClusters > 1; --nClusters) {
            double mostRecentAnncestorHeight = Double.MAX_VALUE;
            int besti = 0, bestj = 0;
            for(int d = 0; d < nClusters; ++d) {
                for(int d1 = d+1; d1 < nClusters; ++d1) {
                    if( height[d][d1] < mostRecentAnncestorHeight ) {
                        mostRecentAnncestorHeight = height[d][d1];
                        besti = d; bestj = d1;
                    }
                }
            }
            double[] newHeights = updateHeights(besti, bestj);
            double supportForClade = newHeights[newHeights.length-1];

            final Node[] children = {subTrees.get(besti), subTrees.get(bestj)};
            final double maxChild = Math.max(consensus.getHeight(children[0]), consensus.getHeight(children[1]));
            mostRecentAnncestorHeight = Math.max(mostRecentAnncestorHeight, maxChild);

            final SimpleRootedNode sub = consensus.createInternalNode(Arrays.asList(children));

            consensus.setHeight(sub, mostRecentAnncestorHeight);
            // root always 100%
            if( nClusters > 2 ) {
                sub.setAttribute(supportAttributeName, 100.0*supportForClade);
            }
            subTrees.set(besti, sub);
            subTreeTips.get(besti).union(subTreeTips.get(bestj));
            subTreeTips.remove(bestj);
            subTrees.remove(bestj);

            // compress distance matrix
            for(int i = 0; i < nClusters; ++i) {
                if( besti < i ) {
                    height[besti][i] = newHeights[i];
                } else {
                    height[i][besti] = newHeights[i];
                }
            }

            for(int i = 0; i < nClusters-1; ++i) {
                for(int j = bestj; j < nClusters-1; ++j) {
                    height[i][j] = height[i + (i >=bestj ? 1 : 0)][j+1];
                }
            }
        }

        supportThreshold *= 100;
        for( Node node : consensus.getInternalNodes() ) {
            Object sup = node.getAttribute(supportAttributeName);
            if( sup != null && (Double)sup < supportThreshold ) {
                consensus.removeInternalNode(node);
            }
        }
        return consensus;
    }
}

package jebl.evolution.trees;

import jebl.evolution.graphs.Node;
import jebl.evolution.distances.DistanceMatrix;
import jebl.evolution.distances.BasicDistanceMatrix;
import jebl.evolution.taxa.Taxon;
import jebl.util.FixedBitSet;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: joseph
 * Date: 15/01/2006
 * Time: 17:26:01
 *
 * @author Joseph Heled
 * @version $Id$
 *
 * Work in progress.
 *
 *  Currently only rooted binary trees are supported.
 */
public class ConsensusTreeBuilder {
    ClusteringTreeBuilder builder;

    private List<Taxon> taxons;
    private TreeInfo[] info;
    private RootedTree[] trees;
    final boolean isRooted;
    private final int nExternalNodes;
    private List<FixedBitSet> subTreeTips;

    class TreeInfo {
        // for each tree, establish a postorder order, and in each internal node the subsets of decendentants
        FixedBitSet[] nodesTipSet;

        int[] postorder;
        int[][] nodeChildren;

        private List<Node> allNodes;
        TreeInfo(RootedTree tree) {

            allNodes = new ArrayList<Node>();
            Node[] externalNodesInOrder = new Node[nExternalNodes];
            for( Node node : tree.getExternalNodes() ) {
                int i = taxons.indexOf( tree.getTaxon(node) );
                externalNodesInOrder[i] = node;
            }
            allNodes.addAll(Arrays.asList(externalNodesInOrder));
            allNodes.addAll(tree.getInternalNodes());

            postorder = new int[allNodes.size() - nExternalNodes];
            nodesTipSet = new FixedBitSet[allNodes.size()];
            nodeChildren = new int [allNodes.size()][];

            inPostorder(tree, tree.getRootNode(), 0);
        }

        private int inPostorder(RootedTree tree, Node node, int nPost) {
            FixedBitSet b = new FixedBitSet(allNodes.size());
            final int c = allNodes.indexOf(node);
            if( tree.isExternal(node) ) {
                b.set(c);
            } else {

                List<Node> children = tree.getChildren(node);
                nodeChildren[c] = new int[children.size()];
                int nc = 0;
                for( Node child : children ) {
                    nPost = inPostorder(tree, child, nPost);
                    int c1 = allNodes.indexOf(child);
                    nodeChildren[c][nc] = c1;
                    ++nc;
                    b.or(nodesTipSet[c1]);
                }
                postorder[nPost] = c;
                ++nPost;
            }
            nodesTipSet[c] = b;
            return nPost;
        }
    }

    private void pairDistances(double[][] distances) {
        for(int iTree = 0; iTree < trees.length; ++iTree) {

            TreeInfo info = this.info[iTree];
            for( int k = 0; k < info.postorder.length; ++k ) {
                int nodeIndex = info.postorder[k];
                int leftChildIndex = info.nodeChildren[nodeIndex][0];
                int rightChildIndex = info.nodeChildren[nodeIndex][1];
                FixedBitSet left = info.nodesTipSet[leftChildIndex];
                FixedBitSet right = info.nodesTipSet[rightChildIndex];

                for(int lTip = left.nextSetBit(0); lTip >= 0; lTip = left.nextSetBit(lTip+1)) {
                    for(int rTip = right.nextSetBit(0); rTip >= 0; rTip = right.nextSetBit(rTip+1)) {
                        int tipsInSubtree = info.nodesTipSet[nodeIndex].cardinality();
                        double dist = (tipsInSubtree-1)*(tipsInSubtree-1);
                        distances[Math.min(lTip,rTip)][Math.max(lTip,rTip)] += dist;
                    }
                }
            }

        }

        for(int d = 0; d < distances[0].length; ++d) {
            for(int d1 = d + 1; d1 < distances[0].length; ++d1) {
                distances[d1][d] = distances[d][d1];
            }
        }
    }

    private double[] clusterDistances(int i, int j) {
        final int nUpdates = subTreeTips.size();
        double[] distances = new double[nUpdates+1];

        FixedBitSet joined = new FixedBitSet(subTreeTips.get(i));
        joined.or(subTreeTips.get(j));
        double totWeight = 0.0;
        boolean firstUpdate = true;

        for(int l = 0; l < nUpdates; ++l) {
            // at least one pass for branches for last cluster (nUpdates == 2)
            if( ((l == i || l == j) && nUpdates > 2) || (nUpdates == 2 && l > 0)) {
                continue;
            }

            FixedBitSet joinedk = new FixedBitSet(joined);
            joinedk.or(subTreeTips.get(l));

            for(int iTree = 0; iTree < trees.length; ++iTree) {
                RootedTree tree = (RootedTree)trees[iTree];
                TreeInfo info = this.info[iTree];
                boolean lenFound = !firstUpdate;
                for( int k = 0; k < info.postorder.length; ++k ) {
                    int nodeIndex = info.postorder[k];
                    FixedBitSet nodeBS = info.nodesTipSet[nodeIndex];

                    if( !lenFound && joined.containedIn(nodeBS) ) {
                        int leftChildIndex = info.nodeChildren[nodeIndex][0];
                        int rightChildIndex = info.nodeChildren[nodeIndex][1];

                        double leftLen = tree.getLength(info.allNodes.get(leftChildIndex));
                        double rightLen = tree.getLength(info.allNodes.get(rightChildIndex));

                        int tipsInSubtree = nodeBS.cardinality();
                        int tipsInClusters = joined.cardinality();
                        double w = 1.0 / (1L << (Math.min(62, tipsInSubtree-tipsInClusters)));
                        boolean leftIsI = info.nodesTipSet[leftChildIndex].contains(i);
                        distances[leftIsI ? i : j] += w * leftLen;
                        distances[leftIsI ? j : i] += w * rightLen;
                        totWeight += w;
                        lenFound = true;
                        if( nUpdates == 2 ) {
                            break;
                        }
                    }

                    if( joinedk.containedIn(nodeBS) ) {
                        int tipsInSubtree = nodeBS.cardinality();
                        int tipsInClusters = joinedk.cardinality();
                        double dist = (tipsInSubtree-tipsInClusters+1)*(tipsInSubtree-tipsInClusters+1);
                        distances[l] += dist;
                        break;
                    }
                }

            }
            firstUpdate = false;
        }
        distances[i] /= totWeight;
        distances[j] /= totWeight;
        distances[nUpdates] = totWeight / trees.length;
        return distances;
    }

    private DistanceMatrix pairsDistances() {
        double[][] distances = new double [nExternalNodes][nExternalNodes];
        pairDistances(distances);

        return new BasicDistanceMatrix(taxons, distances);
    }

    ConsensusTreeBuilder(RootedTree[] trees) {
        this.trees = new RootedTree[trees.length];

        Tree first = trees[0];

        //isRooted = first instanceof RootedTree;
        isRooted = true;

        nExternalNodes = first.getExternalNodes().size();


        taxons = new ArrayList<Taxon>(first.getTaxa());

        info = new TreeInfo[trees.length];

        for(int iTree = 0; iTree < trees.length; ++iTree) {
             Tree t = trees[iTree];

            final int nExternal = t.getExternalNodes().size();
            if( isRooted != (t instanceof RootedTree) || nExternal != nExternalNodes ||
                    !t.getTaxa().containsAll(first.getTaxa()) || !Utils.isBinary((RootedTree)t)) {
                throw new IllegalArgumentException("non compatible trees");
            }

            if( isRooted ) {
                RootedTree r = (RootedTree)t;
                this.trees[iTree] = r;
                info[iTree] = new TreeInfo(r);
            } else {
                assert(false);
            }
        }

        subTreeTips = new ArrayList<FixedBitSet>(nExternalNodes);
        for(int k = 0; k < nExternalNodes; ++k) {
            FixedBitSet b = new FixedBitSet(nExternalNodes);
            b.set(k);
            subTreeTips.add(b);
        }

        DistanceMatrix d = pairsDistances();

        builder = new ClusteringTreeBuilder(d, isRooted ? 2 : 3) {
            double[] cachedDist = null;

            protected double[] joinClusters() {
               cachedDist = clusterDistances(besti, bestj);

                subTreeTips.get(besti).or(subTreeTips.get(bestj));
                subTreeTips.remove(bestj);

                return new double[]{cachedDist[besti],cachedDist[bestj]};
            }

            protected void clusterCreated(Node node) {
               node.setAttribute("Consensus support(%)", 100 * cachedDist[cachedDist.length - 1]);
            }

            protected double updatedDistance(int k) {
                return cachedDist[k];
            }
        };
    }

    public Tree build() {
        return builder.build();
    }
}

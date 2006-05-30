package jebl.evolution.trees;

import jebl.evolution.distances.DistanceMatrix;
import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;
import jebl.util.ProgressListener;

import java.util.List;
import java.util.Arrays;

/**
 * Constructs an unrooted tree by neighbor-joining using pairwise distances.
 *
 * Adapted from BEAST code.
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @author Joseph Heled
 *
 * @version $Id$
 */
public class NeighborJoiningTreeBuilder extends ClusteringTreeBuilder<Tree> {

    private final SimpleTree tree;

    /**
     * construct NJ tree
     *
     * @param distanceMatrix distance matrix
     */
    public NeighborJoiningTreeBuilder(DistanceMatrix distanceMatrix) {
        super(distanceMatrix, 3);

        this.tree = new SimpleTree();

        r = new double[distanceMatrix.getSize()];
    }

    //
    // Non public part
    //

    private double[] r;
    private double scale;
    // Find next two clusters to join. set shared best{i,j} and ab{i,j}
    protected void findNextPair() {

        for (int i = 0; i < numClusters; i++) {
            r[i] = 0;
            for (int j = 0; j < numClusters; j++) {
                double dist = getDist(i, j);
                r[i] += dist;
            }
        }

        besti = 0;
        bestj = 1;
        double smax = -1.0;
        scale = 1.0/(numClusters-2);
        for (int i = 0; i < numClusters-1; i++) {
            for (int j = i+1; j < numClusters; j++) {
                double sij = (r[i] + r[j]) * scale - getDist(i, j);

                if (sij > smax) {
                    smax = sij;
                    besti = i;
                    bestj = j;
                }
            }
        }
    }

    protected Tree getTree() {
        return tree;
    }

    protected Node createExternalNode(Taxon taxon) {
        return tree.createExternalNode(taxon);
    }

    protected Node createInternalNode(Node[] nodes, double[] distances) {
        List<Node> a = Arrays.asList(nodes);
        Node node = tree.createInternalNode(a);
        for(int k = 0; k < nodes.length; ++k) {
            tree.setEdge(node, nodes[k], distances[k]);
        }
        return node;
    }

    protected void finish() {
        // Connect up the final two clusters
        int abi = alias[0];
        int abj = alias[1];

        double dij = getDist(0, 1);

        tree.addEdge(clusters[abi], clusters[abj], dij);

        super.finish();
    }

    protected double[] joinClusters() {
        double dij = getDist(besti, bestj);
        double li = (dij + (r[besti] - r[bestj]) * scale) * 0.5;
        double lj = dij - li;

        if (li < 0.0) li = 0.0;
        if (lj < 0.0) lj = 0.0;
        return new double[]{li, lj};
    }

    protected double updatedDistance(int k) {
        final int i = besti;
        final int j = bestj;

        double d = (getDist(k, i) + getDist(k, j) - getDist(i, j)) * 0.5;
        // Some large distances foil the method
        return Math.max(d, 0.0);
    }

}
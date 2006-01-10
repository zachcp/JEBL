package jebl.evolution.trees;

import jebl.evolution.distances.DistanceMatrix;

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
public class NeighborJoiningBuilder extends ClusteringTreeBuilder {

    /**
     * construct NJ tree
     *
     * @param distanceMatrix distance matrix
     */
    public NeighborJoiningBuilder(DistanceMatrix distanceMatrix) {
        super(distanceMatrix, 3);

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
                r[i] += getDist(i,j);
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
        abi = alias[besti];
        abj = alias[bestj];
    }

    protected void finish() {
        // Connect up the final two clusters
        abi = alias[0];
        abj = alias[1];

        double dij = getDist(0, 1);

        tree.addEdge(clusters[abi], clusters[abj], dij);

        super.finish();
    }

    protected double[] newNodeDistance() {
        double dij = getDist(besti, bestj);
        double li = (dij + (r[besti] - r[bestj]) * scale) * 0.5;
        double lj = dij - li;

        if (li < 0.0) li = 0.0;
        if (lj < 0.0) lj = 0.0;

        return new double[]{li, lj};
    }

    /**
     * compute updated distance between the new cluster (i,j)
     * to any other cluster k
     */
    protected double updatedDistance(int i, int j, int k) {
        return (getDist(k, i) + getDist(k, j) - getDist(i, j)) * 0.5;
    }
}

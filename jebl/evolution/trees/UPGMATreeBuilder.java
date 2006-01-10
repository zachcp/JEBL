package jebl.evolution.trees;

import jebl.evolution.distances.DistanceMatrix;

/**
 * constructs a UPGMA tree from pairwise distances
 *
 * @version $Id$
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @author Joseph Heled
 *
 * Adapted from BEAST code.
 */

class UPGMATreeBuilder extends ClusteringTreeBuilder {

    /**
     * constructor UPGMA tree
     *
     * @param distanceMatrix distance matrix
     */
    public UPGMATreeBuilder(DistanceMatrix distanceMatrix) {
        super(distanceMatrix, 2);
    }

    //
    // Protected and Private stuff
    //

    protected void findNextPair() {

        besti = 0;
        bestj = 1;
        double dmin = getDist(0, 1);
        for (int i = 0; i < numClusters-1; i++) {

            for (int j = i+1; j < numClusters; j++) {
                final double dist = getDist(i, j);
                if (dist < dmin) {
                    dmin = dist;
                    besti = i;
                    bestj = j;
                }
            }
        }
        abi = alias[besti];
        abj = alias[bestj];
    }

    protected double[] newNodeDistance() {
        Double d = getDist(besti, bestj) / 2.0;
        return new double[]{d, d};
    }

    /**
     * compute updated distance between the new cluster (i,j)
     * to any other cluster k
     */
    protected double updatedDistance(int i, int j, int k)
    {
        int ai = alias[i];
        int aj = alias[j];

        double tipSum = (double) (tipCount[ai] + tipCount[aj]);

        return 	(((double)tipCount[ai]) / tipSum) * getDist(k, i) +
                (((double)tipCount[aj]) / tipSum) * getDist(k, j);
    }
}

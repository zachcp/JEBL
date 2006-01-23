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
     // want a rooted tree
    /**
     * constructor UPGMA tree
     *
     * @param distanceMatrix distance matrix
     */
    public UPGMATreeBuilder(DistanceMatrix distanceMatrix) {
        super(distanceMatrix, 2, false);
    }

    //
    // Protected and Private stuff
    //

    protected double[] joinClusters() {
        Double d = getDist(besti, bestj) / 2.0;
        return new double[]{d, d};
    }

    protected double updatedDistance(int k) {
        int i = besti;
        int j = bestj;
        int ai = alias[i];
        int aj = alias[j];

        double tipSum = (double) (tipCount[ai] + tipCount[aj]);

        return 	(((double)tipCount[ai]) / tipSum) * getDist(k, i) +
                (((double)tipCount[aj]) / tipSum) * getDist(k, j);
    }
}

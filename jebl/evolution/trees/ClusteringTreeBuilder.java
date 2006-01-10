package jebl.evolution.trees;

import jebl.evolution.distances.DistanceMatrix;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.graphs.Node;

import java.util.List;
import java.util.HashSet;
import java.util.Arrays;

/**
 * An abstract base class for clustering algorithms from pairwise distances
 *
 * @version $Id$
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @author Joseph Heled
 *
 * Adapted from Alexei Drummond BEAST code.
 */

abstract class ClusteringTreeBuilder {
    private SimpleTree tree;

    /**
     * constructor ClusteringTree
     *
     * @param distanceMatrix distance matrix
     */
    public ClusteringTreeBuilder(DistanceMatrix distanceMatrix, int minimumTaxa) throws IllegalArgumentException {
        this.distanceMatrix = distanceMatrix;
        this.tree = new SimpleTree();
        this.minimumTaxa = minimumTaxa;

        if (distanceMatrix.getSize() < minimumTaxa) {
            throw new IllegalArgumentException("less than " + minimumTaxa + " taxa in distance matrix");
        }
    }

    public Tree build() {
        init(distanceMatrix);

        while (true) {
            findNextPair();

            if (numClusters < minimumTaxa)
                break;

            newCluster();
        }
        finish();

        return tree;
    }

    // Find next two clusters to join. set shared best{i,j} and ab{i,j}
    protected abstract void findNextPair();

    // [besti - dist , bestj dist]
    protected abstract double[] newNodeDistance();

    /**
     * compute updated distance between the new cluster (i,j)
     * to any other cluster k.
     *  (i,j,k) are cluster indices in [0..numClusters-1]
     */
    protected abstract double updatedDistance(int i, int j, int k);

    //
    // Protected and Private stuff
    //

    protected double getDist(int a, int b) {
        return distance[alias[a]][alias[b]];
    }

    protected void init(final DistanceMatrix distanceMatrix) {

        numClusters = distanceMatrix.getSize();
        clusters = new SimpleNode[numClusters];

        distance = new double[numClusters][numClusters];
        for (int i = 0; i < numClusters; i++) {
            for (int j = 0; j < numClusters; j++) {
                distance[i][j] = distanceMatrix.getDistance(i, j);
            }
        }

        final List<Taxon> taxa = distanceMatrix.getTaxa();
        for (int i = 0; i < numClusters; i++) {
            clusters[i] = tree.createExternalNode(taxa.get(i));
        }

        alias = new int[numClusters];
        tipCount = new int[numClusters];

        for (int i = 0; i < numClusters; i++) {
            alias[i] = i;
            tipCount[i] = 1;
        }
    }

    protected void newCluster() {
        double[] d = newNodeDistance();
        Node[] n = { clusters[abi], clusters[abj] };
        newCluster = tree.createInternalNode(new HashSet<Node>(Arrays.asList(n)));
        for(int k = 0; k < 2; ++k) {
            tree.setEdge(newCluster, n[k], d[k]);
        }

        clusters[abi] = newCluster;
        clusters[abj] = null;

        // Update distances
        for (int k = 0; k < numClusters; k++) {

            if (k != besti && k != bestj) {

                int ak = alias[k];
                distance[ak][abi] = distance[abi][ak] = updatedDistance(besti, bestj, k);
                distance[ak][abj] = distance[abj][ak] = -1.0;
            }
        }
        distance[abi][abi] = 0.0;
        distance[abj][abj] = -1.0;

        // Update alias
        for (int i = bestj; i < numClusters-1; i++) {
            alias[i] = alias[i+1];
        }

        tipCount[abi] += tipCount[abj];
        tipCount[abj] = 0;

        numClusters--;
    }

    protected void finish() {
        distance = null;
    }

    final protected DistanceMatrix distanceMatrix;

    // Number of current clusters
    protected int numClusters;

    // length numClusters
    protected Node[] clusters;
    protected Node newCluster;

    // Indices of two clusters in [0 .. numClusters-1], besti < bestj
    protected int besti, bestj;
    //  actual index of besti,bestj into arrays (clusters,tipCount,distance)
    protected int abi, abj;
    // Number of tips in cluster
    protected int[] tipCount;

    // Convert from cluster number to index in arrays
    protected int[] alias;

    // Distance between clusters
    protected double[][] distance;

    protected int minimumTaxa;
}

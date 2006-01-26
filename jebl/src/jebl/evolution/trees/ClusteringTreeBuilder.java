package jebl.evolution.trees;

import jebl.evolution.distances.DistanceMatrix;
import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    protected SimpleTree tree = null;
    protected SimpleRootedTree rtree = null;
    private boolean buildUsingBranches = true;

    /**
     * constructor ClusteringTree
     *
     * @param distanceMatrix distance matrix
     */
    public ClusteringTreeBuilder(DistanceMatrix distanceMatrix, int minimumTaxa, boolean useBranches) throws IllegalArgumentException {
        this.distanceMatrix = distanceMatrix;
        buildUsingBranches = useBranches;

        if (minimumTaxa == 2) {
            this.rtree = new SimpleRootedTree();
        } else {
            if( !buildUsingBranches ) {
              throw new IllegalArgumentException("Only rooted trees can use heights");
            }
          this.tree = new SimpleTree();
        }
        this.minimumTaxa = minimumTaxa;

        if (distanceMatrix.getSize() < minimumTaxa) {
            throw new IllegalArgumentException("less than " + minimumTaxa + " taxa in distance matrix");
        }
    }

    public ClusteringTreeBuilder(DistanceMatrix distanceMatrix, int minimumTaxa) throws IllegalArgumentException {
      this(distanceMatrix, minimumTaxa, true);
    }

    public Tree build() {
        init(distanceMatrix);

        while (true) {
            findNextPair();

            abi = alias[besti];
            abj = alias[bestj];
            if (numClusters < minimumTaxa)
                break;

            newCluster();
        }
        finish();

        return tree != null ? tree : rtree;
    }

    /**
     * Find next two clusters to join. set shared best{i,j}.
     */

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
    }

    /**
     * Inform derived class that clusters besti,bestj are being joinded into a new cluster.
     * New cluster will be (the smaller) besti while clusters greater than bestj are shifted one space back.
     *
     * @return  branch distances to new internal node [besti - dist , bestj dist]
     */
    protected abstract double[] joinClusters();

    /**
     * compute updated distance between the new cluster (besti,bestj)
     * to any other cluster k.
     *  (i,j,k) are cluster indices in [0..numClusters-1]
     */
    protected abstract double updatedDistance(int k);

    /**
     *
     * @param node
     */
    protected void clusterCreated(Node node) {}

    //
    // Protected and Private stuff
    //

    protected double getDist(int a, int b) {
        return distance[alias[a]][alias[b]];
    }

    protected void init(final DistanceMatrix distanceMatrix) {

        numClusters = distanceMatrix.getSize();
        clusters = new Node[numClusters];

        distance = new double[numClusters][numClusters];
        for (int i = 0; i < numClusters; i++) {
            for (int j = 0; j < numClusters; j++) {
                distance[i][j] = distanceMatrix.getDistance(i, j);
            }
        }

        final List<Taxon> taxa = distanceMatrix.getTaxa();
        for (int i = 0; i < numClusters; i++) {
            clusters[i] = (tree != null) ? tree.createExternalNode(taxa.get(i)) :
                                           rtree.createExternalNode(taxa.get(i));
        }

        alias = new int[numClusters];
        tipCount = new int[numClusters];

        for (int i = 0; i < numClusters; i++) {
            alias[i] = i;
            tipCount[i] = 1;
        }
    }

    protected void newCluster() {
        double[] d = joinClusters();
        Node[] n = { clusters[abi], clusters[abj] };
        List<Node> a = Arrays.asList(n);
        if( tree != null ) {
            newCluster = tree.createInternalNode(a);
            for(int k = 0; k < 2; ++k) {
                tree.setEdge(newCluster, n[k], d[k]);
            }
        } else {
          newCluster = rtree.createInternalNode(a);
            if( buildUsingBranches )  {
          for(int k = 0; k < 2; ++k) {
             rtree.setLength(n[k], d[k]);
                }
            } else {
                rtree.setHeight(newCluster, d[0]);
          }
        }

        clusterCreated(newCluster);

        clusters[abi] = newCluster;
        clusters[abj] = null;

        // Update distances
        for (int k = 0; k < numClusters; k++) {
            if (k != besti && k != bestj) {
                int ak = alias[k];
                distance[ak][abi] = distance[abi][ak] = updatedDistance(k);
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
    //  Actual index of besti,bestj into arrays (clusters,tipCount,distance)
    private int abi, abj;
    // Number of tips in cluster
    protected int[] tipCount;

    // Convert from cluster number to index in arrays
    protected int[] alias;

    // Distance between clusters
    protected double[][] distance;

    protected int minimumTaxa;
}
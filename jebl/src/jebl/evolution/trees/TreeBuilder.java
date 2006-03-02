package jebl.evolution.trees;

import jebl.evolution.alignments.Alignment;
import jebl.evolution.distances.*;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.align.PairwiseAligner;
import jebl.util.ProgressListener;
import jebl.evolution.taxa.Taxon;

import java.util.List;

/**
 * A meeting point for tree building from sequence data. A very initial form which will develope to encompass more
 * methods and distances. Currently only pairwise distance methods are implemented.
 *
 * @author Joseph Heled
 * @version $Id$
 *
 */

public class TreeBuilder {

    /**
     * Supported methods for tree building
     */
    public enum Method {UPGMA, NEIGHBOR_JOINING}

    /**
     * Supported pairwise distance methods
     */
    public enum DistanceModel { JukesCantor, F84, HKY, TamuraNei }

    /**
     *
     * @param method
     * @return Wheather method generates a rooted or unrooted tree.
     */
    public static boolean isRootedMethod(Method method) {
        switch( method ) {
            case UPGMA:
            {
                return true;
            }
            case NEIGHBOR_JOINING:
            default:
            {
                return false;
            }
        }
    }

    /**
     *
     * @param method build method to use.
     * @param distances Pre computed pairwise distances.
     * @return A tree builder using method and distance matrix
     */
    static public ClusteringTreeBuilder getBuilder(Method method, DistanceMatrix distances) {
        ClusteringTreeBuilder c;
        switch( method ) {
            case UPGMA:
            {
                c = new UPGMATreeBuilder(distances);
                break;
            }
            case NEIGHBOR_JOINING:
            default:
            {
                c = new NeighborJoiningBuilder(distances);
                break;
            }
        }
        return c;
    }

    static public Tree build(Alignment alignment, Method method, DistanceModel model, ProgressListener progress) {
        progress.setMessage("Computing genetic distance for all pairs");
        DistanceMatrix d;

        boolean timeit = false;

        if( timeit ) progress = null;
        long start = timeit ? System.currentTimeMillis() : 0;              

        switch( model ) {
            case JukesCantor:
            default:
                d = new JukesCantorDistanceMatrix(alignment, progress);
                break;
            case F84:
                d = new F84DistanceMatrix(alignment, progress);
                break;
             case HKY:
                d = new HKYDistanceMatrix(alignment, progress);
                break;
              case TamuraNei:
                d = new TamuraNeiDistanceMatrix(alignment, progress);
                break;
        }
        if( timeit ) {
            System.out.println("took " +(System.currentTimeMillis() - start) + " to build distance matrix");
        }

        if( progress != null ) progress.setMessage("Building tree");
        return getBuilder(method, d).build();
    }

    static public ConsensusTreeBuilder buildUnRooted(Tree[] trees, Taxon outGroup, double supportThreshold) {
        return new GreedyConsensusTreeBuilder(trees, outGroup, supportThreshold);
    }

    static public ConsensusTreeBuilder buildRooted(Tree[] trees, double supportThreshold) {
        return new RootedConsensusTreeBuilder(trees, supportThreshold);
    }


    static public class Result {
        public final Tree tree;
        public final DistanceMatrix distance;

        Result(Tree tree, DistanceMatrix distance) {
            this.tree = tree;
            this.distance = distance;
        }
    }

    static public Result build(List<Sequence> seqs, Method method, PairwiseAligner aligner,
                               ProgressListener progress) {
       progress.setMessage("Computing genetic distance for all pairs");
       final DistanceMatrix d = new SequenceAlignmentsDistanceMatrix(seqs, aligner, progress);
       progress.setMessage("Building tree");
       final Tree t = getBuilder(method, d).build();
       return new Result(t, d);
    }
}

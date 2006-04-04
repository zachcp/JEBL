package jebl.evolution.trees;

import jebl.evolution.align.PairwiseAligner;
import jebl.evolution.alignments.Alignment;
import jebl.evolution.distances.*;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.taxa.Taxon;
import jebl.util.ProgressListener;

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
    public enum Method { NEIGHBOR_JOINING, UPGMA }

    /**
     * Supported pairwise distance methods
     */
    public enum DistanceModel { JukesCantor, F84, HKY, TamuraNei }

    /**
     * Supported consesus methods.
     */
    public enum ConsensusMethod { GREEDY, MRCAC }

    
    public static String niceName(Method method) {
        switch( method ){
            case UPGMA: return "UPGMA";
            case NEIGHBOR_JOINING: return "Neighbor Joining";
        }
        return null;
    }

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
        ClusteringTreeBuilder builder;
        switch( method ) {
            case UPGMA:
            {
                builder = new UPGMATreeBuilder(distances);
                break;
            }
            case NEIGHBOR_JOINING:
            default:
            {
                builder = new NeighborJoiningBuilder(distances);
                break;
            }
        }
        return builder;
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

    static public ConsensusTreeBuilder buildUnRooted(Tree[] trees, Taxon outGroup, double supportThreshold, ConsensusMethod method) {
        switch( method ) {
            case GREEDY: {
                return new GreedyUnrootedConsensusTreeBuilder(trees, outGroup, supportThreshold);
            }
        }
        // bug
        throw new IllegalArgumentException(method.toString());
    }

    static public ConsensusTreeBuilder buildRooted(RootedTree[] trees, double supportThreshold, ConsensusMethod method) {
        switch( method ) {
            case GREEDY: {
                 return new GreedyRootedConsensusTreeBuilder(trees, supportThreshold);
            }
            case MRCAC: {
                return new MRCACConsensusTreeBuilder(trees, supportThreshold);
            }
        }
        // bug
        throw new IllegalArgumentException(method.toString());
    }

    /**
     * convenience method. Convert arrays of trees, guaranteed to be rooted to the array of the appropriate
     * type.
     * @param trees trees - all must be rooted
     * @param supportThreshold
     * @return consensus tree builder
     */
    static public ConsensusTreeBuilder buildRooted(Tree[] trees, double supportThreshold, ConsensusMethod method) {
        RootedTree[] rtrees = new RootedTree[trees.length];
        for(int i = 0; i < trees.length; ++i) {
           rtrees[i] = (RootedTree)trees[i];
        }
        return buildRooted(rtrees, supportThreshold, method);
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

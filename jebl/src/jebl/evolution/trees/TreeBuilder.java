package jebl.evolution.trees;

import jebl.evolution.alignments.Alignment;
import jebl.evolution.distances.*;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.align.PairwiseAligner;
import jebl.evolution.align.AlignmentProgressListener;
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

    static public Tree build(Alignment alignment, Method method, DistanceModel model) {
        DistanceMatrix d;
        switch( model ) {
            case JukesCantor:
            default:
                d = new JukesCantorDistanceMatrix(alignment);
                break;
            case F84:
                d = new F84DistanceMatrix(alignment);
                break;
             case HKY:
                d = new HKYDistanceMatrix(alignment);
                break;
              case TamuraNei:
                d = new TamuraNeiDistanceMatrix(alignment);
                break;
        }

        return getBuilder(method, d).build();
    }

    static public ConsensusTreeBuilder buildUnRooted(Tree[] trees, Taxon outGroup, double supportThreshold) {
        return new UnRootedConsensusTreeBuilder(trees, outGroup, supportThreshold);
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
                               AlignmentProgressListener progress) {
       final DistanceMatrix d = new SequenceAlignmentsDistanceMatrix(seqs, aligner, progress);
       final Tree t = getBuilder(method, d).build();
       return new Result(t, d);
    }
}

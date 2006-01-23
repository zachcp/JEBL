package jebl.evolution.trees;

import jebl.evolution.alignments.Alignment;
import jebl.evolution.distances.JukesCantorDistanceMatrix;
import jebl.evolution.distances.DistanceMatrix;
import jebl.evolution.distances.F84DistanceMatrix;
import jebl.evolution.distances.SequenceAlignmentsDistanceMatrix;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.align.PairwiseAligner;
import jebl.evolution.align.AlignmentProgressListener;

import java.util.List;

/**
 * A meeting point for tree building. A very initial form which will develope to encompass more
 * methods and distances.
 *
 * @author Joseph Heled
 * @version $Id$
 *
 */

public class TreeBuilder {

    public enum Method {UPGMA, NEIGHBOR_JOINING}
    public enum DistanceModel { JukesCantor, F84 }

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
        }

        ClusteringTreeBuilder c;
        switch( method ) {
            case UPGMA:
            {
                c = new UPGMATreeBuilder(d);
                break;
            }
            case NEIGHBOR_JOINING:
            default:
            {
                c = new NeighborJoiningBuilder(d);
                break;
            }

        }
        return c.build();
    }

    static public Tree build(RootedTree[] trees) {
        ConsensusTreeBuilder b = new ConsensusTreeBuilder(trees);
        return b.build();
    }

    static public Tree build(List<Sequence> seqs, PairwiseAligner aligner, AlignmentProgressListener progress) {
       DistanceMatrix d = new SequenceAlignmentsDistanceMatrix(seqs, aligner, progress);
       return new NeighborJoiningBuilder(d).build();
    }
}

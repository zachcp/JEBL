package jebl.evolution.align;

import jebl.evolution.trees.Tree;
import jebl.evolution.trees.TreeBuilderFactory;
import jebl.evolution.trees.TreeBuilder;
import jebl.evolution.distances.*;
import jebl.evolution.alignments.Alignment;
import jebl.evolution.sequences.Sequence;
import jebl.util.ProgressListener;

import java.util.List;

/**
 * @author Joseph Heled
 * @version $Id$
 */
public class AlignmentTreeBuilderFactory {
    static public Tree build(Alignment alignment, TreeBuilderFactory.Method method, TreeBuilderFactory.DistanceModel model, ProgressListener progress) {
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

        TreeBuilder treeBuilder = TreeBuilderFactory.getBuilder(method, d);
        treeBuilder.addProgressListener(progress);
        return treeBuilder.build();
    }


    static public class Result {
        public final Tree tree;
        public final DistanceMatrix distance;

        Result(Tree tree, DistanceMatrix distance) {
            this.tree = tree;
            this.distance = distance;
        }
    }

    static public Result build(List<Sequence> seqs, TreeBuilderFactory.Method method, PairwiseAligner aligner,
                               ProgressListener progress) {
        progress.setMessage("Computing genetic distance for all pairs");
        final DistanceMatrix d = new SequenceAlignmentsDistanceMatrix(seqs, aligner, progress);
        progress.setMessage("Building tree");
        TreeBuilder treeBuilder = TreeBuilderFactory.getBuilder(method, d);
        treeBuilder.addProgressListener(progress);
        final Tree t = treeBuilder.build();
        return new Result(t, d);
    }
}

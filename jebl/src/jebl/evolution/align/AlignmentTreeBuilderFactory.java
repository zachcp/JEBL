package jebl.evolution.align;

import jebl.evolution.trees.Tree;
import jebl.evolution.trees.TreeBuilderFactory;
import jebl.evolution.trees.TreeBuilder;
import jebl.evolution.trees.SimpleRootedTree;
import jebl.evolution.distances.*;
import jebl.evolution.alignments.Alignment;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.graphs.Node;
import jebl.util.ProgressListener;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Joseph Heled
 * @version $Id$
 */
public class AlignmentTreeBuilderFactory {
    static public Tree build(Alignment alignment, TreeBuilderFactory.Method method, TreeBuilderFactory.DistanceModel model, ProgressListener progress) {

        if( progress != null ) {
            progress.setMessage("Computing genetic distance for all pairs");
        }
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

        if( progress != null ) {
            progress.setMessage("Building tree");
        }

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

    static public Result build(List<Sequence> seqs, TreeBuilderFactory.Method method, MultipleAligner aligner,
                                boolean needDistances, ProgressListener progress) {
        SimpleRootedTree gtree = new SimpleRootedTree();
        List<Node> nodes = new ArrayList<Node>();
        for(Sequence s : seqs) {
            Node tip = gtree.createExternalNode(s.getTaxon());
            nodes.add(tip);
        }

        int nnodes = nodes.size();
        while( nnodes > 1 ) {
           List<Node> upnodes = new ArrayList<Node>();
            for(int k = 0; k < nnodes/2; ++k) {
                upnodes.add(gtree.createInternalNode(nodes.subList(2*k,2*k+2)));
            }
            if( (nnodes & 1) != 0 ) {
                upnodes.add(nodes.get(nnodes - 1));
            }
            nodes = upnodes;
            nnodes = nodes.size();
        }

        final int alignWork = seqs.size()-1;
        final int treeWork = 1;
        final int matrixWork = needDistances ? 1 : 0;

        CompoundAlignmentProgressListener p = new CompoundAlignmentProgressListener(progress,
                                                                                    alignWork + treeWork + matrixWork);
        final ProgressListener minorProgress = p.getMinorProgress();

        progress.setMessage("Building alignment for guide");
        p.setSectionSize(alignWork);
        final Alignment alignment = aligner.doAlign(seqs, gtree, minorProgress);
        p.incrementSectionsCompleted(alignWork);

        final boolean isProtein = seqs.get(0).getSequenceType().getCanonicalStateCount() > 4;

        final TreeBuilderFactory.DistanceModel distanceModel =
                isProtein ? TreeBuilderFactory.DistanceModel.JukesCantor : TreeBuilderFactory.DistanceModel.HKY;

        p.setSectionSize(treeWork);
        progress.setMessage("Building guide tree from alignment");
        final Tree guidTree = build(alignment, method, distanceModel, minorProgress);
        p.incrementSectionsCompleted(treeWork);

        DistanceMatrix distanceMatrix = null;
        if( needDistances ) {
            p.setSectionSize(matrixWork);
            progress.setMessage("Computing genetic distance for all pairs");

            if( distanceModel == TreeBuilderFactory.DistanceModel.HKY ) {
                distanceMatrix = new HKYDistanceMatrix(alignment, minorProgress);
            } else {
                distanceMatrix = new JukesCantorDistanceMatrix(alignment, minorProgress);
            }
            p.incrementSectionsCompleted(matrixWork);
        }
        return new Result(guidTree, distanceMatrix);
    }
}

package jebl.evolution.align;

import jebl.evolution.distances.*;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.TreeBuilderFactory;
import jebl.util.CompositeProgressListener;
import jebl.util.ProgressListener;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Builds a distance matrix by performing a series of pairwise alignments between the
 * specified sequences (unlike the methods in jebl.evolution.distances, which
 * extract the pairwise distances from a multiple sequence alignment).
 *
 * @author Joseph Heled
 * @version $Id$
 *
 */
public class SequenceAlignmentsDistanceMatrix extends BasicDistanceMatrix {
    
    public SequenceAlignmentsDistanceMatrix(List<Sequence> seqs, PairwiseAligner aligner, ProgressListener progress)
            throws CannotBuildDistanceMatrixException
    {
        super(getTaxa(seqs), getDistances(seqs, aligner, getDefaultDistanceModel(seqs), progress));
    }

    public SequenceAlignmentsDistanceMatrix(List<Sequence> seqs, PairwiseAligner aligner, ProgressListener progress, TreeBuilderFactory.DistanceModel model)
            throws CannotBuildDistanceMatrixException
    {
        super(getTaxa(seqs), getDistances(seqs, aligner, model, progress));
        boolean isProtein = seqs.get(0).getSequenceType().getCanonicalStateCount() > 4;
        if (model != TreeBuilderFactory.DistanceModel.JukesCantor && isProtein) {
            throw new IllegalArgumentException("Model " + model + " does not support protein sequences");
        }
    }

    static List<Taxon> getTaxa(List<Sequence> seqs) {
        List<Taxon> t = new ArrayList<Taxon>();
        for( Sequence s : seqs ) {
            t.add(s.getTaxon());
        }
        return t;
    }

    public static TreeBuilderFactory.DistanceModel getDefaultDistanceModel(List<Sequence> seqs) {
        boolean isProtein = seqs.get(0).getSequenceType().getCanonicalStateCount()> 4;

        if(isProtein) {
            return TreeBuilderFactory.DistanceModel.JukesCantor;
        } else {
            return TreeBuilderFactory.DistanceModel.F84;
        }
    }

    private static String getNotEnoughMemoryMessage(long memoryRequired) {
        return "Not enough memory to build distance matrix. " + String.format("%,d", memoryRequired / 1000 / 1000) + " MB required";
    }

    private static double[][] getDistances(List<Sequence> seqs, PairwiseAligner aligner, TreeBuilderFactory.DistanceModel model, final ProgressListener progressListener) throws CannotBuildDistanceMatrixException {
        final int n = seqs.size();
        double [][] d;

        final long memoryRequired = ((long)n)*n*8;
        if (n>100) {
            final long maxMemory = Runtime.getRuntime().maxMemory();
            if (memoryRequired>maxMemory) {
                throw new CannotBuildDistanceMatrixException(getNotEnoughMemoryMessage(memoryRequired));
            }
        }
        try {
            d = new double[n][n];
        } catch (OutOfMemoryError e) {
            throw new CannotBuildDistanceMatrixException(getNotEnoughMemoryMessage(memoryRequired));
        }

        CompositeProgressListener compositeProgressListener = new CompositeProgressListener(progressListener,getProgressIncrements(n));

        for(int i = 0; i < n; ++i) {
            compositeProgressListener.beginSubtask();
            CompositeProgressListener subComposite = new CompositeProgressListener(compositeProgressListener, (n - (i + 1))*2);
            for(int j = i+1; j < n; ++j) {
                subComposite.beginSubtask();
                PairwiseAligner.Result result = aligner.doAlignment(seqs.get(i), seqs.get(j), subComposite);
                if(progressListener.isCanceled()) return d;

                subComposite.beginSubtask();
                BasicDistanceMatrix matrix;
                switch( model ) {
                    case F84:
                        matrix = new F84DistanceMatrix(result.alignment, subComposite);
                        break;
                    case HKY:
                        matrix = new HKYDistanceMatrix(result.alignment, subComposite);
                        break;
                    case TamuraNei:
                        matrix = new TamuraNeiDistanceMatrix(result.alignment, subComposite);
                        break;
                    case JukesCantor:
                    default:
                        matrix = new JukesCantorDistanceMatrix(result.alignment, subComposite);
                }
                d[i][j] = matrix.getDistances()[0][1];
                d[j][i] = d[i][j];
            }
        }
        return d;
    }

    private static double[] getProgressIncrements(int n) {
        double[] increments = new double[n];
        for (int i = 0; i < n; ++i) {
            int increment = n - i;
            increments[i] = increment;
        }
        return increments;
    }
}
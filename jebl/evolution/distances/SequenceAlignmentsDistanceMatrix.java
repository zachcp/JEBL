package jebl.evolution.distances;

import jebl.evolution.sequences.Sequence;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.align.PairwiseAligner;
import jebl.evolution.align.AlignmentProgressListener;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: joseph
 * Date: 18/01/2006
 * Time: 09:09:37
 *
 * @author Joseph Heled
 * @version $Id$
 *
 */
public class SequenceAlignmentsDistanceMatrix extends BasicDistanceMatrix {
    public SequenceAlignmentsDistanceMatrix(List<Sequence> seqs, PairwiseAligner aligner, AlignmentProgressListener progress) {
        super(getTaxa(seqs), getDistances(seqs, aligner, progress));
    }

    static List<Taxon> getTaxa(List<Sequence> seqs) {
        List<Taxon> t = new ArrayList<Taxon>();
        for( Sequence s : seqs ) {
            t.add(s.getTaxon());
        }
        return t;
    }



    static double[][] getDistances(List<Sequence> seqs, PairwiseAligner aligner, final AlignmentProgressListener progress) {
        final int n = seqs.size();
        double [][] d = new double[n][n];

        AlignmentProgressListener minorProgress = new AlignmentProgressListener() {
            boolean cancelled = false;
            public int sectionsCompleted = 0;
            final int totalSections = (n* (n-1))/2;

            public boolean setProgress(double fractionCompleted) {
                double totalProgress = (sectionsCompleted + fractionCompleted)/totalSections;
                if( progress.setProgress(totalProgress) ) cancelled = true;
                return cancelled;
            }
        };

        for(int i = 0; i < n; ++i) {
            for(int j = i+1; j < n; ++j) {
                PairwiseAligner.Result result = aligner.doAlignment(seqs.get(i), seqs.get(j), minorProgress);

                d[i][j] = new F84DistanceMatrix(result.alignment).getDistances()[0][1];
                d[j][i] = d[i][j];
            }
        }
        return d;
    }
}

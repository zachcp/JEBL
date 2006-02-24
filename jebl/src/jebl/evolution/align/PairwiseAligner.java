package jebl.evolution.align;

import jebl.evolution.sequences.Sequence;
import jebl.evolution.alignments.Alignment;
import jebl.util.ProgressListener;

/**
 * Created by IntelliJ IDEA.
 * User: joseph
 * Date: 18/01/2006
 * Time: 09:29:39
 *
 * @author Joseph Heled
 * @version $Id$
 *
 */
public interface PairwiseAligner {
    public class Result {
        final public Alignment alignment;
        final public double score;

        Result(Alignment alignment, double score) {
            this.alignment = alignment;
            this.score = score;
        }
    }

    Result doAlignment(Sequence seq1, Sequence seq2, ProgressListener progress);

    double getScore(Sequence seq1, Sequence seq2);
}

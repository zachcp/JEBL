package jebl.evolution.aligners.pairwise;

import jebl.evolution.aligners.Aligner;
import jebl.evolution.alignments.Alignment;
import jebl.evolution.sequences.Sequence;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id$
 */
public interface PairwiseAligner extends Aligner {

    /**
     * Aligns a pair of sequences.
     *
     * @param sequence1
     * @param sequence2
     */
    Alignment alignSequences(Sequence sequence1, Sequence sequence2);

}

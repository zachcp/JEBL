package jebl.evolution.distances;

import jebl.evolution.alignments.Alignment;
import jebl.evolution.sequences.Nucleotides;
import jebl.evolution.sequences.Sequence;

/**
 *
 * @author Joseph Heled
 * @version $Id$
 *
 */
public class ModelBasedDistanceMatrix  {
    protected static final double MAX_DISTANCE = 1000.0;

    protected  static double freqR, freqY;

    protected static double[] getFrequencies(Alignment alignment) {
        final int stateCount = alignment.getSequenceType().getCanonicalStateCount();

        double[] freqs = new double[stateCount];
        long count = 0;
        for( Sequence sequence : alignment.getSequences() ) {
            for( int i : sequence.getStateIndices() ) {
                // ignore non definite states (ask alexei)
                if( i < stateCount ) {
                    ++freqs[i];
                    ++count;
                }
            }
        }

        for(int i = 0; i < stateCount; ++i) {
            freqs[i] /= count;
        }

        if( stateCount == 4 ) {
           freqR = freqs[Nucleotides.A_STATE.getIndex()] + freqs[Nucleotides.G_STATE.getIndex()];
           freqY = freqs[Nucleotides.C_STATE.getIndex()] + freqs[Nucleotides.T_STATE.getIndex()];
        }

        // in extream cases avoid divide by zero
        if( freqR == 0 ) freqR = 1;
        if( freqY == 0 ) freqY = 1;

        return freqs;
    }
}

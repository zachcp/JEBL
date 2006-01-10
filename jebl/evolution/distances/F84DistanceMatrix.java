package jebl.evolution.distances;

import jebl.evolution.taxa.Taxon;
import jebl.evolution.alignments.Alignment;
import jebl.evolution.alignments.Pattern;
import jebl.evolution.sequences.State;
import jebl.evolution.sequences.Nucleotides;
import jebl.evolution.sequences.Sequence;

import java.util.Collection;

/**

 * @author Joseph Heled
 * @version $Id$
 *
 */
public class F84DistanceMatrix extends BasicDistanceMatrix {

    public F84DistanceMatrix(Alignment alignment) {
        super(alignment.getTaxa(), getDistances(alignment));
    }

    //
    // Private stuff
    //
    private static final double MAX_DISTANCE = 1000.0;
    private static Alignment alignment;

    //used in correction formula
    static private double constA, constB, constC;

	/**
	 * Calculate a pairwise distance
	 */
	static private double calculatePairwiseDistance(int taxon1, int taxon2) {

		//int n = patterns.getPatternCount();
		double weight, distance;
		double sumTs = 0.0;
		double sumTv = 0.0;
		double sumWeight = 0.0;

		//int[] pattern;

        for( Pattern pattern : alignment.getPatterns() ) {
             State state1 = pattern.getState(taxon1);
             State state2 = pattern.getState(taxon2);

             weight = pattern.getWeight();
               // acgt
            if (!state1.isAmbiguous() && !state2.isAmbiguous() && state1 != state2) {
              if ( (state1 == Nucleotides.A_STATE && state2 == Nucleotides.G_STATE) ||
                    (state1 == Nucleotides.G_STATE && state2 == Nucleotides.A_STATE)) {
					// it's a transition
					sumTs += weight;
				} else {
					// it's a transversion
					sumTv += weight;
				}
			}
			sumWeight += weight;
        }

		double P = sumTs / sumWeight;
		double Q = sumTv / sumWeight;

		double tmp1 = Math.log(1.0 - (P / (2.0 * constA)) -
								(((constA - constB) * Q) / (2.0 * constA * constC)));

		double tmp2 = Math.log(1.0 - (Q / (2.0 * constC)));

		distance = -(2.0 * constA * tmp1) +
					(2.0 * (constA - constB - constC) * tmp2);

		if (distance < MAX_DISTANCE) {
			return distance;
		} else {
			return MAX_DISTANCE;
		}
	}


    synchronized static double[][] getDistances(Alignment alignment) {
        F84DistanceMatrix.alignment = alignment;

        // ASK Alexei
        int stateCount = alignment.getSequenceType().getCanonicalStateCount();

        if (stateCount != 4) {
			throw new IllegalArgumentException("F84DistanceMatrix must have nucleotide patterns");
		}

        double[] freqs = new double[stateCount];
        for( Sequence sequence : alignment.getSequences() ) {
           for( int i : sequence.getStateIndices() ) {
               assert( (0 <= i && i < stateCount) ||
                       i == sequence.getSequenceType().getGapState().getIndex() ||
                       i == sequence.getSequenceType().getUnknownState().getIndex() );
             if( i < stateCount ) ++freqs[i];
           }
        }

      /*  for( Pattern p : alignment.getPatterns() ) {
            for(int k = 0; k < p.getLength(); ++k) {
              freqs[k] += p.getStateFrequency(p.getState(k));
            }
        }  */

        // Ask Alexei
        double freqA = freqs[0];
		double freqC = freqs[1];
		double freqG = freqs[2];
		double freqT = freqs[3];

		double freqR = freqA + freqG;
		double freqY = freqC + freqT;

		constA =  ((freqA * freqG) / freqR) + ((freqC * freqT) / freqY);
		constB =  (freqA * freqG) + (freqC * freqT);
		constC =  (freqR * freqY);

        int dimension = alignment.getTaxa().size();
        double[][] distances = new double[dimension][dimension];

        for(int i = 0; i < dimension; ++i) {
            for(int j = i+1; j < dimension; ++j) {
                distances[i][j] = calculatePairwiseDistance(i, j);
                distances[j][i] = distances[i][j];
            }
        }

        return distances;
    }
}

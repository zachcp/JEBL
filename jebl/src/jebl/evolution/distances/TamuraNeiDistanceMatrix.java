package jebl.evolution.distances;

import jebl.evolution.alignments.Alignment;
import jebl.evolution.alignments.Pattern;
import jebl.evolution.sequences.Nucleotides;
import jebl.evolution.sequences.State;

/**
 * Created by IntelliJ IDEA.
 * User: joseph
 * Date: 22/01/2006
 * Time: 17:28:59
 *
 * @author Joseph Heled
 * @version $Id$
 *
 *  Estimation of the Number of Nucleotide Substitutions in
 *  the Control Region of Mitochondrial DNA in Humans and
 *  Chimpanzees. Koichiro Tamura and Masatoshi Nei, 1993
 *
 * Estimated Distance is d = 2 (pi(A) pi(G) a1 + pi(T) pi(C) a2 + PI(A)PI(C)) t
 */

public class TamuraNeiDistanceMatrix extends BasicDistanceMatrix {

    public TamuraNeiDistanceMatrix(Alignment alignment) {
        super(alignment.getTaxa(), Initialaizer.getDistances(alignment));
    }

    static class Initialaizer extends ModelBasedDistanceMatrix {

        private static Alignment alignment;

        // used in correction formula
        private static double constA1, constA2, constC;

        /**
         * Calculate a pairwise distance
         */
        static private double calculatePairwiseDistance(int taxon1, int taxon2) {

            double sumTsAG = 0.0;
            double sumTsCT = 0.0;
            double sumTv = 0.0;
            double sumWeight = 0.0;

            for( Pattern pattern : alignment.getPatterns() ) {
                State state1 = pattern.getState(taxon1);
                State state2 = pattern.getState(taxon2);

                double weight = pattern.getWeight();
                // acgt
                if (!state1.isAmbiguous() && !state2.isAmbiguous() && state1 != state2) {
                    if ( Nucleotides.isTransition(state1, state2) ) {
                        // it's a transition
                        if( Nucleotides.isPurine(state1) ) {
                            sumTsAG += weight;
                        } else {
                            sumTsCT += weight;
                        }
                    } else {
                        // it's a transversion
                        sumTv += weight;
                    }
                }
                sumWeight += weight;
            }

            double P1 = sumTsAG / sumWeight;
            double P2 = sumTsCT / sumWeight;
            double Q  = sumTv / sumWeight;

            double tmp11 = Math.log(1.0 - P1 * (1/(2*constA1)) - Q * (1/(2*freqR)));
            double tmp12 = Math.log(1.0 - P2 * (1/(2*constA2)) - Q * (1/(2*freqY))) ;

            double tmp2 = Math.log(1.0 - (Q / (2.0 * constC)));

            double distance = -2.0 * ((constC - constA1*freqY - constA2*freqR) * tmp2
                                       + constA1 * tmp11 + constA2 * tmp12);

            return Math.min(distance, MAX_DISTANCE);
        }


        static double[][] getDistances(Alignment alignment) {
            Initialaizer.alignment = alignment;

            // ASK Alexei
            final int stateCount = alignment.getSequenceType().getCanonicalStateCount();

            if (stateCount != 4) {
                throw new IllegalArgumentException("HKYDistanceMatrix must have nucleotide patterns");
            }

            double[] freqs = getFrequencies(alignment);

            // Ask Alexei (mapping 0-a etc)
            double freqA = freqs[Nucleotides.A_STATE.getIndex()];
            double freqC = freqs[Nucleotides.C_STATE.getIndex()];
            double freqG = freqs[Nucleotides.G_STATE.getIndex()];
            double freqT = freqs[Nucleotides.T_STATE.getIndex()];

            constA1 = (freqA * freqG) / freqR;
            constA2 = (freqT * freqC) / freqY;
            constC =  (freqR * freqY);

            final int dimension = alignment.getTaxa().size();
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
}
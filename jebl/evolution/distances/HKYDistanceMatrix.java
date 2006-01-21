package jebl.evolution.distances;

import jebl.evolution.alignments.Alignment;
import jebl.evolution.alignments.Pattern;
import jebl.evolution.sequences.State;
import jebl.evolution.sequences.Nucleotides;
import jebl.evolution.sequences.Sequence;

/**
 *  Compute HKY corrected distance matrix
 *
 * @author Andrew Rambaut
 * @author Joseph Heled
 * @version $Id$
 *
 * Adapted from BEAST source code.
 *
 * The code in this file appeared originally in the file F84DistanceMatrix, and the comment said (as above) HKY.
 * Initially I thought it was one model under two different names, but that was my ignorance. While similar,
 * there is an small difference which I was able to understance only by examining the transition
 * probability and rate matrices for both models.
 *
 * Simply put, HKY assumes *the same* ratio of transitions/transversions for all bases, while F84 has a
 * different ratio for Purines and Pyrimidines. The confusion stems from the fact that those two different ratios
 * depend on just one parameter. If Kappa is the ratio for HKY then for F84
 * Kappa(Purine aka A,G) = 1 + Kappa/(pi(A)+pi(G)) and Kappa(Pyrimidine aka C,T) = 1 + Kappr/(pi(C)+pi(T)).
 *
 * This difference simplifies the estimation of evolutionary distance and Kappa from F84, since some
 * entries in the HKY transition matrix contain expressions involving exp(-t alpha), where alpha depend on both Kappa
 * and wheather the element is a Purine or a Pyrimidine.
 *
 * pi(A,C,G,T) = stationary frequencies
 *
 * K(AG) = 1 + Kappa/(pi(A)+pi(G))    F84
 *         Kappa                      HKY
 * K(CT) = 1 + Kappa/(pi(C)+pi(T))    F84
 *         Kappa                      HKY
 *
 * Rate Matrix
 *      |   -     1   K(AG)   1   |   | pi(A)   0     0     0   |
 * Q =  |   1     -     1   K(CT) |   |  0    pi(C)   0     0   |
 *      | K(AG)   1     -     1   |   |  0      0   pi(G)   0   |
 *      |   1   K(CT)   1,    -   |   |  0      0     0   pi(T) |
 *
 *
 * Transition Probability Matrix
 *
 * PI(A) = PI(G) = pi(A) + pi(G)
 * PI(C) = PI(T) = pi(C) + pi(T)
 *
 * Alpha(j) = 1 + Kappa        F84
 *            1 + PI(j)*(k-1)  HKY
 *
 * P(transversion) = pi,(i+1)%4 = pi,(i+3)%4 =  pi(i) * ( 1 - exp(-t) )
 * P(transition) = pi,(i+2)%4 = pi(i) + pi(j)(1/PI(j) - 1) * exp(-t)  -    pi(i)/PI(i)    * exp(-t * Alpha(i))
 * Pi,i =                       pi(i) + pi(i)(1/PI(i) - 1) * exp(-t)  - (pi(i)/PI(i) - 1) * exp(-t * Alpha(i))
 *
 *
 * From the above it is easy to see that for F84 Sum(all transversions) = 2 Sum(p(i)) (1 - exp(-t)) = 2(1-exp(-t))
 * which gives the best estimate of t as -log(1 - Sum(all transversions)/2).
 * When there are no transversions (say when sequences are very short or distance is very small) this estimate is 0
 * even when sequences are not identical. I am not sure if there is an easy way to fix this since in this case
 * Kappa is confused with t and only t*(Kappa+1) can be estimated.
 *
 * I am still confused about the code in this file and wheather the formulas are correct. They certainly
 * don't solve the above model. To complicate the situation further the original code had either a bug
 * or a documentation error - The documentation proclaimed transitions/transversions are being counted but code
 * counted only A<-->G substitutions and not C<-->T. Also counting the ratios of transitions to total sites look
 * suspect since estimating transition/transversion rates requires dividing by the number of bases in the sequence,
 * i.e.  #A->G / #A. (See F84DistanceMatrix code, which has been verified).
 */

public class HKYDistanceMatrix extends BasicDistanceMatrix {

    public HKYDistanceMatrix(Alignment alignment) {
        super(alignment.getTaxa(), Initialaizer.getDistances(alignment));
    }

    static class Initialaizer {
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

            double weight, distance;
            double sumTs = 0.0;
            double sumTv = 0.0;
            double sumWeight = 0.0;

            for( Pattern pattern : alignment.getPatterns() ) {
                State state1 = pattern.getState(taxon1);
                State state2 = pattern.getState(taxon2);

                weight = pattern.getWeight();
                // acgt
                if (!state1.isAmbiguous() && !state2.isAmbiguous() && state1 != state2) {
                    if ( Nucleotides.isTransition(state1, state2) ) {
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

            return Math.min(distance, MAX_DISTANCE);
        }


        static double[][] getDistances(Alignment alignment) {
            Initialaizer.alignment = alignment;

            // ASK Alexei
            final int stateCount = alignment.getSequenceType().getCanonicalStateCount();

            if (stateCount != 4) {
                throw new IllegalArgumentException("HKYDistanceMatrix must have nucleotide patterns");
            }

            double[] freqs = new double[stateCount];
            for( Sequence sequence : alignment.getSequences() ) {
                for( int i : sequence.getStateIndices() ) {
                    /*if( !((0 <= i && i < stateCount) ||
                           i == sequence.getSequenceType().getGapState().getIndex() ||
                           i == sequence.getSequenceType().getUnknownState().getIndex()) ) {
                       System.out.print(i);

                   } */
                    // ignore non definite states (ask alexei)
                    if( i < stateCount ) ++freqs[i];
                }
            }

            // Ask Alexei (mapping 0-a etc)
            double freqA = freqs[0];
            double freqC = freqs[1];
            double freqG = freqs[2];
            double freqT = freqs[3];

            double freqR = freqA + freqG;
            double freqY = freqC + freqT;

            // in extream cases avoid divide by zero
            if( freqR == 0 ) freqR = 1;
            if( freqY == 0 ) freqY = 1;

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
}

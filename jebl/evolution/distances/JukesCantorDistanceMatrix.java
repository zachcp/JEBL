package jebl.evolution.distances;

import jebl.evolution.alignments.Alignment;
import jebl.evolution.alignments.Pattern;
import jebl.evolution.sequences.State;

/**
 * Compute jukes-cantor corrected distance matrix for a set of aligned sequences.
 * Adapted from BEAST code by joseph.
 *
 * @author Andrew Rambaut
 * @author Korbinian Strimmer
 * @author Joseph Heled
 *
 * @version $Id$
 */

public class JukesCantorDistanceMatrix extends BasicDistanceMatrix {

    public JukesCantorDistanceMatrix(Alignment alignment) {
        super(alignment.getTaxa(), getDistances(alignment));
    }

    // Helpers during construction
    private static double const1;
    private static double const2;
    private static Alignment alignment;
    private static final double MAX_DISTANCE = 1000.0;

    /**
     * Calculate number of substitution between sequences as a ratio.
     */
    static private double anySubstitutionRatio(int taxon1, int taxon2) {

        double weight, distance;
        double sumDistance = 0.0;
        double sumWeight = 0.0;

        for( Pattern pattern : alignment.getPatterns() ) {

            State state1 = pattern.getState(taxon1);
            State state2 = pattern.getState(taxon2);

            weight = pattern.getWeight();

            if (!state1.isAmbiguous() && !state2.isAmbiguous() && state1 != state2) {
                sumDistance += weight;
            }
            sumWeight += weight;
        }

        distance = sumDistance / sumWeight;

        return distance;
    }


    /**
     * Calculate a pairwise distance between the i'th and j'th taxons/sequences
     */
    static private double calculatePairwiseDistance(int taxon1, int taxon2) {
        double obsDist = anySubstitutionRatio(taxon1, taxon2);

        if (obsDist == 0.0) return 0.0;

        // protect against log(negative number)
        if (obsDist >= const1) {
            return MAX_DISTANCE;
        }

        double expDist = -const1 * Math.log(1.0 - (const2 * obsDist));

        return Math.min(expDist, MAX_DISTANCE);
    }

    synchronized static double[][] getDistances(Alignment alignment) {
        JukesCantorDistanceMatrix.alignment = alignment;

        // ASK Alexei
        int stateCount = alignment.getSequenceType().getCanonicalStateCount(); // getStateCount();

        const1 = ((double)stateCount - 1) / stateCount;
        const2 = 1.0/const1; // ((double)stateCount) / (stateCount - 1) ;

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

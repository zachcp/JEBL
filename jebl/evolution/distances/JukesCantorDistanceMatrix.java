package jebl.evolution.distances;

import jebl.evolution.alignments.Alignment;
import jebl.evolution.alignments.Pattern;
import jebl.evolution.sequences.State;

/**
 * Created by IntelliJ IDEA.
 * User: joseph
 * Date: 10/01/2006
 * Time: 16:02:44
 *
 * @author joseph
 * @version $Id$
 *          To change this template use File | Settings | File Templates.
 */
public class JukesCantorDistanceMatrix extends BasicDistanceMatrix {

    public JukesCantorDistanceMatrix(Alignment alignment) {
        super(alignment.getTaxa(), getDist(alignment));
    }

    // Helpers during construction
    private static double const1;
    private static double const2;
    private static Alignment alignment;
    public static final double MAX_DISTANCE = 1000.0;

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
     * Calculate a pairwise distance
     */
    static private double calculatePairwiseDistance( int i, int j) {
        double obsDist = anySubstitutionRatio(i, j);

        if (obsDist == 0.0) return 0.0;

        if (obsDist >= const1) {
            return MAX_DISTANCE;
        }

        double expDist = -const1 * Math.log(1.0 - (const2 * obsDist));

        if (expDist < MAX_DISTANCE) {
            return expDist;
        } else {
            return MAX_DISTANCE;
        }
    }

    static double[][] getDist(Alignment alignment) {
        JukesCantorDistanceMatrix.alignment = alignment;

        int stateCount = alignment.getSequenceType().getStateCount();

        const1 = ((double)stateCount - 1) / stateCount;
        const2 = ((double)stateCount) / (stateCount - 1) ;

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

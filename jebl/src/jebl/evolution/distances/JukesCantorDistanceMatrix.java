package jebl.evolution.distances;

import jebl.evolution.alignments.Alignment;
import jebl.evolution.alignments.Pattern;
import jebl.evolution.sequences.State;
import jebl.util.ProgressListener;

/**
 * Compute jukes-cantor corrected distance matrix for a set of aligned sequences.
 * Adapted from BEAST code by joseph.
 *
 * @author Andrew Rambaut
 * @author Korbinian Strimmer
 * @author Joseph Heled
 *
 */

public class JukesCantorDistanceMatrix extends BasicDistanceMatrix {

    public JukesCantorDistanceMatrix(Alignment alignment, ProgressListener progress) throws CannotBuildDistanceMatrixException {
        this(alignment,progress,false);    
    }
    
    public JukesCantorDistanceMatrix(Alignment alignment, ProgressListener progress, boolean useTwiceMaximumDistanceWhenPairwiseDistanceNotCalculatable)
            throws CannotBuildDistanceMatrixException
    {
        super(alignment.getTaxa(), new Initializer().getDistances(alignment, progress,useTwiceMaximumDistanceWhenPairwiseDistanceNotCalculatable));
    }

    private static class Initializer implements PairwiseDistanceCalculator {
        // Helpers during construction
        private double maxTheoreticalSubsRate;
        private Alignment alignment;
        private static final double MAX_DISTANCE = 1000.0;

        /**
         * Calculate number of substitution between sequences as a ratio.
         * @throws CannotBuildDistanceMatrixException
         */
        private double anySubstitutionRatio(int taxon1, int taxon2) throws CannotBuildDistanceMatrixException {
            double distance;
            double sumDistance = 0.0;
            double sumWeight = 0.0;
            boolean noGapsPairFound = false;

            // If both sequences are of zero length then the substitution ratio is zero because they are identical
            if(alignment.getPatterns().size() == 0)
                return 0.0;

            for( Pattern pattern : alignment.getPatterns() ) {
                State state1 = pattern.getState(taxon1);
                State state2 = pattern.getState(taxon2);

                final double weight = pattern.getWeight();


                // ignore any ambiguous states or gaps
                if( state1.isAmbiguous() || state2.isAmbiguous() || state1.isGap() || state2.isGap()) {
                    continue;
                }
                noGapsPairFound = true;

                if(state1 != state2)
                    sumDistance += weight;
                sumWeight += weight;
            }

            if(!noGapsPairFound)
                throw new CannotBuildDistanceMatrixException("Jukes-Cantor", getTaxonName(taxon1), getTaxonName(taxon2));

            distance = sumDistance / sumWeight;

            return distance;
        }


        /**
         * Calculate a pairwise distance between the i'th and j'th taxons/sequences
         * @throws CannotBuildDistanceMatrixException
         */
        public double calculatePairwiseDistance(int taxon1, int taxon2) throws CannotBuildDistanceMatrixException {
            double obsDist = anySubstitutionRatio(taxon1, taxon2);

            if (obsDist == 0.0) return 0.0;

            // protect against log(negative number)
            if (obsDist >= maxTheoreticalSubsRate) {
                return MAX_DISTANCE;
            }

            double expDist = -maxTheoreticalSubsRate * Math.log(1.0 - ((1/maxTheoreticalSubsRate) * obsDist));

            return Math.min(expDist, MAX_DISTANCE);
        }

        synchronized double[][] getDistances(Alignment alignment, ProgressListener progress, boolean useTwiceMaximumDistanceWhenPairwiseDistanceNotCalculatable)
                throws CannotBuildDistanceMatrixException
        {
            this.alignment = alignment;

            // ASK Alexei
            int stateCount = alignment.getSequenceType().getCanonicalStateCount();

            maxTheoreticalSubsRate = ((double)stateCount - 1) / stateCount;

            int dimension = alignment.getTaxa().size();
            return BasicDistanceMatrix.buildDistancesMatrix(this,dimension,useTwiceMaximumDistanceWhenPairwiseDistanceNotCalculatable,progress);
        }

        private String getTaxonName(int index) {
            return alignment.getSequenceList().get(index).getTaxon().getName();
        }
    }
}
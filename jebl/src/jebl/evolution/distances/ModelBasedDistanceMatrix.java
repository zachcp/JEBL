package jebl.evolution.distances;

import jebl.evolution.taxa.Taxon;

import java.util.Collection;

/**
 *
 * @author Joseph Heled
 * @version $Id$
 *
 */
public abstract class ModelBasedDistanceMatrix extends BasicDistanceMatrix {
    protected static final double MAX_DISTANCE = 1000.0;

    public ModelBasedDistanceMatrix(Collection<Taxon> taxa, double[][] distances) {
        super(taxa, distances);
    }
  /*
    static double[][] buildDistances(int dimension) {
        double[][] distances = new double[dimension][dimension];

        for(int i = 0; i < dimension; ++i) {
            for(int j = i+1; j < dimension; ++j) {
                distances[i][j] = calculatePairwiseDistance(i, j);
                distances[j][i] = distances[i][j];
            }
        }

        return distances;
    }
    */
    //static abstract double calculatePairwiseDistance(int i, int j);
}

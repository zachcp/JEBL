package jebl.evolution.io;

import jebl.evolution.distances.DistanceMatrix;

import java.util.List;
import java.io.IOException;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id$
 */
public interface DistanceMatrixImporter {

    enum Triangle { LOWER, UPPER, BOTH };

    /**
     * importDistances.
     */
    List<DistanceMatrix> importDistanceMatrices() throws IOException, ImportException;
}

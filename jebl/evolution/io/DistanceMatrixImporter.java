package jebl.evolution.io;

import jebl.evolution.distances.DistanceMatrix;

import java.io.IOException;
import java.util.List;

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

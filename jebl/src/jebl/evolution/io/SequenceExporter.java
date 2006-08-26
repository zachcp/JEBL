package jebl.evolution.io;

import jebl.evolution.sequences.Sequence;

import java.io.IOException;
import java.util.List;
import java.util.Collection;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 *
 * @version $Id$
 */
public interface SequenceExporter {

    /**
     * exportSequences.
     */
    void exportSequences(Collection<? extends Sequence> sequences) throws IOException;
}

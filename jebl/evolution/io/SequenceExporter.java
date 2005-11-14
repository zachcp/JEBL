package jebl.evolution.io;

import jebl.evolution.alignments.Alignment;
import jebl.evolution.sequences.Sequence;

import java.io.IOException;
import java.util.List;

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
	void exportSequences(List<Sequence> sequences) throws IOException;
}

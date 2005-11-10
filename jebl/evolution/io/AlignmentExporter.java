package jebl.evolution.io;

import jebl.evolution.alignments.Alignment;

import java.io.IOException;
import java.util.List;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 *
 * @version $Id$
 */
public interface AlignmentExporter {

	/**
	 * exportAlignment.
	 */
	void exportAlignment(Alignment alignment) throws IOException;
}

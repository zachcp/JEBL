package jebl.evolution.io;

import jebl.evolution.alignments.Alignment;

import java.io.IOException;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 *
 * @version $Id$
 */
public interface AlignmentExporter {

	/**
	 * export one alignment.
     * @param alignment  to export
     * @throws java.io.IOException
     */
	void exportAlignment(Alignment alignment) throws IOException;
}

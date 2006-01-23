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
public interface AlignmentImporter {

	/**
	 * importAlignment.
	 */
	List<Alignment> importAlignments() throws IOException, ImportException;
}

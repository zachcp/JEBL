/*
 * SequenceImporter.java
 *
 * (c) 2002-2005 BEAST Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */

package jebl.evolution.io;

import jebl.evolution.sequences.Sequence;

import java.io.IOException;
import java.util.List;

/**
 * Interface for importers that do sequences
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 *
 */
public interface SequenceImporter {

	/**
	 * importSequences.
	 */
	List<Sequence> importSequences() throws IOException, ImportException;
}

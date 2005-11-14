/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */

package jebl.evolution.io;

import jebl.evolution.sequences.*;
import jebl.evolution.taxa.Taxon;

import java.io.*;
import java.util.*;

/**
 * Class for importing PHYLIP sequential file format
 *
 * @version $Id$
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 */
public class FastaExporter implements SequenceExporter {

	/**
	 * Constructor
	 */
	public FastaExporter(Writer writer) {
        this.writer = new PrintWriter(writer);
    }

	/**
	 * importAlignment.
	 */
	public void exportSequences(List<Sequence> sequences) throws IOException {

        for (Sequence sequence : sequences) {
            writer.println(">" + sequence.getTaxon().getName());
            writer.println(sequence.getString());
        }
    }

	private final PrintWriter writer;
}

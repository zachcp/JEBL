/*
 * Copyright (c) 2005 JEBL Development team. All Rights Reserved.
 */

package jebl.evolution.io;

import jebl.evolution.sequences.Sequence;
import jebl.evolution.taxa.Taxon;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;

/**
 * Class for exporting a fasta file format.
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
	 * export alignment.
	 */
	public void exportSequences(List<Sequence> sequences) throws IOException {

        for (Sequence sequence : sequences) {
            Taxon taxon = sequence.getTaxon();
            String desc = (String)taxon.getAttribute(FastaImporter.descriptionPropertyName);
            writer.println(">" + taxon.getName() + ((desc != null) ? (" " + desc) : ""));
            writer.println(sequence.getString());
        }
    }

	private final PrintWriter writer;
}

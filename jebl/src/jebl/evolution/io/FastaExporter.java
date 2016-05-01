/*
 * Copyright (c) 2005 JEBL Development team. All Rights Reserved.
 */

package jebl.evolution.io;

import jebl.evolution.sequences.Sequence;
import jebl.evolution.taxa.Taxon;
import jebl.util.SafePrintWriter;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

/**
 * Class for exporting a fasta file format.
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 */
public class FastaExporter implements SequenceExporter {

    /**
     * Constructor
     */
    public FastaExporter(Writer writer) {
        this.writer = new SafePrintWriter(writer);
    }

    /**
     * export alignment or set of sequences.
     */
    public void exportSequences(Collection<? extends Sequence> sequences) throws IOException {
        for (Sequence sequence : sequences) {
            final Taxon taxon = sequence.getTaxon();
            String desc = (String) sequence.getAttribute(FastaImporter.descriptionPropertyName);
            if(desc== null) desc = (String) taxon.getAttribute(FastaImporter.descriptionPropertyName);
            writer.println(">" + taxon.getName().replace(' ','_') + ((desc != null) ? (" " + desc) : ""));
            writer.println(sequence.getString());
        }
    }

    private final SafePrintWriter writer;
}

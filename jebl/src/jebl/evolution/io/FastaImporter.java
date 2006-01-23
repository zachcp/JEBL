/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */

package jebl.evolution.io;

import jebl.evolution.sequences.BasicSequence;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.sequences.SequenceType;
import jebl.evolution.taxa.Taxon;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Class for importing Fasta sequential file format
 *
 * @version $Id$
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 */
public class FastaImporter implements SequenceImporter {

     public static final String descriptionPropertyName = "description";

    /**
	 * Constructor
	 */
	public FastaImporter(Reader reader, SequenceType sequenceType) {
        helper = new ImportHelper(reader);
        helper.setCommentDelimiters(';');
        this.sequenceType = sequenceType;
	}

	/**
	 * importAlignment.
	 */
	public 	List<Sequence> importSequences() throws IOException, ImportException {

		List<Sequence> sequences = new ArrayList<Sequence>();

		try {

            int ch = helper.read();
            while (ch != '>') {
                ch = helper.read();
            }

    		do {
                String line = helper.readLine();

                StringTokenizer tokenizer = new StringTokenizer(line, " \t");
                String name = tokenizer.nextToken();

                String description = tokenizer.hasMoreElements() ? tokenizer.nextToken("") : null;

                StringBuffer seq = new StringBuffer();
				helper.readSequence(seq, sequenceType, ">", Integer.MAX_VALUE, "-", "?", "", null);
				ch = helper.getLastDelimiter();

                Taxon taxon = Taxon.getTaxon(name);
                if( description != null && description.length() > 0 ) {
                   taxon.setAttribute(descriptionPropertyName, description);
                }

                sequences.add(new BasicSequence(sequenceType, taxon, seq.toString()));

			} while(ch == '>');


		} catch (EOFException e) { }

		return sequences;
	}

	private final ImportHelper helper;
    private final SequenceType sequenceType;
}

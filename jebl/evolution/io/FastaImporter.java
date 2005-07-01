/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */

package jebl.evolution.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;

import jebl.evolution.sequences.Sequence;
import jebl.evolution.sequences.SequenceType;
import jebl.evolution.sequences.BasicSequence;
import jebl.evolution.taxa.Taxon;

/**
 * Class for importing PHYLIP sequential file format
 *
 * @version $Id$
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 */
public class FastaImporter implements SequenceImporter {

	/**
	 * Constructor
	 */
	public FastaImporter(Reader reader, SequenceType sequenceType) {
        helper = new ImportHelper(reader);
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

				StringBuffer seq = new StringBuffer();
				helper.readSequence(seq, sequenceType, ">", Integer.MAX_VALUE, "-", "?", "", null);
				ch = helper.getLastDelimiter();

				sequences.add(new BasicSequence(Taxon.getTaxon(name), sequenceType, seq.toString()));

			} while(ch == '>');
			
			
		} catch (EOFException e) { }	

		return sequences;
	}

	private final ImportHelper helper;
    private final SequenceType sequenceType;
}

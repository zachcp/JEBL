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

import jebl.evolution.io.ImportHelper;
import jebl.evolution.io.SequenceImporter;
import jebl.evolution.sequences.SequenceType;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.sequences.BasicSequence;
import jebl.evolution.alignments.Alignment;
import jebl.evolution.taxa.Taxon;

/**
 * Class for importing PHYLIP sequential file format
 *
 * @version $Id$
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 */
public class PhylipSequentialImporter implements SequenceImporter {

	/**
	 * Constructor
	 */
	public PhylipSequentialImporter(Reader reader, SequenceType sequenceType, int maxNameLength) {
        helper = new ImportHelper(reader);

		this.sequenceType = sequenceType;
		this.maxNameLength = maxNameLength;
	}

	/**
	 * importSequences. 
	 */
	public List<Sequence> importSequences() throws IOException, ImportException {

        List<Sequence> sequences = new ArrayList<Sequence>();

        try {

            int taxonCount = helper.readInteger();
            int siteCount = helper.readInteger();

            String firstSeq = null;

            for (int i = 0; i < taxonCount; i++) {
                StringBuffer name = new StringBuffer();

                char ch = helper.read();
                int n = 0;
                while (!Character.isWhitespace(ch) && (maxNameLength < 1 || n < maxNameLength)) {
                    name.append(ch);
                    ch = helper.read();
                    n++;
                }

                StringBuffer seq = new StringBuffer(siteCount);
                helper.readSequence(seq, sequenceType, "", siteCount, "-", "?", ".", firstSeq);

                if (firstSeq == null) { firstSeq = seq.toString(); }

                sequences.add(new BasicSequence(Taxon.getTaxon(name.toString()), sequenceType, seq.toString()));
            }


        } catch (EOFException e) { }

        return sequences;
	}

    private final ImportHelper helper;
    private final SequenceType sequenceType;
	private int maxNameLength = 10;
}

/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */

package jebl.evolution.io;

import jebl.evolution.sequences.BasicSequence;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.sequences.SequenceType;
import jebl.evolution.sequences.Utils;
import jebl.evolution.taxa.Taxon;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * Class for importing Fasta sequential file format.
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @author Joseph Heled
 * @version $Id$
 */
public class FastaImporter implements SequenceImporter {

    /**
     * Name of Jebl sequence property which stores sequence description (i.e. anything after sequence name in fasta
     * file), so this data is available and an export to fasta can preserves the original data.
     * This is stored some attribute of the sequence  and of the taxon for backwards compatibility.
     * Generally, attributes on taxon should not be used, as they are unsafe
     * when dealing with objects that share the same taxon.
     */
    public static final String descriptionPropertyName = "description";


    private final ImportHelper helper;
    private final SequenceType sequenceType;

    /**
     * @param reader       holds sequences data
     * @param sequenceType pre specified sequences type. We should try and guess hem some day.
     */
    public FastaImporter(Reader reader, SequenceType sequenceType) {
        helper = new ImportHelper(reader);
        helper.setCommentDelimiters(';');
        this.sequenceType = sequenceType;
    }

    /**
     * @return sequences from file.
     * @throws IOException
     * @throws ImportException
     */
    public final List<Sequence> importSequences() throws IOException, ImportException {

        List<Sequence> sequences = new ArrayList<Sequence>();
        final char fastaFirstChar = '>';
        final String fasta1stCharAsString = new String(new char[]{fastaFirstChar});
        final SequenceType seqtypeForGapsAndMissing = sequenceType != null ? sequenceType : SequenceType.NUCLEOTIDE;

        try {
            // find fasta line start
            while (helper.read() != fastaFirstChar) {
            }

            do {
                final String line = helper.readLine();

                final StringTokenizer tokenizer = new StringTokenizer(line, " \t");
                final String name = tokenizer.nextToken().replace('_', ' ');

                final String description = tokenizer.hasMoreElements() ? tokenizer.nextToken("") : null;

                final StringBuffer seq = new StringBuffer();

                helper.readSequence(seq, seqtypeForGapsAndMissing, fasta1stCharAsString, Integer.MAX_VALUE, "-", "?", "", null);

                final Taxon taxon = Taxon.getTaxon(name);
                if (description != null && description.length() > 0) {
                    taxon.setAttribute(descriptionPropertyName, description);
                }

                final String sequenceString = seq.toString();
                SequenceType type = ( sequenceType != null ) ? sequenceType : Utils.guessSequenceType(sequenceString);

                if( type == null ) {
                    throw new ImportException("Sequence contains illegal characters (near line " + helper.getLineNumber() + ")");
                }
                BasicSequence sequence = new BasicSequence(type, taxon, sequenceString);
                if (description != null && description.length() > 0) {
                    sequence.setAttribute(descriptionPropertyName, description);
                }
                sequences.add(sequence);
            } while (helper.getLastDelimiter() == fastaFirstChar);

        } catch (EOFException e) {
            // catch end of file the ugly way.
        } catch (NoSuchElementException e) {
            throw new ImportException("Incorrectly formatted fasta file (near line " + helper.getLineNumber() + ")");
        }

        return sequences;
    }
}

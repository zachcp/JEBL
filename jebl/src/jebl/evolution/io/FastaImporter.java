/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */

package jebl.evolution.io;

import jebl.evolution.sequences.BasicSequence;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.sequences.SequenceType;
import jebl.evolution.sequences.Utils;
import jebl.evolution.taxa.Taxon;
import jebl.util.ProgressListener;

import java.io.*;
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
public class FastaImporter implements SequenceImporter, ImmediateSequenceImporter {

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
    private final boolean closeReaderAtEnd;

    private final Reader reader;

    /**
     * Use this constructor if you are reading from a file. The advantage over the
     * other constructor is that a) the input size is known, so read() can report
     * meaningful progress, and b) the file is closed at the end.
     * @param file
     * @param sequenceType
     * @throws FileNotFoundException
     */
    public FastaImporter(File file, SequenceType sequenceType) throws FileNotFoundException {
        this(new BufferedReader(new FileReader(file)), sequenceType, true);
        helper.setExpectedInputLength(file.length());
    }

    /**
     * This constructor should normally never be needed because usually we
     * want to import from a file. Then, the constructor expecting a file
     * should be used. Therefore, this constructor is deprecated for now.
     * @param reader       holds sequences data
     * @param sequenceType pre specified sequences type. We should try and guess them some day.
     */
    @Deprecated
    public FastaImporter(Reader reader, SequenceType sequenceType) {
        this(reader, sequenceType, false);
    }

    private FastaImporter(final Reader reader, final SequenceType sequenceType, final boolean closeReaderAtEnd) {
        this.reader = reader;
        this.sequenceType = sequenceType;
        this.closeReaderAtEnd = closeReaderAtEnd;
        helper = new ImportHelper(reader);
        helper.setCommentDelimiters(';');
    }

    /**
     * @return sequences from file.
     * @throws IOException
     * @throws ImportException
     */
    private List<Sequence> read(ImmediateSequenceImporter.Callback callback, ProgressListener progressListener)
            throws IOException, ImportException
    {
        List<Sequence> sequences = callback != null ? new ArrayList<Sequence>() : null;
        final char fastaFirstChar = '>';
        final String fasta1stCharAsString = new String(new char[]{fastaFirstChar});
        final SequenceType seqtypeForGapsAndMissing = sequenceType != null ? sequenceType : SequenceType.NUCLEOTIDE;

        try {
            // find fasta line start
            while (helper.read() != fastaFirstChar) {
            }

            boolean importAborted;
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
                if( callback != null ) {
                    callback.add(sequence);
                } else {
                    sequences.add(sequence);
                }
                // helper.getProgress currently assumes each character to be
                // one byte long, but this should be ok for fasta files.
                importAborted = progressListener.setProgress(helper.getProgress());
            } while (!importAborted && (helper.getLastDelimiter() == fastaFirstChar));
        } catch (EOFException e) {
            // catch end of file the ugly way.
        } catch (NoSuchElementException e) {
            throw new ImportException("Incorrectly formatted fasta file (near line " + helper.getLineNumber() + ")");
        }
        if (closeReaderAtEnd) {
            reader.close();
        }
        return sequences;
    }


    /**
     * @return sequences from file.
     * @throws IOException
     * @throws ImportException
     */
    public final List<Sequence> importSequences() throws IOException, ImportException {
        return read(null, ProgressListener.empty());
    }

    public void importSequences(Callback callback, ProgressListener progressListener) throws IOException, ImportException {
        read(callback, progressListener);
    }
}

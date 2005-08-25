// Implementation of some algorithms for pairwise alignment from
// Durbin et al: Biological Sequence Analysis, CUP 1998, chapter 2.
// Peter Sestoft, sestoft@dina.kvl.dk 1999-09-25, 2003-04-20 version 1.4
// Reference:  http://www.dina.kvl.dk/~sestoft/bsa.html

// License: Anybody can use this code for any purpose, including
// teaching, research, and commercial purposes, provided proper
// reference is made to its origin.  Neither the author nor the Royal
// Veterinary and Agricultural University, Copenhagen, Denmark, can
// take any responsibility for the consequences of using this code.

package jebl.evolution.aligners.pairwise;

import jebl.evolution.aligners.scores.*;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.sequences.SequenceType;
import jebl.evolution.sequences.BasicSequence;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.alignments.Alignment;
import jebl.evolution.alignments.BasicAlignment;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;

abstract class AbstractAligner implements PairwiseAligner {

    private final Scores scores;    // scores matrix
    Sequence sequence1 = null;
    Sequence sequence2 = null;      // the sequences
    Traceback B0;                	// the starting point of the traceback

    AbstractAligner(Scores scores) {
        this.scores = scores;
    }

    /**
     * Aligns a collection of sequences.
     *
     * @param sequences
     */
    public Alignment alignSequences(Collection<Sequence> sequences) {
        if (sequences.size() != 2) {
            throw new IllegalArgumentException("Pairwise aligners can only align 2 sequences");
        }

        Iterator<Sequence> iter = sequences.iterator();
        this.sequence1 = iter.next();
        this.sequence2 = iter.next();

        return alignSequences(sequence1, sequence2);
    }

    /**
     * Aligns a pair of sequences.
     *
     * @param sequence1
     * @param sequence2
     */
    public Alignment alignSequences(Sequence sequence1, Sequence sequence2) {
        this.sequence1 = sequence1;
        this.sequence2 = sequence2;

        prepareAlignment(sequence1, sequence2);
        doAlignment(sequence1, sequence2);

        String[] seqStrings = getOptimalAlignmentStrings();
        BasicAlignment alignment = new BasicAlignment();
        alignment.addSequence(new BasicSequence(sequence1.getSequenceType(), sequence1.getTaxon(), seqStrings[0]));
        alignment.addSequence(new BasicSequence(sequence2.getSequenceType(), sequence2.getTaxon(), seqStrings[1]));
        return alignment;
    }

    /**
     * Performs the alignment, abstract.
     *
     * @param sequence1
     * @param sequence2
     */
    protected abstract void doAlignment(Sequence sequence1, Sequence sequence2);

    /**
     * Initialises the matrices for the alignment.
     *
     * @param sequence1
     * @param sequence2
     */
    protected abstract void prepareAlignment(Sequence sequence1, Sequence sequence2);

    public Scores getScores() {
        return scores;
    }

    /**
     * @return two-string array containing an alignment with maximal score
     */
    public String[] getOptimalAlignmentStrings() {
        StringBuffer res1 = new StringBuffer();
        StringBuffer res2 = new StringBuffer();
        Traceback tb = B0;

        int i = tb.i, j = tb.j;
        while ((tb = next(tb)) != null) {
            if (i == tb.i) {
                res1.append('-');
            } else {
                res1.append(sequence1.getState(i-1).getCode());
            }
            if (j == tb.j) {
                res2.append('-');
            } else {
                res2.append(sequence2.getState(j-1).getCode());
            }
            i = tb.i; j = tb.j;
        }
        return new String[]{ res1.reverse().toString(), res2.reverse().toString() };
    }

    /**
     *
     * @param val
     * @return float value of string val
     */
    public String formatScore(float val) {
        return Float.toString(val);
    }

    /**
     * Print the score, the F matrix, and the alignment
     *
     * @param out output to print to
     * @param msg message printed at start
     * @param outputFMatrix print the score matrix
     */
    public void print(PrintStream out, String msg, boolean outputFMatrix) {
        out.println(msg + ":");
        out.println("Score = " + getScore());
        if (outputFMatrix) {
            out.println("The F matrix:");
            printMatrix(out);
        }
        out.println("An optimal alignment:");
        String[] match = getOptimalAlignmentStrings();
        out.println(match[0]);
        out.println(match[1]);
        out.println();
    }

    /**
     * Print the score and the alignment
     *
     * @param out output to print to
     * @param msg msg printed at the start
     */
    public void print(PrintStream out, String msg) { print(out, msg, false); }

    /**
     * Get the next state in the traceback
     *
     * @param tb current Traceback
     * @return next Traceback
     */
    public Traceback next(Traceback tb) { return tb; } // dummy implementation for the `smart' algs.

    /**
     * @return the score of the best alignment
     */
    public abstract float getScore();

    /**
     * Print the matrix (matrices) used to compute the alignment
     *
     * @param out output to print to
     */
    public abstract void printMatrix(PrintStream out);

    // auxillary static functions

    static float max(float x1, float x2) { return (x1 > x2 ? x1 : x2); }

    static float max(float x1, float x2, float x3) { return max(x1, max(x2, x3)); }

    static float max(float x1, float x2, float x3, float x4) { return max(max(x1, x2), max(x3, x4)); }

    /**
     *
     * @param s string to pad
     * @param width width to pad to
     * @return string padded to specified width with space chars.
     */
    static String padLeft(String s, int width) {
        int filler = width - s.length();
        if (filler > 0) {           // and therefore width > 0
            StringBuffer res = new StringBuffer(width);
            for (int i=0; i<filler; i++)
                res.append(' ');
            return res.append(s).toString();
        } else
            return s;
    }

    public static void main(String[] args) {

        Sequence seq1 = new BasicSequence(SequenceType.AMINO_ACID, Taxon.getTaxon("sequence1"),
                "PLQMKKTAFTLLLFIALTLTTSPLVNGSEKSEEINEKDLRKKSELQGTALGNLKQIYYYNEKAKTENKES" +
                        "HDQFLQHTILFKGFFTDHSWYNDLLVDFDSKDIVDKYKGKKVDLYGAYYGYQCAGGTPNKTACMYGGVTL" +
                        "HDNNRLTEEKKVPINLWLDGKQNTVPLETVKTNKKNVTVQELDLQARRYLQEKYNLYNSDVFDGKVQRGL" +
                        "IVFHTSTEPSVNYDLFGAQGQYSNTLLRIYRDNKTISSENMHIDIYLYTSY");

        Sequence seq2 = new BasicSequence(SequenceType.AMINO_ACID, Taxon.getTaxon("sequence2"),
                "MYKRLFISHVILIFALILVISTPNVLAESQPDPKPDELHKSSKFTGLMENMKVLYDDNHVSAINVKSIDQ" +
                        "FLYFDLIYSIKDTKLGNYDNVRVEFKNKDLADKYKDKYVDVFGANYYYQCYFSKKTNDINSHQTDKRKTC" +
                        "MYGGVTEHNGNQLDKYRSITVRVFEDGKNLLSFDVQTNKKKVTAQELDYLTRHYLVKNKKLYEFNNSPYE" +
                        "TGYIKFIENENSFWYDMMPAPGDKFDQSKYLMMYNDNKMVDSKDVKIEVYLTTKKK");
        int reps = 500;

        NeedlemanWunschAligner nw = new NeedlemanWunschAligner(new Blosum45(), 10);
        long start = System.currentTimeMillis();
        for (int i = 0; i < reps; i++) {
            nw.alignSequences(seq1, seq2);
        }
        nw.print(System.out, "NeedlemanWunsch");
        System.out.println((System.currentTimeMillis() - start));

        SmithWatermanAligner sw = new SmithWatermanAligner(new Blosum45(), 10);
        start = System.currentTimeMillis();
        for (int i = 0; i < reps; i++) {
            sw.alignSequences(seq1, seq2);
        }
        nw.print(System.out, "SmithWaterman");
        System.out.println((System.currentTimeMillis() - start));

    }
}
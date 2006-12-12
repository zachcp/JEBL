package jebl;

import jebl.evolution.align.NeedlemanWunsch;
import jebl.evolution.align.scores.Blosum45;
import jebl.evolution.aligners.pairwise.NeedlemanWunschAligner;
import jebl.evolution.sequences.BasicSequence;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.sequences.SequenceType;
import jebl.evolution.taxa.Taxon;

/**
 * @author Alexei Drummond
 *
 * @version $Id$
 */
public class TestAligners {

    /*
    Performance characteristics (ms, 1.8 GHz PowerPC G5 iMac with 1Gb RAM, reps=500)

    NeedlemanWunschAligner	5684
    NeedlemanWunsch	2994
    NeedlemanWunschAligner	5507
    NeedlemanWunsch	2927
    NeedlemanWunschAffine	8996
    NeedlemanWunschLinearSpace	2767
    SmithWaterman	4369
    SmithWatermanLinearSpaceAffine	6913

    */

    public static void main(String[] args) {

        String seq1 = "PLQMKKTAFTLLLFIALTLTTSPLVNGSEKSEEINEKDLRKKSELQGTALGNLKQIYYYNEKAKTENKES" +
                    "HDQFLQHTILFKGFFTDHSWYNDLLVDFDSKDIVDKYKGKKVDLYGAYYGYQCAGGTPNKTACMYGGVTL" +
                    "HDNNRLTEEKKVPINLWLDGKQNTVPLETVKTNKKNVTVQELDLQARRYLQEKYNLYNSDVFDGKVQRGL" +
                    "IVFHTSTEPSVNYDLFGAQGQYSNTLLRIYRDNKTISSENMHIDIYLYTSY";

        String seq2 = "MYKRLFISHVILIFALILVISTPNVLAESQPDPKPDELHKSSKFTGLMENMKVLYDDNHVSAINVKSIDQ" +
                    "FLYFDLIYSIKDTKLGNYDNVRVEFKNKDLADKYKDKYVDVFGANYYYQCYFSKKTNDINSHQTDKRKTC" +
                    "MYGGVTEHNGNQLDKYRSITVRVFEDGKNLLSFDVQTNKKKVTAQELDYLTRHYLVKNKKLYEFNNSPYE" +
                    "TGYIKFIENENSFWYDMMPAPGDKFDQSKYLMMYNDNKMVDSKDVKIEVYLTTKKK";

        Sequence sequence1 = new BasicSequence(SequenceType.AMINO_ACID, Taxon.getTaxon("A"),seq1);
        Sequence sequence2 = new BasicSequence(SequenceType.AMINO_ACID, Taxon.getTaxon("B"),seq2);

        int reps = 500;

        NeedlemanWunschAligner nwan = new NeedlemanWunschAligner(new jebl.evolution.aligners.scores.Blosum45(), 10);
        long start = System.currentTimeMillis();
        for (int i = 0; i < reps; i++) {
            nwan.alignSequences(sequence1, sequence2);
        }
        System.out.print("NeedlemanWunschAligner\t");
        System.out.println((System.currentTimeMillis() - start));

        NeedlemanWunsch nw = new NeedlemanWunsch(new Blosum45(), 10);
        start = System.currentTimeMillis();
        for (int i = 0; i < reps; i++) {
            nw.doAlignment(seq1, seq2);
            nw.getMatch();
        }
        System.out.print("NeedlemanWunsch\t");
        System.out.println((System.currentTimeMillis() - start));

/*
        NeedlemanWunschAligner nwan2 = new NeedlemanWunschAligner(new jebl.evolution.aligners.scores.Blosum45(), 10);
        start = System.currentTimeMillis();
        for (int i = 0; i < reps; i++) {
            nwan2.alignSequences(sequence1, sequence2);
        }
        System.out.print("NeedlemanWunschAligner\t");
        System.out.println((System.currentTimeMillis() - start));

        NeedlemanWunsch nw2 = new NeedlemanWunsch(new Blosum45(), 10);
        start = System.currentTimeMillis();
        for (int i = 0; i < reps; i++) {
            nw2.doAlignment(seq1, seq2);
        }
        System.out.print("NeedlemanWunsch\t");
        System.out.println((System.currentTimeMillis() - start));

        NeedlemanWunschAffine nwa = new NeedlemanWunschAffine(new Blosum45(), 10, 4);
        start = System.currentTimeMillis();
        for (int i = 0; i < reps; i++) {
            nwa.doAlignment(seq1, seq2);
        }
        System.out.print("NeedlemanWunschAffine\t");
        System.out.println((System.currentTimeMillis() - start));

        NeedlemanWunschLinearSpace nwls = new NeedlemanWunschLinearSpace(new Blosum45(), 10);
        start = System.currentTimeMillis();
        for (int i = 0; i < reps; i++) {
            nwls.doAlignment(seq1, seq2);
        }
        System.out.print("NeedlemanWunschLinearSpace\t");
        System.out.println((System.currentTimeMillis() - start));


        SmithWaterman sw = new SmithWaterman(new Blosum45(), 10);
        start = System.currentTimeMillis();
        for (int i = 0; i < reps; i++) {
            sw.doAlignment(seq1, seq2);
        }
        System.out.print("SmithWaterman\t");
        System.out.println((System.currentTimeMillis() - start));

        SmithWatermanLinearSpaceAffine swlsa = new SmithWatermanLinearSpaceAffine(new Blosum45(), 10, 4);
        start = System.currentTimeMillis();
        for (int i = 0; i < reps; i++) {
            swlsa.doAlignment(seq1, seq2);
        }
        System.out.print("SmithWatermanLinearSpaceAffine\t");
        System.out.println((System.currentTimeMillis() - start));
*/

    }
}

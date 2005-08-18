package jebl.evolution.align;

import jebl.evolution.align.scores.Scores;
import jebl.evolution.align.scores.Blosum45;

abstract class Align {

    Scores sub;             		// scores matrix
    float d;          				// gap cost
    String seq1 = null;
    String seq2 = null;           	// the sequences
    int n = 0; int m = 0;           // their lengths
    Traceback B0;                	// the starting point of the traceback

    public Align(Scores sub, float d) {
        setGapOpen(d);
        setScores(sub);
    }

    /**
     * Performs the alignment, abstract.
     * 
     * @param seq1
     * @param seq2
     */
    public abstract void doAlignment(String seq1, String seq2);

    /**
     * Initialises the matrices for the alignment.
     * 
     * @param seq1
     * @param seq2
     */
    public abstract void prepareAlignment(String seq1, String seq2);

    public void setGapOpen(float d) {
        this.d = d;
    }

    public void setScores(Scores sub) {
        this.sub = sub;
    }

    /**
     * @return two-element array containing an alignment with maximal score
     */
    public String[] getMatch() {
        StringBuffer res1 = new StringBuffer();
        StringBuffer res2 = new StringBuffer();
        Traceback tb = B0;

        int i = tb.i, j = tb.j;
        while ((tb = next(tb)) != null) {
            if (i == tb.i) {
                res1.append('-');
            } else {
                res1.append(seq1.charAt(i-1));
            }
            if (j == tb.j) {
                res2.append('-');
            } else {
                res2.append(seq2.charAt(j-1));
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
    public void doMatch(Output out, String msg, boolean outputFMatrix) {
        out.println(msg + ":");
        out.println("Score = " + getScore());
        if (outputFMatrix) {
          out.println("The F matrix:");
          printf(out);
        }
        out.println("An optimal alignment:");
        String[] match = getMatch();
        out.println(match[0]);
        out.println(match[1]);

        int[] counts = matchCounts(match);

        out.println("matchs=" + counts[0] + " mismatchs=" + counts[1] + " gaps=" + counts[2]);
        out.println("percent identity=" + Math.round((double)counts[0]*1000 / match[0].length())/10.0 + "%");

    }

    private int[] matchCounts(String[] match) {
        int[] matchCounts = new int[3];
        for (int i = 0; i < match[0].length(); i++) {
            char c1 = match[0].charAt(i);
            char c2 = match[1].charAt(i);

            if (c1 == c2) {
                matchCounts[0] += 1;
            } else if (c1 != '-' && c2 != '-') {
                matchCounts[1] += 1;
            } else {
                matchCounts[2] += 1;
            }
        }
        return matchCounts;
    }

    public void traceback(TracebackPlotter plotter) {

        plotter.newTraceBack(seq1, seq2);

        Traceback tb = B0;
        while (tb != null) {
            plotter.traceBack(tb);
            tb = next(tb);
        }
        plotter.finishedTraceBack();
    }

    /**
     * Print the score and the alignment
     * 
     * @param out output to print to
     * @param msg msg printed at the start
     */
    public void doMatch(Output out, String msg) { doMatch(out, msg, false); }

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
    public abstract void printf(Output out);

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

    // PRIVATE METHODS

    /**
     * Strips the given string of all characters that are not recognized sequence states.
     * 
     * @param s
     * @return the stripped string
     */
    String strip(String s) {

        boolean[] valid = new boolean[127];
        String residues = sub.getStates();
        for (int i=0; i<residues.length(); i++) {
            char c = residues.charAt(i);
            if (c < 96) {
                valid[c] = valid[c+32] = true;
            } else {
                valid[c-32] = valid[c] = true;
            }
        }
        StringBuffer res = new StringBuffer(s.length());
        for (int i=0; i<s.length(); i++) {
            if (valid[s.charAt(i)]) {
                res.append(s.charAt(i));
            }
        }
        return res.toString();
    }


    /*
    Performance characteristics (ms, 1.8 GHz PowerPC G5 iMac with 1Gb RAM, reps=500)

    NeedlemanWunsch	                3753	3814	3851	3824	3862
    NeedlemanWunschAffine	        9626	9678	9753	9839	9782
    NeedlemanWunschLinearSpace	    3468	3534	3518	3542	3567
    SmithWaterman	                4249	4332	4342	4363	4409
    SmithWatermanLinearSpaceAffine	6824	6929	6924	6981	6975
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

        int reps = 500;

        NeedlemanWunsch nw = new NeedlemanWunsch(new Blosum45(), 10);
        long start = System.currentTimeMillis();
        for (int i = 0; i < reps; i++) {
            nw.doAlignment(seq1, seq2);
        }
        System.out.print("NeedlemanWunsch\t");
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

    }
}
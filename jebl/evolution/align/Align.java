package jebl.evolution.align;


import jebl.evolution.align.scores.Scores;
import jebl.evolution.align.scores.Blosum50;

abstract class Align {

    Scores sub;             // scores matrix
    int d;                        // gap cost
    String seq1, seq2;            // the sequences
    int n, m;                     // their lengths
    Traceback B0;                 // the starting point of the traceback

    final static int NegInf = Integer.MIN_VALUE/2; // negative infinity

    public Align(Scores sub, int d, String seq1, String seq2) {
        this.sub = sub;
        this.seq1 = strip(seq1); this.seq2 = strip(seq2);
        this.d = d;
        this.n = this.seq1.length(); this.m = this.seq2.length();
    }


    // Return two-element array containing an alignment with maximal score
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
        String[] res = { res1.reverse().toString(), res2.reverse().toString() };
        return res;
    }

    public String fmtscore(int val) {
        if (val < NegInf/2) {
            return "-Inf";
        } else {
            return Integer.toString(val);
        }
    }

    // Print the score, the F matrix, and the alignment
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
        out.println();
    }

    public void doMatch(Output out, String msg) { doMatch(out, msg, false); }

    // Get the next state in the traceback
    public Traceback next(Traceback tb) { return tb; } // dummy implementation for the `smart' algs.

    /**
     * @return the score of the best alignment
     */
    public abstract int getScore();

    // Print the matrix (matrices) used to compute the alignment
    public abstract void printf(Output out);

    // test main
    public static void main(String[] args) {

        Blosum50 blosum50 = new Blosum50();

        String seq1 =
                "MYKRLFISHVILIFALILVISTPNVLAESQPDPKPDELHKSSKFTGLMENMKVLYDDNHVSAINVKSIDQ\n" +
                "FLYFDLIYSIKDTKLGNYDNVRVEFKNKDLADKYKDKYVDVFGANYYYQCYFSKKTNDINSHQTDKRKTC\n" +
                "MYGGVTEHNGNQLDKYRSITVRVFEDGKNLLSFDVQTNKKKVTAQELDYLTRHYLVKNKKLYEFNNSPYE\n" +
                "TGYIKFIENENSFWYDMMPAPGDKFDQSKYLMMYNDNKMVDSKDVKIEVYLTTKKK";

        String seq2 =
                "PLQMKKTAFTLLLFIALTLTTSPLVNGSEKSEEINEKDLRKKSELQGTALGNLKQIYYYNEKAKTENKES\n" +
                "HDQFLQHTILFKGFFTDHSWYNDLLVDFDSKDIVDKYKGKKVDLYGAYYGYQCAGGTPNKTACMYGGVTL\n" +
                "HDNNRLTEEKKVPINLWLDGKQNTVPLETVKTNKKNVTVQELDLQARRYLQEKYNLYNSDVFDGKVQRGL\n" +
                "IVFHTSTEPSVNYDLFGAQGQYSNTLLRIYRDNKTISSENMHIDIYLYTSY";

        SmithWaterman sw = null;
        long time = System.currentTimeMillis();
        for (int i = 0; i < 50; i++) {
             sw = new SmithWaterman(blosum50,8,seq1,seq2);
        }
        long timeTaken = System.currentTimeMillis() - time;
        System.out.println(timeTaken + " ms");
        sw.doMatch(new SystemOut(),"blah");

        /*NeedlemanWunsch nw = null;
        time = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
             nw = new NeedlemanWunsch(blosum50,8,seq1,seq2);
        }
        timeTaken = System.currentTimeMillis() - time;
        System.out.println(timeTaken + " ms");
        nw.doMatch(new SystemOut(),"blah");*/

        SmithWatermanLinearSpace swls = null;
        time = System.currentTimeMillis();
        for (int i = 0; i < 50; i++) {
             swls = new SmithWatermanLinearSpace(blosum50,8,seq1,seq2);
        }
        timeTaken = System.currentTimeMillis() - time;
        System.out.println(timeTaken + " ms");
        swls.doMatch(new SystemOut(),"blah");

        SmithWatermanLinearSpaceAffine swlsa = null;
        time = System.currentTimeMillis();
        for (int i = 0; i < 50; i++) {
             swlsa = new SmithWatermanLinearSpaceAffine(blosum50,10,2,seq1,seq2);
        }
        timeTaken = System.currentTimeMillis() - time;
        System.out.println(timeTaken + " ms");
        swlsa.doMatch(new SystemOut(),"blah");
    }

    // auxillary static functions

    static int max(int x1, int x2) { return (x1 > x2 ? x1 : x2); }

    static int max(int x1, int x2, int x3) { return max(x1, max(x2, x3)); }

    static int max(int x1, int x2, int x3, int x4) { return max(max(x1, x2), max(x3, x4)); }

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
     * @param s
     * @return the stripped string
     */
    private String strip(String s) {

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
}
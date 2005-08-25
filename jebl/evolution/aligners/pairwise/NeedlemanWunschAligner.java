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

import jebl.evolution.aligners.scores.Scores;
import jebl.evolution.sequences.Sequence;

public class NeedlemanWunschAligner extends SimpleAligner {

    private int prev = 0;
    private int curr = 1;
    private float maxScore = 0;

    public NeedlemanWunschAligner(Scores scores, float gapCost) {
        super(scores, gapCost);
    }

    /**
     * @param sequence1
     * @param sequence2
     */
    protected void doAlignment(Sequence sequence1, Sequence sequence2) {

        int n = sequence1.getLength();
        int m = sequence2.getLength();

	    int[] seq1 = sequence1.getStateIndices();
	    int[] seq2 = sequence2.getStateIndices();

        float d = getGapCost();
        Scores scores = getScores();

        F[curr][0] = -d;
        for (int i=1; i<=n; i++) {
            B[i][0].setTraceback(i-1, 0);
        }
        for (int j=1; j<=m; j++) {
            F[prev][j] = -d * j;
            B[0][j].setTraceback(0, j-1);
        }
        for (int i=1; i<=n; i++) {
            for (int j=1; j<=m; j++) {
	            float s = scores.getScore(seq1[i-1], seq2[j-1]);
                float val = max(F[prev][j-1]+s, F[prev][j]-d, F[curr][j-1]-d);
                F[curr][j] = val;
                if (val == F[prev][j-1]+s) {
                    B[i][j].setTraceback(i-1, j-1);
                } else if (val == F[prev][j]-d) {
                    B[i][j].setTraceback(i-1, j);
                } else if (val == F[curr][j-1]-d) {
                    B[i][j].setTraceback(i, j-1);
                } else {
                    throw new Error("Error in Needleman-Wunch pairwise alignment.");
                }
            }
            int temp = prev;
            prev = curr;
            curr = temp;
            F[curr][0] = -d * (i + 1);
        }
        B0 = new TracebackSimple(n, m);
        maxScore = F[curr][m];
    }

    /**
     * @return the score of the best alignment
     */
    public float getScore() { return maxScore; }
}

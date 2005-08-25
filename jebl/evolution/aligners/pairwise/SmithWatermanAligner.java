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

public class SmithWatermanAligner extends SimpleAligner {

    public SmithWatermanAligner(Scores scores, float gapCost) {
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

        int maxi = n, maxj = m;
        float maxval = Float.NEGATIVE_INFINITY;
        for (int i=1; i<=n; i++) {
            for (int j=1; j<=m; j++) {
                float s = scores.getScore(seq1[i-1], seq2[j-1]);
                float val = max(0, F[i-1][j-1]+s, F[i-1][j]-d, F[i][j-1]-d);
                F[i][j] = val;
                if (val == 0) {
                    B[i][j].setTraceback(-1,-1);
                } else if (val == F[i-1][j-1]+s) {
                    B[i][j].setTraceback(i-1, j-1);
                } else if (val == F[i-1][j]-d) {
                    B[i][j].setTraceback(i-1, j);
                } else if (val == F[i][j-1]-d) {
                    B[i][j].setTraceback(i, j-1);
                } else {
                    throw new Error("Error in SmithWaterman alignment.");
                }
                if (val > maxval) {
                    maxval = val;
                    maxi = i; maxj = j;
                }
            }
        }
        B0 = new TracebackSimple(maxi, maxj);
    }

	/**
	 * @param sequence1
	 * @param sequence2
	 */
	protected void doAlignment2(Sequence sequence1, Sequence sequence2) {

	    int n = sequence1.getLength();
	    int m = sequence2.getLength();

	    float d = getGapCost();
	    Scores scores = getScores();

	    int maxi = n, maxj = m;
	    float maxval = Float.NEGATIVE_INFINITY;
	    for (int i=1; i<=n; i++) {
	        for (int j=1; j<=m; j++) {
	            float s = scores.getScore(sequence1.getState(i-1), sequence2.getState(j-1));
	            float val = max(0, F[i-1][j-1]+s, F[i-1][j]-d, F[i][j-1]-d);
	            F[i][j] = val;
	            if (val == 0) {
	                B[i][j].setTraceback(-1,-1);
	            } else if (val == F[i-1][j-1]+s) {
	                B[i][j].setTraceback(i-1, j-1);
	            } else if (val == F[i-1][j]-d) {
	                B[i][j].setTraceback(i-1, j);
	            } else if (val == F[i][j-1]-d) {
	                B[i][j].setTraceback(i, j-1);
	            } else {
	                throw new Error("Error in SmithWaterman alignment.");
	            }
	            if (val > maxval) {
	                maxval = val;
	                maxi = i; maxj = j;
	            }
	        }
	    }
	    B0 = new TracebackSimple(maxi, maxj);
	}
}

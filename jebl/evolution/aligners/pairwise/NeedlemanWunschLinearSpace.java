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

public class NeedlemanWunschLinearSpace extends LinearSpaceAligner {

    private int prev = 0;
    private int curr = 1;
    private float maxScore = 0;

	private int u;     // Halfway through seq1
	private int[][] c; // Best alignment from (0,0) to (i,j) passes through (u, c[i][j])

    public NeedlemanWunschLinearSpace(Scores scores, float gapCost) {
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

        float[][] scoreMatrix = getScores().getScoreMatrix();

	    u = n/2;
	    c = new int[2][m+1];

	    for (int j=0; j<=m; j++) {
	        F[1][j] = -d * j;
	    }

	    float s, val;
	    for (int i=1; i<=n; i++) {
	        swap(F); swap(c);
	        // F[1] represents (new) column i and F[0] represents (old) column i-1
	        F[1][0] = -d * i;
	        for (int j=1; j<=m; j++) {
	            s = scoreMatrix[seq1[i-1]][seq2[j-1]];
	            val = max(F[0][j-1]+s, F[0][j]-d, F[1][j-1]-d);
	            F[1][j] = val;
	            if (i == u) {
	                c[1][j] = j;
	            } else {
	                if (val == F[0][j-1]+s) {
	                    c[1][j] = c[0][j-1];
	                } else if (val == F[0][j]-d) {
	                    c[1][j] = c[0][j];
	                } else if (val == F[1][j-1]-d) {
	                    c[1][j] = c[1][j-1];
	                } else {
		                throw new Error("Error in Needleman-Wunch linear-space pairwise alignment.");
	                }
	            }
	        }
	    }

	    maxScore = c[1][m];
    }

	static void swap(Object[] A) {
	    Object tmp = A[1]; A[1] = A[0]; A[0] = tmp;
	}

    /**
     * @return the score of the best alignment
     */
    public float getScore() { return maxScore; }
}

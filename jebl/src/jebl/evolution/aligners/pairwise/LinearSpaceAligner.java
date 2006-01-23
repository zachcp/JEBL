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

import java.io.PrintStream;

abstract class LinearSpaceAligner extends AbstractAligner {

    private final float gapCost;        // gap cost
    float[][] F = null;                 // the matrix used to compute the alignment
    TracebackSimple[][] B;             	// the traceback matrix

    public LinearSpaceAligner(Scores scores, float gapCost) {
        super(scores);
        this.gapCost = gapCost;
    }

    protected void prepareAlignment(Sequence sequence1, Sequence sequence2) {

        int m = sequence2.getLength();

        // If F is null then this is the first run, otherwise check the size of the matrices
        if(F == null || F[0].length < m+1) {
	        F = new float[2][m+1];
        }

    }

    public float getGapCost() {
        return gapCost;
    }

    /**
     * Get the next state in the traceback
     *
     * @param tb current Traceback
     * @return next Traceback
     */
    public Traceback next(Traceback tb) {
        TracebackSimple tb2 = (TracebackSimple)tb;
        if(tb.i == 0 && tb.j == 0 && B[tb2.i][tb2.j].i == 0 && B[tb2.i][tb2.j].j == 0 || tb.i == -1)
            return null;
        else
            return B[tb2.i][tb2.j];
    }

    /**
     * @return the score of the best alignment
     */
    public float getScore() { return F[B0.i][B0.j]; }

    /**
     * Print matrix used to calculate this alignment.
     *
     * @param out PrintStream to print to.
     */
    public void printMatrix(PrintStream out) {

        for (int j=0; j <= F.length; j++) {
            for (float[] f : F) {
                out.print(padLeft(formatScore(f[j]), 5));
            }
            out.println();
        }
    }
}


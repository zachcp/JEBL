package jebl.evolution.aligners.pairwise;

import jebl.evolution.aligners.scores.Scores;
import jebl.evolution.sequences.Sequence;

import java.io.PrintStream;

/**
 * Alignment with affine gap costs
 */

abstract class AffineAligner extends AbstractAligner {

    private final float gapOpen;        // gap opening cost
    private final float gapExtend;      // gap extension cost
    float[][][] F = null;               // the matrices used to compute the alignment
    TracebackAffine[][][] B = null;     // the traceback matrix

    public AffineAligner(Scores scores, float gapOpen, float gapExtend) {
        super(scores);
        this.gapOpen = gapOpen;
        this.gapExtend = gapExtend;
    }

    protected void prepareAlignment(Sequence sequence1, Sequence sequence2) {

        int n = sequence1.getLength();
        int m = sequence2.getLength();

        // If F is null then this is the first run, otherwise check the size of the matrices
        if(F == null || F[0].length < n+1 || F[0][0].length < m+1) {
            F = new float[3][n+1][m+1];
            B = new TracebackAffine[3][n+1][m+1];
            for(int k = 0; k < 3; k++) {
                for(int i = 0; i < n+1; i ++) {
                    for(int j = 0; j < m+1; j++)
                        B[k][i][j] = new TracebackAffine(0,0,0);
                }
            }
        }
    }

    public float getGapOpen() {
        return gapOpen;
    }

    public float getGapExtend() {
        return gapExtend;
    }

    /**
     * Get the next state in the traceback
     * 
     * @param tb current Traceback
     * @return next Traceback
     */
    public Traceback next(Traceback tb) {
        TracebackAffine tb3 = (TracebackAffine)tb;

        //traceback has reached the origin, therefore stop.
        if(tb3.i + tb3.j + B[tb3.k][tb3.i][tb3.j].i + B[tb3.k][tb3.i][tb3.j].j == 0)
            return null;

        else
            return B[tb3.k][tb3.i][tb3.j];
    }

    /**
     * @return score for this alignment
     */
    public float getScore() {
        return F[((TracebackAffine)B0).k][B0.i][B0.j];
    }

    /**
     * Print matrix used to calculate this alignment.
     *
     * @param out PrintStream to print to.
     */
    public void printMatrix(PrintStream out) {

        for (int k=0; k<3; k++) {
            out.println("F[" + k + "]:");
            for (int j=0; j<=F[k][0].length; j++) {
                for (int i=0; i<F[k].length; i++) {
                    out.print(padLeft(formatScore(F[k][i][j]), 5));
                }
                out.println();
            }
        }
    }
}


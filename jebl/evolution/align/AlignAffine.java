package jebl.evolution.align;

import jebl.evolution.align.scores.Scores;


// Alignment with affine gap costs

abstract class AlignAffine extends Align {

    float e;                        // gap extension cost
    float[][][] F;                  // the matrices used to compute the alignment
    TracebackAffine[][][] B;      // the traceback matrix

    public AlignAffine(Scores sub, float d, float e, String sq1, String sq2) {

        super(sub, d, sq1, sq2);
        this.e = e;
        F = new float[3][n+1][m+1];
        B = new TracebackAffine[3][n+1][m+1];
    }

    public Traceback next(Traceback tb) {

        TracebackAffine tb3 = (TracebackAffine)tb;
        return B[tb3.k][tb3.i][tb3.j];
    }

    public float getScore() {
        return F[((TracebackAffine)B0).k][B0.i][B0.j];
    }

    public void printf(Output out) {

        for (int k=0; k<3; k++) {
            out.println("F[" + k + "]:");
            for (int j=0; j<=m; j++) {
                for (int i=0; i<F[k].length; i++) {
                    out.print(padLeft(formatScore(F[k][i][j]), 5));
                }
                out.println();
            }
        }
    }
}


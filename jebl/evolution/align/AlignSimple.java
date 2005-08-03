package jebl.evolution.align;

import jebl.evolution.align.scores.Scores;

abstract class AlignSimple extends Align {

    float[][] F;                    // the matrix used to compute the alignment
    TracebackSimple[][] B;             // the traceback matrix

    public AlignSimple(Scores sub, float d, String seq1, String seq2) {
        super(sub, d, seq1, seq2);
        F = new float[n+1][m+1];
        B = new TracebackSimple[n+1][m+1];
    }

    public Traceback next(Traceback tb) {
        TracebackSimple tb2 = (TracebackSimple)tb;
        return B[tb2.i][tb2.j];
    }

    public float getScore() { return F[B0.i][B0.j]; }

    public void printf(Output out) {

        for (int j=0; j<=m; j++) {
            for (int i=0; i<F.length; i++) {
                out.print(padLeft(formatScore(F[i][j]), 5));
            }
            out.println();
        }
    }
}
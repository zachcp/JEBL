package jebl.evolution.align;

import jebl.evolution.align.scores.Scores;

abstract class AlignLinearSpace extends Align {

    int[][] F;

    public AlignLinearSpace(Scores sub, int d, String sq1, String sq2) {

        super(sub, d, sq1, sq2);
        F = new int[2][m+1];
    }

    public void printf(Output out) {
        for (int j=0; j<=m; j++) {
            for (int i=0; i<F.length; i++) {
                out.print(padLeft(fmtscore(F[i][j]), 5));
            }
            out.println();
        }
    }

    static void swap01(Object[] A) {
        Object tmp = A[1]; A[1] = A[0]; A[0] = tmp;
    }
}

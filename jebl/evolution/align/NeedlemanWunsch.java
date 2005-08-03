package jebl.evolution.align;

import jebl.evolution.align.scores.Scores;

public class NeedlemanWunsch extends AlignSimple {

    public NeedlemanWunsch(Scores sub, int d, String sq1, String sq2) {

        super(sub, d, sq1, sq2);
        int n = this.n, m = this.m;
        int[][] score = sub.score;

        for (int i=1; i<=n; i++) {
            F[i][0] = -d * i;
            B[i][0] = new TracebackSimple(i-1, 0);
        }
        for (int j=1; j<=m; j++) {
            F[0][j] = -d * j;
            B[0][j] = new TracebackSimple(0, j-1);
        }
        for (int i=1; i<=n; i++) {
            for (int j=1; j<=m; j++) {
                int s = score[seq1.charAt(i-1)][seq2.charAt(j-1)];
                int val = max(F[i-1][j-1]+s, F[i-1][j]-d, F[i][j-1]-d);
                F[i][j] = val;
                if (val == F[i-1][j-1]+s) {
                    B[i][j] = new TracebackSimple(i-1, j-1);
                } else if (val == F[i-1][j]-d) {
                    B[i][j] = new TracebackSimple(i-1, j);
                } else if (val == F[i][j-1]-d) {
                    B[i][j] = new TracebackSimple(i, j-1);
                } else {
                    throw new Error("Error in Needleman-Wunch pairwise alignment.");
                }
            }
        }
        B0 = new TracebackSimple(n, m);
    }
}

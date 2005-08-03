package jebl.evolution.align;

import jebl.evolution.align.scores.Scores;

public class SmithWaterman extends AlignSimple {

    public SmithWaterman(Scores sub, float d, String sq1, String sq2) {

        super(sub, d, sq1, sq2);
        int n = this.n, m = this.m;
        float[][] score = sub.score;
        int maxi = n, maxj = m;
        float maxval = Float.NEGATIVE_INFINITY;
        for (int i=1; i<=n; i++) {
            for (int j=1; j<=m; j++) {
                float s = score[seq1.charAt(i-1)][seq2.charAt(j-1)];
                float val = max(0, F[i-1][j-1]+s, F[i-1][j]-d, F[i][j-1]-d);
                F[i][j] = val;
                if (val == 0) {
                    B[i][j] = null;
                } else if (val == F[i-1][j-1]+s) {
                    B[i][j] = new TracebackSimple(i-1, j-1);
                } else if (val == F[i-1][j]-d) {
                    B[i][j] = new TracebackSimple(i-1, j);
                } else if (val == F[i][j-1]-d) {
                    B[i][j] = new TracebackSimple(i, j-1);
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

package jebl.evolution.align;

import jebl.evolution.align.scores.Scores;

public class RepeatAlign extends AlignSimple {

    private int T;

    public RepeatAlign(Scores sub, int d, int T, String sq1, String sq2) {
        super(sub, d, sq1, sq2);
        this.T = T;
        int n = this.n, m = this.m;
        int[][] score = sub.score;

        // F[0][0..m] = 0 by construction
        for (int i=1; i<=n; i++) {
            int maxj = maxj(i-1);
            F[i][0] = maxjval(i-1, maxj);
            B[i][0] = new TracebackSimple(i-1, maxj);

            for (int j=1; j<=m; j++) {
                int s = score[seq1.charAt(i-1)][seq2.charAt(j-1)];
                int val = max(F[i][0], F[i-1][j-1]+s, F[i-1][j]-d, F[i][j-1]-d);
                F[i][j] = val;
                if (val == F[i][0]) {
                    B[i][j] = new TracebackSimple(i, 0);
                } else if (val == F[i-1][j-1]+s) {
                    B[i][j] = new TracebackSimple(i-1, j-1);
                } else if (val == F[i-1][j]-d) {
                    B[i][j] = new TracebackSimple(i-1, j);
                } else if (val == F[i][j-1]-d) {
                    B[i][j] = new TracebackSimple(i, j-1);
                } else {
                    throw new Error("Error in repeat align.");
                }
            }
        }
        B0 = new TracebackSimple(n, maxj(n));
    }

    public String[] getMatch() {

        StringBuffer res1 = new StringBuffer();
        StringBuffer res2 = new StringBuffer();
        Traceback tb = B0;
        int i = tb.i, j = tb.j;
        while ((tb = next(tb)) != null) {
            if (i != tb.i) {          // Never make a gap in seq1
                res1.append(seq1.charAt(i-1));
                if (j == 0) {
                  res2.append('.');
                } else if (j == tb.j) {
                  res2.append('-');
                } else {
                  res2.append(seq2.charAt(j-1));
                }
            }
            i = tb.i; j = tb.j;
        }
        String[] res = { res1.reverse().toString(), res2.reverse().toString() };
        return res;
    }

    private int maxj(int i) {

        int maxj = 0, val = F[i][maxj]+T;
        for (int j=1; j<=m; j++) {
            if (val < F[i][j]) {
                maxj = j;
                val = F[i][j];
            }
        }
        return maxj;
    }

    private int maxjval(int i, int maxj) {
        if (maxj == 0) {
            return F[i][0];
        } else {
            return F[i][maxj] - T;
        }
    }
}

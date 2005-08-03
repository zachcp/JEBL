package jebl.evolution.align;

import jebl.evolution.align.scores.Scores;

/**
 * @author Alexei Drummond
 *
 * @version $Id$
 */
public class NeedlemanWunschLinearSpace extends AlignLinearSpace {
    int u;     // Halfway through seq1
    int[][] c; // Best alignment from (0,0) to (i,j) passes through (u, c[i][j])

    public NeedlemanWunschLinearSpace(Scores sub, int d, String sq1, String sq2) {
        super(sub, d, sq1, sq2);
        int n = this.n, m = this.m;
        u = n/2;
        c = new int[2][m+1];
        int[][] score = sub.score;
        for (int j=0; j<=m; j++) {
            F[1][j] = -d * j;
        }
        for (int i=1; i<=n; i++) {
            swap01(F); swap01(c);
            // F[1] represents (new) column i and F[0] represents (old) column i-1
            F[1][0] = -d * i;
            for (int j=1; j<=m; j++) {
                int s = score[seq1.charAt(i-1)][seq2.charAt(j-1)];
                int val = max(F[0][j-1]+s, F[0][j]-d, F[1][j-1]-d);
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
                        throw new Error("NWSmart 1");
                    }
                }
            }
        }
    }

    public int getV() { return c[1][m]; }

    public String[] getMatch() {
        int v = getV();
        if (n > 1 && m > 1) {
            NeedlemanWunschLinearSpace al1, al2;
            al1 = new NeedlemanWunschLinearSpace(sub, d, seq1.substring(0, u), seq2.substring(0, v));
            al2 = new NeedlemanWunschLinearSpace(sub, d, seq1.substring(u),    seq2.substring(v));
            String[] match1 = al1.getMatch();
            String[] match2 = al2.getMatch();
            String[] match = { match1[0] + match2[0], match1[1] + match2[1] };
            return match;
        } else {
            NeedlemanWunsch al = new NeedlemanWunsch(sub, d, seq1, seq2);
            return al.getMatch();
        }
    }

    public int getScore() { return F[1][m]; }
}


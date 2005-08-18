package jebl.evolution.align;

import jebl.evolution.align.scores.Scores;

/**
 * @author Alexei Drummond
 *
 * @version $Id$
 */
public class NWLinearSpaceAffine extends AlignLinearSpaceAffine {

    int u;     // Halfway through seq1
    int[][][] c; // Best alignment from (0,0) to (i,j) passes through (u, c[i][j])

    public NWLinearSpaceAffine(Scores sub, float d, float e) {
        super(sub, d, e);
    }

    /**
     * @param sq1
     * @param sq2
     */
    public void doAlignment(String sq1, String sq2) {

        prepareAlignment(sq1, sq2);

        int n = this.n, m = this.m;
        u = n/2;
        c = new int[3][2][m+1];
        float[][] score = sub.score;

        float[][] M = F[0], Ix = F[1], Iy = F[2];
        int[][] cM = c[0], cIx = c[1], cIy = c[2];
        float val;
        float s, a, b, f;

        for (int j=1; j<=m; j++) {
            Iy[0][j] = -d - e * (j-1);
        }

        for (int j=1; j<=m; j++) {
            Ix[0][j] = M[0][j] = Float.NEGATIVE_INFINITY;
        }

        for (int i=1; i<=n; i++) {
            swap01(F); swap01(c);
            // F[1] represents (new) column i and F[0] represents (old) column i-1
            Ix[1][0] = -d - e* (i-1);

            for (int j=1; j<=m; j++) {
                s = score[seq1.charAt(i-1)][seq2.charAt(j-1)];

                M[1][j] = max(M[0][j-1]+s, Ix[0][j-1]+s, Iy[0][j-1]+s);
                Ix[1][j] = max(M[0][j]-d, Ix[0][j]-e, Iy[0][j]-d);
                Iy[1][j] = max(M[1][j-1]-d, Iy[1][j-1]-e, Ix[1][j-1]-d);

                val = max(M[1][j], Ix[1][j],Iy[1][j]);

/*
                if (i == u) {
                    if (val == M[1][j]) {
                        c[1][j] = j;
                    }
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
*/
            }
        }
    }



}

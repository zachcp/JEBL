package jebl.evolution.align;

import jebl.evolution.align.scores.Scores;

// Global alignment using the Needleman-Wunsch algorithm (affine gap costs)

public class NeedlemanWunschAffine extends AlignAffine {

    public NeedlemanWunschAffine(Scores sub, float d, float e) {
        super(sub, d, e);
    }

	/**
	 * @param sq1
	 * @param sq2
	 */
    public void doAlignment(String sq1, String sq2) {
        doAlignment (sq1,sq2, TYPE_ANY, TYPE_ANY);
        //the type_... parameters should generally only be used by the
        //NeedlemanWunschLinearSpaceAffine algorithm to handle it's base
        //recursion case.
    }

    int[][][] Bi, Bj, Bk;
    private int allocatedn= 0;
    private int allocatedm= 0;
    int B0k, B0i, B0j;

    public void allocateMatrices (int n, int m) {
        //first time running this alignment. Create all new matrices.
        if (n > allocatedn || m > allocatedm) {
            n = maxi(n, allocatedn + 5);
            m = maxi(m, allocatedm + 5);
            allocatedn = n;
            allocatedm = m;
            F = new float[3][n + 1][m + 1];
            Bi = new int[3][n + 1][m + 1];
            Bj = new int[3][n + 1][m + 1];
            Bk = new int[3][n + 1][m + 1];
        }

    }

    public void prepareAlignment(String sq1, String sq2) {

        n = sq1.length();
        m = sq2.length();
        this.seq1 = sq1;
        this.seq2 = sq2;

        allocateMatrices (n,m);
    }

    public void doAlignment(String sq1, String sq2, int startType, int endType) {

    	prepareAlignment(sq1, sq2);

        char[] s1 = sq1.toCharArray();
        char[] s2 = sq2.toCharArray();

        int n = this.n, m = this.m;
        float[][] score = sub.score;
        float[][] M = F[0], Ix = F[1], Iy = F[2];
        float val;
        float s, a, b, c;

        M[0][0]= 0;
        Ix[0][0]= 0;
        Iy[0][0]= 0;

        for (int i=1; i<=n; i++) {
            float base =d;
            if (startType == TYPE_X)
                base = e;//if startType IS TYPE_X then we were already in a
            // gap, so we can use gap extension penalty rather than gap starting penalty
            Ix[i][0] = -base - e * (i-1);
//            B[1][i][0].setTraceback(1, i-1, 0);
            Bk[1][i][0]= 1;
            Bi[1][i][0] =i- 1;
            Bj[1][i][0]= 0;

        }

        for (int i=1; i<=n; i++) {
            Iy[i][0] = M[i][0] = Float.NEGATIVE_INFINITY;
        }

        for (int j=1; j<=m; j++) {
            float base = d;
            if (startType == TYPE_Y)
                base = e;//if startType IS TYPE_Y then we were already in a
            // gap, so we can use gap extension penalty rather than gap starting penalty

            Iy[0][j] = -base - e * (j-1);
//            B[2][0][j].setTraceback(2, 0, j-1);
            Bk[2][0][j]= 2;
            Bi[2][0][j]= 0;
            Bj[2][0][j]=j- 1;
        }

        for (int j=1; j<=m; j++) {
            Ix[0][j] = M[0][j] = Float.NEGATIVE_INFINITY;
        }

        for (int i=1; i<=n; i++) {

            for (int j=1; j<=m; j++) {
                s = score[s1[i-1]][s2[j-1]];
                a = M[i-1][j-1]+s;
                b = Ix[i-1][j-1]+s;
                c = Iy[i-1][j-1]+s;

                val = M[i][j] = max(a, b, c);
                Bi[0][i][j]=i- 1;
                Bj[0][i][j]=j- 1;
                if (val == a) {
                    Bk[0][i][j]= 0;
//                    B[0][i][j].setTraceback(0, i-1, j-1);
                } else if (val == b) {
                    Bk[0][i][j] = 1;
//                    B[0][i][j].setTraceback(1, i-1, j-1);
                } else if (val == c) {
                    Bk[0][i][j] = 2;
//                    B[0][i][j].setTraceback(2, i-1, j-1);
                } else {
                    throw new Error("NWAffine 1");
                }

                a = M[i-1][j]-d;
                b = Ix[i-1][j]-e;
                c = Iy[i-1][j]-d;
                val = Ix[i][j] = max(a, b, c);

                Bi[1][i][j] = i - 1;
                Bj[1][i][j] = j;
                if (val == a) {
                    Bk[1][i][j]= 0;
//                    B[1][i][j].setTraceback(0, i-1, j);
                } else if (val == b) {
                    Bk[1][i][j] = 1;
//                    B[1][i][j].setTraceback(1, i-1, j);
                } else if (val == c) {
                    Bk[1][i][j] = 2;
//                    B[1][i][j].setTraceback(2, i-1, j);
                } else {
                    throw new Error("NWAffine 2");
                }

                a = M[i][j-1]-d;
                b = Iy[i][j-1]-e;
                c = Ix[i][j-1]-d;
                val = Iy[i][j] = max(a, b, c);
                Bi[2][i][j] = i;
                Bj[2][i][j] = j- 1;
                if (val == a) {
                    Bk[2][i][j]= 0;
//                    B[2][i][j].setTraceback(0, i, j-1);
                } else if (val == b) {
                    Bk[2][i][j] = 2;
//                    B[2][i][j].setTraceback(2, i, j-1);
                } else if (val == c) {
                    Bk[2][i][j] = 1;
//                    B[2][i][j].setTraceback(1, i, j-1);
                } else {
                    throw new Error("NWAffine 3");
                }
            }
        }
        // Find maximal score
        int maxk = 0;
        float maxval = F[0][n][m];
        for (int k=1; k<3; k++) {
            if (maxval < F[k][n][m]) {
                maxval = F[k][n][m];
                maxk = k;
            }
        }
        if (endType == TYPE_X)
            maxk= 1;
        if (endType == TYPE_Y)
            maxk= 2;

        B0k= maxk;
        B0i=n;
        B0j=m;
//        B0 = new TracebackAffine(maxk, n, m);
    }


    public String[] getMatch() {

        char[] sq1 = seq1.toCharArray();
        char[] sq2 = seq2.toCharArray();

        StringBuffer res1 = new StringBuffer();
        StringBuffer res2 = new StringBuffer();
        int tbi,tbj,tbk;
//        Traceback tb = B0;

        int i= B0i;
        int j= B0j;
        int k= B0k;
//        int i = tb.i, j = tb.j;
        while (i!= 0 || j!= 0) {
            tbi= Bi[k][i][j];
            tbj= Bj[k][i][j];
            tbk= Bk[k][i][j];

            if (i == tbi) {
                res1.append('-');
            } else {
                res1.append(sq1[i - 1]);
            }
            if (j == tbj) {
                res2.append('-');
            } else {
                res2.append(sq2[j - 1]);
            }
            i = tbi;
            j = tbj;
            k = tbk;
        }
        return new String[]{res1.reverse().toString(), res2.reverse().toString()};
    }


    public float getScore() {
        return F [ B0k][ B0i] [ B0j];
    }

    private static final int TYPE_ANY = 0;
    private static final int TYPE_X = 1;
    private static final int TYPE_Y = 2;

}

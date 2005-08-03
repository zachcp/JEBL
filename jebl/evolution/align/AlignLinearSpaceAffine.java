package jebl.evolution.align;

import jebl.evolution.align.scores.Scores;

// Alignment with affine gap costs; smart linear-space algorithm

abstract class AlignLinearSpaceAffine extends Align {

    float e;        // gap extension cost
    float[][][] F;  // the matrices used to compute the alignment

    public AlignLinearSpaceAffine(Scores sub, float d, float e, String sq1, String sq2) {
    super(sub, d, sq1, sq2);
    this.e = e;
    F = new float[3][2][m+1];
    }

    public void printf(Output out) {
    for (int k=0; k<3; k++) {
      out.println("F[" + k + "]:");
      for (int j=0; j<=m; j++) {
        for (int i=0; i<F[k].length; i++)
      out.print(padLeft(formatScore(F[k][i][j]), 5));
        out.println();
      }
    }
    }

    static void swap01(Object[] A)
    { Object tmp = A[1]; A[1] = A[0]; A[0] = tmp; }
}
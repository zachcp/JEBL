package jebl.evolution.align.scores;

public class Pam220 extends AminoAcidScores {

  private final float[][] residueScores = {

            /*  A   R   N   D   C   Q   E   G   H   I   L   K   M   F   P   S   T   W   Y   V */
            {   2},
            {  -2,  7},
            {   0,  0,  3},
            {   0, -2,  2,  4},
            {  -2, -4, -4, -6, 12},
            {  -1,  1,  1,  2, -6,  5},
            {   0, -1,  2,  4, -6,  3,  4},
            {   1, -3,  0,  0, -4, -2,  0,  5},
            {  -2,  2,  2,  1, -4,  3,  1, -3,  7},
            {  -1, -2, -2, -3, -3, -2, -2, -3, -3,  5},
            {  -2, -3, -3, -5, -7, -2, -4, -5, -2,  2,  6},
            {  -1,  4,  1,  0, -6,  1,  0, -2,  0, -2, -3,  5},
            {  -1, -1, -2, -3, -6, -1, -2, -3, -3,  2,  4,  1,  8},
            {  -4, -5, -4, -6, -5, -5, -6, -5, -2,  1,  2, -6,  0, 10},
            {   1,  0, -1, -1, -3,  0, -1, -1,  0, -2, -3, -1, -2, -5,  7},
            {   1,  0,  1,  0,  0, -1,  0,  1, -1, -2, -3,  0, -2, -4,  1,  2},
            {   1, -1,  0,  0, -3, -1, -1,  0, -2,  0, -2,  0, -1, -4,  0,  2,  3},
            {  -6,  2, -4, -8, -8, -5, -8, -8, -3, -6, -2, -4, -5,  0, -6, -3, -6, 17},
            {  -4, -5, -2, -5,  0, -5, -5, -6,  0, -1, -1, -5, -3,  7, -6, -3, -3,  0, 11},
            {   0, -3, -2, -3, -2, -2, -2, -2, -3,  4,  2, -3,  2, -2, -1, -1,  0, -7, -3,  5}};

  public Pam220() { buildScores(residueScores); }
}

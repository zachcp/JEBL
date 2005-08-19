package jebl.evolution.align.scores;

public class Hamming extends NucleotideScores {

  private final float[][] residueScores = {

            /*  A   C   G   T   U */
            {   0},
            {  -1,  0},
            {  -1, -1,  0},
            {  -1, -1,  -1,  0},
            {  -1, -1,  -1,  0, 0}};

  public Hamming() { buildScores(residueScores); }
}

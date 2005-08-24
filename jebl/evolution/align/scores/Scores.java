package jebl.evolution.align.scores;

/**
 * Base class for all score matrices in the package.
 *
 * @author Alexei Drummond
 *
 * @version $Id$
 *
 * Based on code originally by Peter Setsoft. See package.html.
 */
public abstract class Scores implements ScoreMatrix {

    public float[][] score;

    protected void buildScores(float[][] scores) {

        String states = getAlphabet();
        // Allow lowercase and uppercase states (ASCII code <= 127):
        score = new float[127][127];
        for (int i=0; i<states.length(); i++) {
            char res1 = states.charAt(i);
            for (int j=0; j<=i; j++) {
                char res2 = states.charAt(j);
                score[res1][res2] = score[res2][res1]
                    = score[res1][res2+32] = score[res2+32][res1]
                    = score[res1+32][res2] = score[res2][res1+32]
                    = score[res1+32][res2+32] = score[res2+32][res1+32]
                    = scores[i][j];
            }
        }
    }

    public final float getScore(char x, char y) {
        return score[x][y];
    }

    public String toString() {
        String name = getClass().getName();
        return name.substring(name.lastIndexOf(".")+1);
    }
}
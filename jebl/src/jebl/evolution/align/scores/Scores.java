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

    public static Scores duplicate(Scores scores){
        Scores result = null;
        if(scores instanceof AminoAcidScores)
            result =new AminoAcidScores();
        else
            result =new NucleotideScores();
        result.score = new float[127][127];
        for (int i = 0; i < 127; i++) {
            for (int j = 0; j < 127; j++) {
                result.score[i][j]= scores.score[i][j];
            }

        }
        return result;
    }

    public static Scores includeGaps(Scores scores, float gapCost) {
        Scores result =duplicate(scores);
        String states = scores.getAlphabet();
        for (int i = 0; i < states.length(); i++) {
            char res1 = states.charAt(i);
            result.score['-'] [res1] = gapCost;
            result.score[res1]['-'] = gapCost;

        }
        return result;
    }
}
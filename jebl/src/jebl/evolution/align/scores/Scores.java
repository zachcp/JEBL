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

    void buildScores(float match, float mismatch) {
        int states = getAlphabet().length();
        float[][] scores = new float[states][states];

        for (int i = 0; i < states; i++) {
            for (int j = 0; j < states; j++) {
                if (i == j) {
                    scores[i][j] = match;
                } else {
                    scores[i][j] = mismatch;
                }
            }
        }
        buildScores(scores);
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

    /**
     *
     * @param scores
     * @param gapVersusResidueCost should be a negative value
     * @param gapVersusGapCost should be a positive value
     */
    public static Scores includeGaps(Scores scores, float gapVersusResidueCost, float gapVersusGapCost) {
//        System.out.println("cost =" + gapVersusResidueCost+ "," + gapVersusGapCost);
        Scores result =duplicate(scores);
        String states = scores.getAlphabet();
        for (int i = 0; i < states.length(); i++) {
            char res1 = states.charAt(i);
            result.score['-'] [res1] = gapVersusResidueCost;
            result.score[res1]['-'] = gapVersusResidueCost;
        }
        result.score['-']['-'] = gapVersusGapCost;
        return result;
    }

    /**
     * extends the given score matrix to include gap Versus gap and gap Versus residue costs
     * The gap versus the gap cost is taken to be the same as the average residue match cost
     * The gap in versus residue cost is taken to be the same as the average residue mismatch cost
     * @param scores
     */
    // this function is a bad idea, don't use it.
/*    public static Scores includeGaps(Scores scores) {
        float totalMismatch = 0;
        float totalMatch = 0;
        int mismatchCount= 0;
        int matchCount = 0;
        String states = scores.getAlphabet();
        for (int i = 0; i < states.length(); i++) {
            char res1 = states.charAt(i);
            for (int j = 0; j < states.length(); j++) {
                char res2 = states.charAt(j);
                double score = scores.score[res1] [res2];
                if(i==j) {
                    totalMatch += score;
                    matchCount ++;
                }
                else {
                    totalMismatch += score;
                    mismatchCount ++;
                }
            }
        }
        return includeGaps(scores, totalMismatch/mismatchCount-0.1f, totalMatch/matchCount);
    }*/
}
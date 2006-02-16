package jebl.evolution.align.scores;

/**
 * 
 * @author Richard Moir
 * @author Alexei Drummond
 *
 * @version $Id$
 * 
 */
public class NucleotideScores extends Scores {

    float match = 5;
    float mismatchTransition = -4;
    float mismatchTransversion = -4;
    String name= "";

    public NucleotideScores() {}

    /**
     * @param m match score
     * @param n mismatch score
     */
    public NucleotideScores(float m, float n) {
        this("",m,n,n);
    }

    public NucleotideScores(String name, float match, float mismatchTransition, float mismatchTransversion) {
        this . name = name;
        buildScores(match, mismatchTransition, mismatchTransversion);
    }


    void buildScores(float match, float mismatchTransition, float mismatchTransversion) {

        this.match = match;
        this.mismatchTransition = mismatchTransition;
        this.mismatchTransversion = mismatchTransversion;

        int states = getAlphabet().length();
        float[][] scores = new float[states][states];

        for (int i = 0; i < states; i++) {
            for (int j = 0; j < states; j++) {
                if (i == j) {
                    scores[i][j] = match;
                }
                else if(isPurine(i)== isPurine(j)) {
                    scores[i][j] = mismatchTransition;
                }
                else {
                    scores[i][j] = mismatchTransversion;

                }
            }
        }
        buildScores(scores);
    }

    private String residues = "ACGT";

    private boolean isPurine(int state) {
        return state == 0 || state == 2;
    }

    public final String getAlphabet() { return residues; }

    public String toString() {
        String result = match + "/" + mismatchTransition;
        if(mismatchTransversion != mismatchTransition) {
            result = result + "/" + mismatchTransversion;
        }
        if(name.length ()> 0){
            result = name + " (" + result + ")";
        }
        return result;
    }
}


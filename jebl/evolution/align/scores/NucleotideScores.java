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
    float mismatch = -4;

    /**
     * @param m match score
     * @param n mismatch score
     */
    public NucleotideScores(float m, float n) {

        match = m;
        mismatch = n;

        int states = getStates().length();
        float[][] scores = new float[states][states];

        for (int i = 0; i < states; i++) {
            for (int j = 0; j < states; j++) {
                if (i == j) {
                    scores[i][j] = match;
                }
                else {
                    scores[i][j] = mismatch;
                }
            }
        }       
        buildScores(scores);
    }

    private String residues = "ACGT";
	
	public final String getStates() { return residues; }

    public String toString() {
        return match + "/" + mismatch;
    }
}


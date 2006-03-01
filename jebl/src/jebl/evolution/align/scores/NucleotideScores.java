package jebl.evolution.align.scores;

import jebl.evolution.sequences.Nucleotides;

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
     * @param match match score
     * @param misMatch mismatch score
     */
    public NucleotideScores(float match, float misMatch) {
        this("", match, misMatch, misMatch);
    }

    public NucleotideScores(String name, float match, float mismatchTransition, float mismatchTransversion) {
        this.name = name;
        buildScores(match, mismatchTransition, mismatchTransversion);
    }


    void buildScores(float match, float mismatchTransition, float mismatchTransversion) {

        this.match = match;
        this.mismatchTransition = mismatchTransition;
        this.mismatchTransversion = mismatchTransversion;

        final int states = Nucleotides.getCanonicalStateCount();
        float[][] scores = new float[states][states];

        for (int i = 0; i < states; i++) {
            for (int j = 0; j < states; j++) {
                float val = (i == j) ? match :
                        ((isPurine(i) == isPurine(j)) ? mismatchTransition : mismatchTransversion);
                scores[i][j] = val;
            }
        }
        buildScores(scores);
    }

    private static final String residues =
            Nucleotides.CANONICAL_STATES[0].getCode() +
            Nucleotides.CANONICAL_STATES[1].getCode() +
            Nucleotides.CANONICAL_STATES[2].getCode() +
            Nucleotides.CANONICAL_STATES[3].getCode();


    private boolean isPurine(int state) {
        return Nucleotides.isPurine(Nucleotides.CANONICAL_STATES[state]);
    }

    public final String getAlphabet() { return residues; }

    public String toString() {
        String result = match + "/" + mismatchTransition;
        if(mismatchTransversion != mismatchTransition) {
            result = result + "/" + mismatchTransversion;
        }
        if(name.length()>  0){
            result = name + " (" + result + ")";
        }
        return result;
    }
}


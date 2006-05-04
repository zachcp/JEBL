package jebl.evolution.align.scores;

import jebl.evolution.sequences.Nucleotides;
import jebl.evolution.sequences.NucleotideState;
import jebl.evolution.sequences.State;

import java.util.List;
import java.util.ArrayList;

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
    private boolean includeAmbiguities;
    private String alphabet =
            Nucleotides.CANONICAL_STATES[0].getCode() +
                    Nucleotides.CANONICAL_STATES[1].getCode() +
                    Nucleotides.CANONICAL_STATES[2].getCode() +
                    Nucleotides.CANONICAL_STATES[3].getCode();

    public NucleotideScores() {
    }

    public NucleotideScores(NucleotideScores scores) {
        alphabet = scores.getAlphabet();
    }

    /**
     * @param match match score
     * @param misMatch mismatch score
     */
    public NucleotideScores(float match, float misMatch) {
        this("", match, misMatch, misMatch);
    }

    public NucleotideScores(float match, float misMatch, float ambiguousMatch) {
        this("", match, misMatch, misMatch, ambiguousMatch);
    }

    public NucleotideScores(String name, float match, float mismatchTransition, float mismatchTransversion) {
        this.name = name;
        buildScores(match, mismatchTransition, mismatchTransversion, 0,false);
    }

    public NucleotideScores(String name, float match, float mismatchTransition, float mismatchTransversion, float ambiguousMatch) {
        this.name = name;
        buildScores(match,mismatchTransition, mismatchTransversion, ambiguousMatch,true);
    }

    void buildScores(float match, float mismatchTransition, float mismatchTransversion, float ambiguousMatch, boolean includeAmbiguities) {

        this.match = match;
        this.mismatchTransition = mismatchTransition;
        this.mismatchTransversion = mismatchTransversion;
        this.includeAmbiguities = includeAmbiguities;

//        final int states = includeAmbiguities? Nucleotides.getStateCount():Nucleotides.getCanonicalStateCount();
        List<NucleotideState> states=new ArrayList<NucleotideState>();
        StringBuilder builder = new StringBuilder();
        for (NucleotideState state : Nucleotides.STATES) {
            if (state.isGap()) continue;
            if (state.isAmbiguous()&& !includeAmbiguities) continue;
            states.add (state);
            builder.append (state.getCode ());
        }
        alphabet = builder.toString();
        int statesCount= states.size ();
        float[][] scores = new float[statesCount][statesCount];
        for (int i = 0; i < statesCount; i++) {
            State state1 = states.get(i);
            for (int j = 0; j < statesCount; j++) {
                State state2 = states.get(j);
                float value;
                if (state1.equals (state2)) {
                    value = match;
                }
                else if (state1.possiblyEqual(state2)) {
                    value = ambiguousMatch;
                }
                else if (
                    (Nucleotides.isPurine(state1) && Nucleotides.isPurine(state2)) ||
                    (Nucleotides.isPyrimidine(state1) && Nucleotides.isPyrimidine(state2)) ) {
                        value = mismatchTransition;
                    }
                else {
                    value = mismatchTransversion;
                }


               /* float val = (i == j) ? match :
                        ((isPurine(i) == isPurine(j)) ? mismatchTransition : mismatchTransversion);
           */
                scores[i][j] = value;
            }
        }
        buildScores(scores);
    }


    private boolean isPurine(int state) {
        return Nucleotides.isPurine(Nucleotides.CANONICAL_STATES[state]);
    }

    public final String getAlphabet() {

        return alphabet+ getExtraResidues ();
    }

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


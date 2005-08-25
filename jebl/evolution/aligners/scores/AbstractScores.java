package jebl.evolution.aligners.scores;

import jebl.evolution.sequences.SequenceType;
import jebl.evolution.sequences.State;

import java.util.List;
import java.util.Set;

public abstract class AbstractScores implements Scores {

    /**
     * This function takes a string which gives the order of amino acids and a lower
     * triangular matrix of scores in the order. It then creates a full score matrix
     * including averaging over ambiguity permutations.
     * @param stateOrder
     * @param similarityScores
     */
    protected final void constructScores(SequenceType sequenceType, String stateOrder, float[][] similarityScores) {

        scores = new float[sequenceType.getStateCount()][sequenceType.getStateCount()];

        int[] stateOrderMap = new int[stateOrder.length()];

        for (int i = 0; i < stateOrder.length(); i++) {
            State state = sequenceType.getState(stateOrder.substring(i, i+1));
            stateOrderMap[state.getIndex()] = i;
        }

        List<State> states = sequenceType.getStates();
        for (State state1 : states) {
            // Get the canonical states for state1 (may be multiple if state1 is ambiguous)
            Set<State> stateSet1 = state1.getCanonicalStates();

            for (State state2 : states) {
                // Get the canonical states for state2 (may be multiple if state2 is ambiguous)
                Set<State> stateSet2 = state2.getCanonicalStates();

                float score = 0.0F;
                int count = 0;

                for (State s1 : stateSet1) {
                    int i1 = stateOrderMap[s1.getIndex()];

                    for (State s2 : stateSet2) {
                        int i2 = stateOrderMap[s2.getIndex()];

                        // similarityScores is a lower triangular matrix...
                        if (i2 < i1) {
                            score += similarityScores[i1][i2];
                        } else {
                            score += similarityScores[i2][i1];
                        }
                        count ++;
                    }
                }

                scores[state1.getIndex()][state2.getIndex()] = score / count;

            }

        }
    }


	public float[][] getScoreMatrix() {
		return scores;
	}

	public float getScore(State state1, State state2) {
	    return scores[state1.getIndex()][state2.getIndex()];
	}

	public float getScore(int stateIndex1, int stateIndex2) {
		return scores[stateIndex1][stateIndex2];
	}

	private float[][] scores;
}

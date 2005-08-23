package jebl.evolution.aligners.scores;

import jebl.evolution.sequences.State;
import jebl.evolution.sequences.SequenceType;

/**
 * This is a simple hamming distance score matrix.
 */
public class Hamming extends AbstractScores {

    public Hamming(SequenceType sequenceType) {

        StringBuffer states = new StringBuffer();
        for (State state : sequenceType.getStates()) {
            states.append(state.getCode());
        }

        String stateString = states.toString();
        float[][] tmpScores = new float[stateString.length()][];
        for (int i = 0; i < stateString.length(); i++) {
            tmpScores[i] = new float[i + 1];
            for (int j = 0; j <= i; j++) {
                if (i == j) {
                    tmpScores[i][j] = 1.0F;
                } else {
                    tmpScores[i][j] = -1.0F;
                }

            }
        }

        constructScores(sequenceType, stateString, tmpScores);
    }

}

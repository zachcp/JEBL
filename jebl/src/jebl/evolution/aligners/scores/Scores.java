package jebl.evolution.aligners.scores;

import jebl.evolution.sequences.State;

public interface Scores {

	float[][] getScoreMatrix();

    float getScore(State state1, State state2);

	float getScore(int stateIndex1, int stateIndex2);
}

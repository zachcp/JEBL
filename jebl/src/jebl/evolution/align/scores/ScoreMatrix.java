package jebl.evolution.align.scores;

/**
 * @author Alexei Drummond
 *
 */
public interface ScoreMatrix {
    /**
     *
     * @return human readable name
     */
    public String getName();

    /**
     * @return the score for matching char x with char y
     */
    float getScore(char x, char y);

    /**
     * @return a string containing the valid characters for this score matrix.
     */
    String getAlphabet();
}

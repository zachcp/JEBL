package jebl.evolution.align.scores;

/**
 * @author Alexei Drummond
 *
 * @version $Id$
 */
public interface ScoreMatrix {

    /**
     * @return the score for matching char x with char y
     */
    float getScore(char x, char y);

    /**
     * @return a string containing the valid characters for this score matrix.
     */
    String getAlphabet();
}

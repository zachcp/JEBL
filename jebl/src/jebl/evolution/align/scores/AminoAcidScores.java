package jebl.evolution.align.scores;

/**
 * @author Alexei Drummond
 *
 * @version $Id$
 */
public class AminoAcidScores extends Scores {

    private String residues = "ARNDCQEGHILKMFPSTWYV";

    public final String getAlphabet() { return residues; }

    public AminoAcidScores() {
    }

    public AminoAcidScores(float m, float n) {
        buildScores(m, n);
    }
}

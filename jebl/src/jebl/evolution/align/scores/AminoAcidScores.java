package jebl.evolution.align.scores;

/**
 * @author Alexei Drummond
 *
 */
public class AminoAcidScores extends Scores {

    private String residues = "ARNDCQEGHILKMFPSTWYV";

    public String getName() {
        return toString();
    }

    public final String getAlphabet() { return residues + getExtraResidues(); }


    public AminoAcidScores() {
    }

    public AminoAcidScores(float m, float n) {
        buildScores(m, n);
    }
}

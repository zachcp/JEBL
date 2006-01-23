package jebl.evolution.align.scores;

/**
 * @author Alexei Drummond
 *
 * @version $Id$
 */
public class AminoAcidScores extends Scores {

    private String residues = "ARNDCQEGHILKMFPSTWYV";

    public final String getAlphabet() { return residues; }

}

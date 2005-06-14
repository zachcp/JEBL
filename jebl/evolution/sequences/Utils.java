package jebl.evolution.sequences;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 *
 * @version $Id$
 */
public class Utils {

	public static State[] translate(State[] nucleotides, GeneticCode geneticCode) {
		State[] translation = new State[nucleotides.length / 3];
		for (int i = 0; i < translation.length; i++) {
			translation[i] = geneticCode.translate(nucleotides[i], nucleotides[i+1], nucleotides[i+2]);
		}
		return translation;
	}

}

/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package jebl.evolution.sequences;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 *
 * @version $Id$
 */
public class Utils {

	public static AminoAcidState[] translate(NucleotideState[] nucleotides, GeneticCode geneticCode) {
		AminoAcidState[] translation = new AminoAcidState[nucleotides.length / 3];
		for (int i = 0; i < translation.length; i++) {
            CodonState state = Codons.getState(nucleotides[i * 3], nucleotides[(i * 3) + 1], nucleotides[(i * 3) + 2]);
			translation[i] = geneticCode.getTranslation(state);
		}
		return translation;
	}

}

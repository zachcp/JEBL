/*
 * Utils.java
 *
 * (c) 2002-2005 JEBL Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.sequences;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 *
 * @version $Id$
 */
public class Utils {

	public static AminoAcidState[] translate(final State[] states, GeneticCode geneticCode) {
		if (states == null) throw new NullPointerException("States array is null");
		if (states.length == 0) return new AminoAcidState[0];

		if (states[0] instanceof NucleotideState) {
			AminoAcidState[] translation = new AminoAcidState[states.length / 3];
			for (int i = 0; i < translation.length; i++) {
				CodonState state = Codons.getState((NucleotideState)states[i * 3],
													(NucleotideState)states[(i * 3) + 1],
													(NucleotideState)states[(i * 3) + 2]);
				translation[i] = geneticCode.getTranslation(state);
			}
			return translation;
		} else if (states[0] instanceof CodonState) {
			AminoAcidState[] translation = new AminoAcidState[states.length];
			for (int i = 0; i < translation.length; i++) {
				translation[i] = geneticCode.getTranslation((CodonState)states[i]);
			}
			return translation;
		} else {
			throw new IllegalArgumentException("Given states are not nucleotides or codons so cannot be translated");
		}
	}

	public static State[] stripGaps(final State[] sequence) {
		int count = 0;
		for (State state : sequence) {
			if (!state.isGap()) {
				count++;
			}
		}

		State[] stripped = new State[count];
		int index = 0;
		for (State state : sequence) {
			if (!state.isGap()) {
				stripped[index] = state;
				index += 1;
			}
		}

		return stripped;
	}

	public static State[] reverse(final State[] sequence) {
		State[] reversed = new State[sequence.length];
		for (int i = 0; i < sequence.length; i++) {
			reversed[i] = sequence[sequence.length - i - 1];
		}
		return reversed;
	}

	public static NucleotideState[] complement(final NucleotideState[] sequence) {
		NucleotideState[] complemented = new NucleotideState[sequence.length];
		for (int i = 0; i < sequence.length; i++) {
			complemented[i] = Nucleotides.COMPLEMENTARY_STATES[sequence[i].getIndex()];
		}
		return complemented;
	}

	public static NucleotideState[] reverseComplement(final NucleotideState[] sequence) {
		NucleotideState[] reverseComplemented = new NucleotideState[sequence.length];
		for (int i = 0; i < sequence.length; i++) {
			reverseComplemented[i] = Nucleotides.COMPLEMENTARY_STATES[sequence[sequence.length - i - 1].getIndex()];
		}
		return reverseComplemented;
	}

}

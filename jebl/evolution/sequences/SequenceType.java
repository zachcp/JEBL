/*
 * SequenceType.java
 *
 * (c) 2002-2005 JEBL Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */

package jebl.evolution.sequences;

import java.util.*;

/**
 * Base class for sequences data types.
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 *
 * @version $Id$
 */
public abstract class SequenceType {

    // FACTORY METHODS

    public final static SequenceType getNucleotides() {
        return NUCLEOTIDE_SEQUENCE;
    }

    public final static SequenceType getAminoAcids() {
        return AMINO_ACID_SEQUENCE;
    }

    public final static SequenceType getCodons() {
        return CODON_SEQUENCE;
    }

    /**
     * Get number of unique states
     *
     * @return number of unique states
     */
    public abstract int getStateCount();

    /**
     * Get number of states including ambiguous states
     *
     * @return number of ambiguous states
     */
    public abstract int getAmbiguousStateCount();

    /**
     * Get a list of states ordered by their indices.
     *
     * @return a list of states
     */
    public abstract List<State> getStates();

    /**
     * Get state corresponding to a string code
     *
     * @param code a string code
     * @return the state
     */
    public abstract State getState(String code);

    /**
     * Get state corresponding to a state index
     *
     * @param index a state index
     * @return the state
     */
    public abstract State getState(int index);

    /**
     * Get state corresponding to an unknown
     *
     * @return the state
     */
    public abstract State getUnknownState();

    /**
     * Get state corresponding to a gap
     *
     * @return state
     */
    public abstract State getGapState();

	/**
	 * @return true if this state is an unknown state
	 */
	public abstract boolean isUnknown(State state);

	/**
	 * @return true if this state is a gap
	 */
	public abstract boolean isGap(State state);

    /**
     * name of data type
     *
     * @return string describing the data type
     */
    public abstract String getName();

	/**
	 * Converts a string of state codes into an array of State objects for this SequenceType
	 * @param sequenceString
	 * @return the State array
	 */
	public abstract State[] toStateArray(String sequenceString);

    /**
     * Converts an array of state indices into an array of State objects for this SequenceType
     * @param indexArray
     * @return the State array
     */
    public abstract State[] toStateArray(int[] indexArray);

    public String toString() {
        return getName();
    }

    private final static SequenceType NUCLEOTIDE_SEQUENCE = new NucleotideSequenceType();
    private final static SequenceType AMINO_ACID_SEQUENCE = new AminoAcidSequenceType();
    private final static SequenceType CODON_SEQUENCE = new CodonSequenceType();

    private static class NucleotideSequenceType extends SequenceType {
        private NucleotideSequenceType() {

            states = Nucleotides.CANONICAL_STATES;
            name = "Nucleotides";

            stateList = Collections.unmodifiableList(Arrays.asList(states));
        }

        public int getStateCount() { return Nucleotides.CANONICAL_STATE_COUNT; }

        public int getAmbiguousStateCount() { return Nucleotides.AMBIGUOUS_STATE_COUNT; }

        public List<State> getStates() { return stateList; }

        public State getState(String code) { return Nucleotides.getState(code); }

        public State getState(int index) { return Nucleotides.getState(index); }

        public State getUnknownState() { return Nucleotides.UNKNOWN_STATE; }

        public State getGapState() { return Nucleotides.GAP_STATE; }

        public boolean isUnknown(State state) { return state == Nucleotides.UNKNOWN_STATE; }

        public boolean isGap(State state) { return state == Nucleotides.GAP_STATE; }

        public String getName() { return name; }

	    public State[] toStateArray(String sequenceString) {
		    State[] seq = new NucleotideState[sequenceString.length()];
		    for (int i = 0; i < seq.length; i++) {
			    seq[i] = Nucleotides.getState(sequenceString.charAt(i));
		    }
		    return seq;
	    }

        public State[] toStateArray(int[] indexArray) {
            State[] seq = new NucleotideState[indexArray.length];
            for (int i = 0; i < seq.length; i++) {
                seq[i] = Nucleotides.getState(indexArray[i]);
            }
            return seq;
        }

        /**
         * This table maps indices (0-17) into state objects.
         */
        private final State[] states;
        private final List<State> stateList;

        private final String name;
    };

    private static class AminoAcidSequenceType extends SequenceType {
        private AminoAcidSequenceType() {
            states = AminoAcids.AMINO_ACID_STATES;
            stateList = Collections.unmodifiableList(Arrays.asList(states));
            name = "Amino Acid";
        }

        public int getStateCount() { return AminoAcids.CANONICAL_STATE_COUNT; }

        public int getAmbiguousStateCount() { return AminoAcids.AMBIGUOUS_STATE_COUNT; }

        public List<State> getStates() { return stateList; }

        public State getState(String code) { return AminoAcids.getState(code.charAt(0)); }

        public State getState(int index) { return AminoAcids.getState(index); }

        public State getUnknownState() { return AminoAcids.UNKNOWN_STATE; }

        public State getGapState() { return AminoAcids.GAP_STATE; }

        public boolean isUnknown(State state) { return state == AminoAcids.UNKNOWN_STATE; }

        public boolean isGap(State state) { return state == AminoAcids.GAP_STATE; }

        public String getName() { return name; }

	    public State[] toStateArray(String sequenceString) {
		    State[] seq = new State[sequenceString.length()];
		    for (int i = 0; i < seq.length; i++) {
			    seq[i] = AminoAcids.getState(sequenceString.charAt(i));
		    }
		    return seq;
	    }

        public State[] toStateArray(int[] indexArray) {
            State[] seq = new State[indexArray.length];
            for (int i = 0; i < seq.length; i++) {
                seq[i] = AminoAcids.getState(indexArray[i]);
            }
            return seq;
        }

        /**
         * This table maps indices (0-17) into state objects.
         */
        private final State[] states;
        private final List<State> stateList;

        private final String name;
    };

    private static class CodonSequenceType extends SequenceType {
        private CodonSequenceType() {
            states = Codons.CODON_STATES;
            stateList = Collections.unmodifiableList(Arrays.asList(states));
            name = "Codon";
        }

        public int getStateCount() { return Codons.CANONICAL_STATE_COUNT; }

        public int getAmbiguousStateCount() { return Codons.AMBIGUOUS_STATE_COUNT; }

        public List<State> getStates() { return stateList; }

        public State getState(String code) { return Codons.getState(code); }

        public State getState(int index) { return Codons.getState(index); }

        public State getUnknownState() { return Codons.UNKNOWN_STATE; }

        public State getGapState() { return Codons.GAP_STATE; }

        public boolean isUnknown(State state) { return state == Codons.UNKNOWN_STATE; }

        public boolean isGap(State state) { return state == Codons.GAP_STATE; }

        public String getName() { return name; }

	    public State[] toStateArray(String sequenceString) {
		    int n = sequenceString.length() / 3;
		    State[] seq = new CodonState[n];
		    for (int i = 0; i < n; i++) {
			    seq[i] = Codons.getState(sequenceString.substring(i * 3, (i * 3) + 3));
		    }
		    return seq;
	    }

        public State[] toStateArray(int[] indexArray) {
            State[] seq = new CodonState[indexArray.length];
            for (int i = 0; i < seq.length; i++) {
                seq[i] = Codons.getState(indexArray[i]);
            }
            return seq;
        }

        private final State[] states;
        private final List<State> stateList;

        private final String name;
    };
}

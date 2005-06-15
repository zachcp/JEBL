/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
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

    public final static SequenceType getDNA() {
        return DNA_SEQUENCE;
    }

    public final static SequenceType getRNA() {
        return RNA_SEQUENCE;
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
    public abstract List getStates();

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

    public String toString() {
        return getName();
    }

    private final static SequenceType DNA_SEQUENCE = new NucleotideSequenceType(false);
    private final static SequenceType RNA_SEQUENCE = new NucleotideSequenceType(true);
    private final static SequenceType AMINO_ACID_SEQUENCE = new AminoAcidSequenceType();
    private final static SequenceType CODON_SEQUENCE = new CodonSequenceType();

    private static class NucleotideSequenceType extends SequenceType {
        private NucleotideSequenceType(boolean isRNA) {

            if (isRNA) {
                states = Nucleotides.RNA_CANONICAL_STATES;
                name = "RNA";
            } else {
                states = Nucleotides.DNA_CANONICAL_STATES;
                name = "DNA";
            }

            stateList = Collections.unmodifiableList(Arrays.asList(states));

            statesByCode = new State[128];
            for (int i = 0; i < states.length; i++) {
                if (i >= 'A' && i <= 'z') {
                    // Undefined letters are mapped to UNKOWN_STATE
                    statesByCode[i] = Nucleotides.UNKNOWN_STATE;
                } else {
                    // Undefined punctuations are mapped to GAP_STATE
                    statesByCode[i] = Nucleotides.GAP_STATE;
                }
            }

            for (int i = 0; i < states.length; i++) {
                State state = (State)getStates().get(i);
                statesByCode[state.getCode().charAt(0)] = state;
                statesByCode[Character.toLowerCase(state.getCode().charAt(0))] = state;
            }

            if (isRNA) {
                statesByCode['T'] = Nucleotides.U_STATE;
                statesByCode['t'] = Nucleotides.U_STATE;
            } else {
                statesByCode['U'] = Nucleotides.T_STATE;
                statesByCode['u'] = Nucleotides.T_STATE;
            }
        }

        public int getStateCount() { return Nucleotides.CANONICAL_STATE_COUNT; }

        public int getAmbiguousStateCount() { return Nucleotides.AMBIGUOUS_STATE_COUNT; }

        public List getStates() { return stateList; }

        public State getState(String code) { return statesByCode[code.charAt(0)]; }

        public State getState(int index) { return states[index]; }

        public State getUnknownState() { return Nucleotides.UNKNOWN_STATE; }

        public State getGapState() { return Nucleotides.GAP_STATE; }

        public boolean isUnknown(State state) { return state == Nucleotides.UNKNOWN_STATE; }

        public boolean isGap(State state) { return state == Nucleotides.GAP_STATE; }

        public String getName() { return name; }

	    public State[] toStateArray(String sequenceString) {
		    State[] states = new State[sequenceString.length()];
		    for (int i = 0; i < states.length; i++) {
			    states[i] = statesByCode[sequenceString.charAt(i)];
		    }
		    return states;
	    }

        /**
         * This table maps indices (0-17) into state objects.
         */
        private final State[] states;
        private final List stateList;

        /**
         * This table maps nucleotide characters into state objects (0-17)
         * Nucleotides go ACGTURYMWSKBDHVN?-", Other letters are mapped to ?.
         * ? and - are mapped to themselves. All other chars are mapped to -.
         */
        private final State[] statesByCode;

        private final String name;
    };

    private static class AminoAcidSequenceType extends SequenceType {
        private AminoAcidSequenceType() {
            states = AminoAcids.AMINO_ACID_STATES;
            stateList = Collections.unmodifiableList(Arrays.asList(states));
            name = "Amino Acid";

            statesByCode = new State[128];
            for (int i = 0; i < states.length; i++) {
                if (i >= 'A' && i <= 'z') {
                    // Undefined letters are mapped to UNKOWN_STATE
                    statesByCode[i] = AminoAcids.UNKNOWN_STATE;
                } else {
                    // Undefined punctuations are mapped to GAP_STATE
                    statesByCode[i] = AminoAcids.GAP_STATE;
                }
            }

            for (int i = 0; i < states.length; i++) {
                State state = (State)getStates().get(i);
                statesByCode[state.getCode().charAt(0)] = state;
                statesByCode[Character.toLowerCase(state.getCode().charAt(0))] = state;
            }

        }

        public int getStateCount() { return AminoAcids.CANONICAL_STATE_COUNT; }

        public int getAmbiguousStateCount() { return AminoAcids.AMBIGUOUS_STATE_COUNT; }

        public List getStates() { return stateList; }

        public State getState(String code) { return statesByCode[code.charAt(0)]; }

        public State getState(int index) { return states[index]; }

        public State getUnknownState() { return AminoAcids.UNKNOWN_STATE; }

        public State getGapState() { return AminoAcids.GAP_STATE; }

        public boolean isUnknown(State state) { return state == AminoAcids.UNKNOWN_STATE; }

        public boolean isGap(State state) { return state == AminoAcids.GAP_STATE; }

        public String getName() { return name; }

	    public State[] toStateArray(String sequenceString) {
		    State[] states = new State[sequenceString.length()];
		    for (int i = 0; i < states.length; i++) {
			    states[i] = statesByCode[sequenceString.charAt(i)];
		    }
		    return states;
	    }

        /**
         * This table maps indices (0-17) into state objects.
         */
        private final State[] states;
        private final List stateList;

        /**
         * This table maps nucleotide characters into state objects (0-17)
         * Nucleotides go ACGTURYMWSKBDHVN?-", Other letters are mapped to ?.
         * ? and - are mapped to themselves. All other chars are mapped to -.
         */
        private final State[] statesByCode;

        private final String name;
    };

    private static class CodonSequenceType extends SequenceType {
        private CodonSequenceType() {
            states = Codons.CODON_STATES;
            stateList = Collections.unmodifiableList(Arrays.asList(states));
            name = "Codon";

            statesByCode = new HashMap();
            for (int i = 0; i < states.length; i++) {
                statesByCode.put(states[i].getCode(), states[i]);
            }
        }

        public int getStateCount() { return Codons.CANONICAL_STATE_COUNT; }

        public int getAmbiguousStateCount() { return Codons.AMBIGUOUS_STATE_COUNT; }

        public List getStates() { return stateList; }

        public State getState(String code) { return (State)statesByCode.get(code); }

        public State getState(int index) { return states[index]; }

        public State getUnknownState() { return Codons.UNKNOWN_STATE; }

        public State getGapState() { return Codons.GAP_STATE; }

        public boolean isUnknown(State state) { return state == Codons.UNKNOWN_STATE; }

        public boolean isGap(State state) { return state == Codons.GAP_STATE; }

        public String getName() { return name; }

	    public State[] toStateArray(String sequenceString) {
		    int n = sequenceString.length() / 3;
		    State[] states = new State[n];
		    for (int i = 0; i < n; i++) {
			    states[i] = getState(sequenceString.substring(i * 3, (i * 3) + 3));
		    }
		    return states;
	    }

        private final State[] states;
        private final List stateList;

        private final Map statesByCode;

        private final String name;
    };
}

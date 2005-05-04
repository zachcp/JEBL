/*
 * DataType.java
 *
 * (c) 2005 JEBL Development Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */

package jebl.evolution.datatypes;

import java.util.*;

/**
 * Base class for sequences data types.
 *
 * @author Andrew Rambaut
 *
 * @version $Id$
 */
public abstract class DataType {

    // FACTORY METHODS

    public final static DataType getDNA() {
        return DNA_DATA_TYPE;
    }

    public final static DataType getRNA() {
        return RNA_DATA_TYPE;
    }

    public final static DataType getAminoAcids() {
        return AMINO_ACID_DATA_TYPE;
    }

    public final static DataType getCodons() {
        return CODON_DATA_TYPE;
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

    public String toString() {
        return getName();
    }

    private final static DataType DNA_DATA_TYPE = new NucleotideDataType(false);
    private final static DataType RNA_DATA_TYPE = new NucleotideDataType(true);
    private final static DataType AMINO_ACID_DATA_TYPE = new AminoAcidDataType();
    private final static DataType CODON_DATA_TYPE = new CodonDataType();

    private static class NucleotideDataType extends DataType {
        private NucleotideDataType(boolean isRNA) {

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

    private static class AminoAcidDataType extends DataType {
        private AminoAcidDataType() {
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

    private static class CodonDataType extends DataType {
        private CodonDataType() {
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

        private final State[] states;
        private final List stateList;

        private final Map statesByCode;

        private final String name;
    };
}

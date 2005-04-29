/*
 * DataType.java
 *
 * (c) 2005 JEBL Development Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */

package jebl.evolution.datatypes;

import dr.evolution.datatype.Nucleotides;
import dr.evolution.datatype.TwoStates;
import dr.evolution.datatype.AminoAcids;

import java.util.*;

/**
 * Base class for sequences data types.
 *
 * @version $Id$
 *
 * @author Andrew Rambaut
 */
public abstract class DataType {

    // FACTORY METHODS

    public final static DataType getDNA() {
        return DNA_DATA_TYPE;
    }

    public final static DataType getRNA() {
        return RNA_DATA_TYPE;
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
     * Get state corresponding to a three-letter code
     *
     * @param code a string code
     * @return the state
     */
    public abstract State getState(String code);

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

    private static class NucleotideDataType extends DataType {
        private NucleotideDataType(boolean isRNA) {

            if (isRNA) {
                states = new State[] {
                    Nucleotides.A_STATE, Nucleotides.C_STATE, Nucleotides.G_STATE, Nucleotides.U_STATE,
                    Nucleotides.R_STATE, Nucleotides.Y_STATE, Nucleotides.M_STATE, Nucleotides.W_STATE,
                    Nucleotides.S_STATE, Nucleotides.K_STATE, Nucleotides.B_STATE, Nucleotides.D_STATE,
                    Nucleotides.H_STATE, Nucleotides.V_STATE, Nucleotides.N_STATE,
                    Nucleotides.UNKNOWN_STATE, Nucleotides.GAP_STATE
                };

                name = "RNA";
            } else {
                states = new State[] {
                    Nucleotides.A_STATE, Nucleotides.C_STATE, Nucleotides.G_STATE, Nucleotides.T_STATE,
                    Nucleotides.R_STATE, Nucleotides.Y_STATE, Nucleotides.M_STATE, Nucleotides.W_STATE,
                    Nucleotides.S_STATE, Nucleotides.K_STATE, Nucleotides.B_STATE, Nucleotides.D_STATE,
                    Nucleotides.H_STATE, Nucleotides.V_STATE, Nucleotides.N_STATE,
                    Nucleotides.UNKNOWN_STATE, Nucleotides.GAP_STATE
                };

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

        public State getState(String code) { return states[code.charAt(0)]; }

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
                states = new State[] {
                    Nucleotides.A_STATE, Nucleotides.C_STATE, Nucleotides.G_STATE, Nucleotides.U_STATE,
                    Nucleotides.R_STATE, Nucleotides.Y_STATE, Nucleotides.M_STATE, Nucleotides.W_STATE,
                    Nucleotides.S_STATE, Nucleotides.K_STATE, Nucleotides.B_STATE, Nucleotides.D_STATE,
                    Nucleotides.H_STATE, Nucleotides.V_STATE, Nucleotides.N_STATE,
                    Nucleotides.UNKNOWN_STATE, Nucleotides.GAP_STATE
                };

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

        }

        public int getStateCount() { return Nucleotides.CANONICAL_STATE_COUNT; }

        public int getAmbiguousStateCount() { return Nucleotides.AMBIGUOUS_STATE_COUNT; }

        public List getStates() { return stateList; }

        public State getState(String code) { return states[code.charAt(0)]; }

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
}

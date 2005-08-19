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
 * Interface for sequences data types.
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 *
 * @version $Id$
 */
public interface SequenceType {

    /**
     * Get number of states including ambiguous states
     *
     * @return number of states
     */
    int getStateCount();

    /**
     * Get a list of states ordered by their indices.
     *
     * @return a list of states
     */
    List<State> getStates();

    /**
     * Get number of canonical states
     *
     * @return number of states
     */
    int getCanonicalStateCount();

    /**
     * Get a list of canonical states ordered by their indices.
     *
     * @return a list of states
     */
    List<State> getCanonicalStates();

    /**
     * Get state corresponding to a string code
     *
     * @param code a string code
     * @return the state
     */
    State getState(String code);

    /**
     * Get state corresponding to a state index
     *
     * @param index a state index
     * @return the state
     */
    State getState(int index);

    /**
     * Get state corresponding to an unknown
     *
     * @return the state
     */
    State getUnknownState();

    /**
     * Get state corresponding to a gap
     *
     * @return state
     */
    State getGapState();

	/**
	 * @return true if this state is an unknown state
	 */
	boolean isUnknown(State state);

	/**
	 * @return true if this state is a gap
	 */
	boolean isGap(State state);

    /**
     * name of data type
     *
     * @return string describing the data type
     */
    String getName();

	/**
	 * Converts a string of state codes into an array of State objects for this SequenceType
	 * @param sequenceString
	 * @return the State array
	 */
	State[] toStateArray(String sequenceString);

    /**
     * Converts an array of state indices into an array of State objects for this SequenceType
     * @param indexArray
     * @return the State array
     */
    State[] toStateArray(int[] indexArray);

	public static final SequenceType NUCLEOTIDE = new SequenceType() {
		public int getStateCount() { return Nucleotides.getStateCount(); }
		public List<State> getStates() { return Nucleotides.getStates(); }
        public int getCanonicalStateCount() { return Nucleotides.getCanonicalStateCount(); }
        public List<State> getCanonicalStates() { return Nucleotides.getCanonicalStates(); }
		public State getState(String code) { return Nucleotides.getState(code); }
		public State getState(int index) { return Nucleotides.getState(index); }
		public State getUnknownState() { return Nucleotides.getUnknownState(); }
		public State getGapState() { return Nucleotides.getGapState(); }
		public boolean isUnknown(State state) { return Nucleotides.isUnknown((NucleotideState)state); }
		public boolean isGap(State state) { return Nucleotides.isGap((NucleotideState)state); }
		public String getName() { return Nucleotides.NAME; }
		public State[] toStateArray(String sequenceString) { return Nucleotides.toStateArray(sequenceString); }
		public State[] toStateArray(int[] indexArray) { return Nucleotides.toStateArray(indexArray); }
	};

	public static final SequenceType AMINO_ACID = new SequenceType() {
        public int getStateCount() { return AminoAcids.getStateCount(); }
        public List<State> getStates() { return AminoAcids.getStates(); }
        public int getCanonicalStateCount() { return AminoAcids.getCanonicalStateCount(); }
        public List<State> getCanonicalStates() { return AminoAcids.getCanonicalStates(); }
		public State getState(String code) { return AminoAcids.getState(code); }
		public State getState(int index) { return AminoAcids.getState(index); }
		public State getUnknownState() { return AminoAcids.getUnknownState(); }
		public State getGapState() { return AminoAcids.getGapState(); }
		public boolean isUnknown(State state) { return AminoAcids.isUnknown((AminoAcidState)state); }
		public boolean isGap(State state) { return AminoAcids.isGap((AminoAcidState)state); }
		public String getName() { return AminoAcids.NAME; }
		public State[] toStateArray(String sequenceString) { return AminoAcids.toStateArray(sequenceString); }
		public State[] toStateArray(int[] indexArray) { return AminoAcids.toStateArray(indexArray); }
	};

	public static final SequenceType CODON = new SequenceType() {
        public int getStateCount() { return Codons.getStateCount(); }
        public List<State> getStates() { return Codons.getStates(); }
        public int getCanonicalStateCount() { return Codons.getCanonicalStateCount(); }
        public List<State> getCanonicalStates() { return Codons.getCanonicalStates(); }
		public State getState(String code) { return Codons.getState(code); }
		public State getState(int index) { return Codons.getState(index); }
		public State getUnknownState() { return Codons.getUnknownState(); }
		public State getGapState() { return Codons.getGapState(); }
		public boolean isUnknown(State state) { return Codons.isUnknown((CodonState)state); }
		public boolean isGap(State state) { return Codons.isGap((CodonState)state); }
		public String getName() { return Codons.NAME; }
		public State[] toStateArray(String sequenceString) { return Codons.toStateArray(sequenceString); }
		public State[] toStateArray(int[] indexArray) { return Codons.toStateArray(indexArray); }
	};
}

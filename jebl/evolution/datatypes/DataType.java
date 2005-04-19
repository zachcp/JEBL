// DataType.java
//
// (c) 2002-2004 BEAST Development Core Team
//
// This package may be distributed under the
/*
 * DataType.java
 *
 * (c) 2002-2005 BEAST Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */

package jebl.evolution.datatypes;

import dr.evolution.datatype.Nucleotides;
import dr.evolution.datatype.TwoStates;
import dr.evolution.datatype.AminoAcids;

/**
 * Base class for sequences data types.
 *
 * @version $Id$
 *
 * @author Andrew Rambaut
 */
public abstract class DataType
{
	public static final int NUCLEOTIDES = 0;
	public static final int AMINO_ACIDS = 1;
	public static final int CODONS = 2;
	public static final int TWO_STATES = 3;
	public static final int GENERAL = 4;

	public static final char UNKNOWN_CHARACTER = '?';
	public static final char GAP_CHARACTER = '-';
	
	protected int stateCount;
	protected int ambiguousStateCount;

	/**
	 * guess data type suitable for a given sequences
	 *
	 * @param sequence a string of symbols representing a molecular sequences of unknown data type.
	 *
	 * @return suitable DataType object
	 */
	public static DataType guessDataType(String sequence)
	{
		// count A, C, G, T, U, N
		long numNucs = 0;
		long numChars = 0;
		long numBins = 0;
		for (int i = 0; i < sequence.length(); i++)
		{
			char c = sequence.charAt(i);
			int s = Nucleotides.INSTANCE.getState(c);
			
			if (s != Nucleotides.UNKNOWN_STATE && s != Nucleotides.GAP_STATE) {
				numNucs++;
			}

			if (c != '-' && c != '?') numChars++;

			if (c == '0' || c == '1') numBins++;
		}

		if (numChars == 0) { numChars = 1; }

		// more than 85 % frequency advocates nucleotide data
		if ((double) numNucs / (double) numChars > 0.85) {
			return Nucleotides.INSTANCE;
		} else if ((double) numBins / (double) numChars > 0.2) {
			return TwoStates.INSTANCE;
		} else {
			return AminoAcids.INSTANCE;
		}
	}

	/**
	 * Get number of unique states
	 *
	 * @return number of unique states
	 */
	public int getStateCount() {
		return stateCount;
	}

	/**
	 * Get number of states including ambiguous states
	 *
	 * @return number of ambiguous states
	 */
	public int getAmbiguousStateCount() {
		return ambiguousStateCount;
	}

	/**
	 * Get state corresponding to a character
	 *
	 * @param c character
	 *
	 * @return state
	 */
	public int getState(char c) {
		return (int)c - 'A';
	}

	/**
	 * Get state corresponding to an unknown
	 *
	 * @return state
	 */
	public int getUnknownState() {
		return stateCount;
	}

	/**
	 * Get state corresponding to a gap
	 *
	 * @return state
	 */
	public int getGapState() {
		return stateCount + 1;
	}

	/**
	 * Get character corresponding to a given state
	 *
	 * @param state state
	 *
	 * return corresponding character
	 */
	public char getChar(int state) {
		return (char)(state + 'A');
	}

	/**
	 * Get triplet string corresponding to a given state
	 *
	 * @param state state
	 *
	 * return corresponding triplet string
	 */
	public String getTriplet(int state)
	{
		return " " + getChar(state) + " ";
	}

	/**
	 * returns an array containing the non-ambiguous states that this state represents.
	 */
	public int[] getStates(int state) {

		int[] states;
		if (!isAmbiguousState(state)) {
			states = new int[1];
			states[0] = state;
		} else {
			states = new int[stateCount];
			for (int i = 0; i < stateCount; i++) {
				states[i] = i;
			}
		}

		return states;
	}

	/**
	 * returns an array containing the non-ambiguous states that this state represents.
	 */
	public boolean[] getStateSet(int state) {
	
		boolean[] stateSet = new boolean[stateCount];
		if (!isAmbiguousState(state)) {
			for (int i = 0; i < stateCount; i++) {
				stateSet[i] = false;
			}
				
			stateSet[state] = true;
		} else {
			for (int i = 0; i < stateCount; i++) {
				stateSet[i] = true;
			}
		}
		
		return stateSet;
	}

	/**
	 * returns the uncorrected distance between two states
	 */
	public double getObservedDistance(int state1, int state2)
	{
		if (!isAmbiguousState(state1) && !isAmbiguousState(state2) && state1 != state2) {
			return 1.0;
		}

		return 0.0;
	}
	
	/**
	 * returns the uncorrected distance between two states with full
	 * treatment of ambiguity.
	 */
	public double getObservedDistanceWithAmbiguity(int state1, int state2)
	{
		boolean[] stateSet1 = getStateSet(state1);
		boolean[] stateSet2 = getStateSet(state2);
		
		double sumMatch = 0.0;
		double sum1 = 0.0;
		double sum2 = 0.0;
		for (int i = 0; i < stateCount; i++) {
			if (stateSet1[i]) {
				sum1 += 1.0;
				if (stateSet1[i] == stateSet2[i]) {
					sumMatch += 1.0;
				}
			} 
			if (stateSet2[i]) {
				sum2 += 1.0;
			} 
		}
		
		double distance = (1.0 - (sumMatch / (sum1 * sum2)));

		return distance;
	}
	
	public String toString() { 
		return getDescription();
	}
	
	/**
	 * description of data type
	 *
	 * @return string describing the data type
	 */
	public abstract String getDescription();

	/**
	 * type of data type
	 *
	 * @return integer code for the data type
	 */
	public abstract int getType();

	/**
	 * @return true if this character is an ambiguous state
	 */
	public boolean isAmbiguousChar(char c) {
		return isAmbiguousState(getState(c));
	}

	/**
	 * @return true if this character is a gap
	 */
	public boolean isUnknownChar(char c) {
		return isUnknownState(getState(c));
	}

	/**
	 * @return true if this character is a gap
	 */
	public boolean isGapChar(char c) {
		return isGapState(getState(c));
	}

	/**
	 * returns true if this state is an ambiguous state.
	 */
	public boolean isAmbiguousState(int state) {
		return (state >= stateCount);
	}

	/**
	 * @return true if this state is an unknown state
	 */
	public boolean isUnknownState(int state) {
		return (state == getUnknownState());
	}
	
	/**
	 * @return true if this state is a gap
	 */
	public boolean isGapState(int state) {
		return (state == getGapState());
	}

}

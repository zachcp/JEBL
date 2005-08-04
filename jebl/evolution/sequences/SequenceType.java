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

}

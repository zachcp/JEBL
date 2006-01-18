/*
 * BasicSequence.java
 *
 * (c) 2002-2005 JEBL Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.sequences;

import jebl.evolution.taxa.Taxon;
import jebl.util.AttributableHelper;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * A default implementation of the Sequence interface.
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 *
 * @version $Id$
 */
public class BasicSequence implements Sequence {

    /**
     * Creates a sequence with a name corresponding to the taxon name
     * @param taxon
     * @param sequenceString
     */
    public BasicSequence(SequenceType sequenceType, Taxon taxon, String sequenceString) {

        if (sequenceType == null) {
            throw new IllegalArgumentException("sequenceType is not allowed to be null");
        }
        if (taxon == null) {
            throw new IllegalArgumentException("taxon is not allowed to be null");
        }
        this.sequenceType = sequenceType;
        this.taxon = taxon;
        this.sequence = new int[sequenceString.length()];
        int k = 0;
        for (int i = 0; i < sequenceString.length(); i++) {
            State state = sequenceType.getState(sequenceString.substring(i, i + 1));

            if (state == null) {
                // Something is wrong. Keep original length by inserting an unknown state
                state = sequenceType.getUnknownState();
            }
            sequence[k] = state.getIndex();
            k++;
        }
    }

    /**
     * Creates a sequence with a name corresponding to the taxon name
     * @param taxon
     * @param sequenceType
     * @param states
     */
    public BasicSequence(SequenceType sequenceType, Taxon taxon, State[] states) {

	    this.sequenceType = sequenceType;
        this.taxon = taxon;
        this.sequence = new int[states.length];
        for (int i = 0; i < sequence.length; i++) {
            sequence[i] = states[i].getIndex();
        }
    }

    /**
     * @return the type of symbols that this sequence is made up of.
     */
    public SequenceType getSequenceType() {
        return sequenceType;
    }

    /**
     * @return a string representing the sequence of symbols.
     */
    public String getString() {
        StringBuffer buffer = new StringBuffer();
        for (int i : sequence) {
            buffer.append(sequenceType.getState(i).getCode());
        }
        return buffer.toString();
    }

    public String getCleanString() {
        StringBuffer buffer = new StringBuffer();
        for (int i : sequence) {
            State state = sequenceType.getState(i);
            if (state.isAmbiguous()|| state.isGap()) continue;
            buffer.append(sequenceType.getState(i).getCode());
        }
        return buffer.toString();
    }

	/**
	 * @return an array of state objects.
	 */
	public State[] getStates() {
	    return sequenceType.toStateArray(sequence);
	}

	public int[] getStateIndices() {
		return sequence;
	}

	/**
	 * @return the state at site.
	 */
	public State getState(int site) {
	    return sequenceType.getState(sequence[site]);
	}

    /**
     * Returns the length of the sequence
     * @return the length
     */
    public int getLength() {
        return sequence.length;
    }

    /**
     * @return that taxon that this sequence represents (primarily used to match sequences with tree nodes)
     */
    public Taxon getTaxon() {
        return taxon;
    }

	/**
	 * Sequences are compared by their taxa
	 * @param o another sequence
	 * @return an integer
	 */
    public int compareTo(Object o) {
        return taxon.compareTo(((Sequence)o).getTaxon());
    }

	// Attributable IMPLEMENTATION

	public void setAttribute(String name, Object value) {
		if (helper == null) {
			helper = new AttributableHelper();
		}
		helper.setAttribute(name, value);
	}

	public Object getAttribute(String name) {
		if (helper == null) {
			return null;
		}
		return helper.getAttribute(name);
	}

	public Set<String> getAttributeNames() {
		if (helper == null) {
			return Collections.emptySet();
		}
		return helper.getAttributeNames();
	}

	public Map<String, Object> getAttributeMap() {
		if (helper == null) {
			return Collections.emptyMap();
		}
		return helper.getAttributeMap();
	}

	private AttributableHelper helper = null;

	// private members

    private final Taxon taxon;
    private final SequenceType sequenceType;
    private final int[] sequence;

	private Map<String, Object>attributeMap = null;
}

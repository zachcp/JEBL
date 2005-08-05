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

import java.util.*;

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

	    this.sequenceType = sequenceType;
        this.taxon = taxon;
        this.sequence = new int[sequenceString.length()];
        for (int i = 0; i < sequence.length; i++) {
            sequence[i] = sequenceType.getState(sequenceString.substring(i, i + 1)).getIndex();
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
        for (int i = 0; i < sequence.length; i++) {
            buffer.append(sequenceType.getState(sequence[i]).getCode());
        }
        return buffer.toString();
    }

	/**
	 * @return an array of state objects.
	 */
	public State[] getStates() {
	    return sequenceType.toStateArray(sequence);
	}

    /**
     * @return the state at site.
     */
    public State getState(int site) {
        return sequenceType.getState(sequence[site]);
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

	// Attributable implementation

	public void setAttribute(String name, Object value) {
		if (attributeMap == null) {
			attributeMap = new HashMap<String, Object>();
		}
		attributeMap.put(name, value);
	}

	public Object getAttribute(String name) {
		if (attributeMap == null) {
			return null;
		}
		return attributeMap.get(name);
	}

	public Set<String> getAttributeNames() {
		if (attributeMap == null) {
			return Collections.emptySet();
		}
		return attributeMap.keySet();
	}

	// private members

    private final Taxon taxon;
    private final SequenceType sequenceType;
    private final int[] sequence;

	private Map<String, Object>attributeMap = null;
}

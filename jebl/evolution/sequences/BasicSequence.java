/*
 * BasicSequence.java
 *
 * (c) 2005 JEBL Development Team
 *
 * This package is distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.sequences;

import jebl.evolution.taxa.Taxon;

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
     * @param sequenceType
     * @param sequenceString
     */
    public BasicSequence(Taxon taxon, SequenceType sequenceType, String sequenceString) {

        this.taxon = taxon;
        this.sequenceType = sequenceType;
        this.sequence = new int[sequenceString.length()];
        for (int i = 0; i < sequence.length; i++) {
            sequence[i] = sequenceType.getState(sequenceString.substring(i, i + 1)).getIndex();
        }
    }

    public BasicSequence(SequenceType sequenceType, Sequence sourceSequence) {

        this(sourceSequence.getTaxon(), sequenceType, Utils.translate((NucleotideState[])sourceSequence.getSequenceStates(), GeneticCode.UNIVERSAL));
    }

    /**
     * Creates a sequence with a name corresponding to the taxon name
     * @param taxon
     * @param sequenceType
     * @param states
     */
    public BasicSequence(Taxon taxon, SequenceType sequenceType, State[] states) {

        this.taxon = taxon;
        this.sequenceType = sequenceType;
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
    public String getSequenceString() {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < sequence.length; i++) {
            buffer.append(sequenceType.getState(sequence[i]).getCode());
        }
        return buffer.toString();
    }

	/**
	 * @return an array of state objects.
	 */
	public State[] getSequenceStates() {
	    return sequenceType.toStateArray(sequence);
	}

    /**
     * @return the state at site.
     */
    public State getState(int site) {
        return null;
    }

    /**
     * @return that taxon that this sequence represents (primarily used to match sequences with tree nodes)
     */
    public Taxon getTaxon() {
        return taxon;
    }

    // private members

    private final Taxon taxon;
    private final SequenceType sequenceType;
    private final int[] sequence;
}

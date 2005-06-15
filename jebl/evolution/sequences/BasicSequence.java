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

    public BasicSequence(String name, Taxon taxon, SequenceType sequenceType, String sequenceString) {

        this.name = name;
        this.taxon = taxon;
        this.sequenceType = sequenceType;
        this.sequenceString = sequenceString;
    }

    /**
     * Creates a sequence with a name corresponding to the taxon name
     * @param taxon
     * @param sequenceType
     * @param sequenceString
     */
    public BasicSequence(Taxon taxon, SequenceType sequenceType, String sequenceString) {

        this.name = taxon.getName();
        this.taxon = taxon;
        this.sequenceType = sequenceType;
        this.sequenceString = sequenceString;
    }

	public BasicSequence(SequenceType sequenceType, Sequence sourceSequence) {

	    this.name = sourceSequence.getName();
	    this.taxon = sourceSequence.getTaxon();
	    this.sequenceType = sequenceType;

		State[] states = sourceSequence.getSequenceStates();

	    State[] translatedStates = Utils.translate(states, GeneticCode.UNIVERSAL);
        StringBuffer buffer = new StringBuffer();
        for (State state : translatedStates) {
            buffer.append(state.getCode());
        }
        sequenceString = buffer.toString();
	}

    /**
     * @return the type of symbols that this sequence is made up of.
     */
    public SequenceType getSequenceType() {
        return sequenceType;
    }

    /**
     * @return a human-readable name for this sequence.
     */
    public String getName() {
        return name;
    }

    /**
     * @return a string representing the sequence of symbols.
     */
    public String getSequenceString() {
        return sequenceString;
    }

	/**
	 * @return an array of state objects.
	 */
	public State[] getSequenceStates() {
	    return sequenceType.toStateArray(sequenceString);
	}

    /**
     * @return that taxon that this sequence represents (primarily used to match sequences with tree nodes)
     */
    public Taxon getTaxon() {
        return taxon;
    }

    // private members

    private final String name;
    private final Taxon taxon;
    private final SequenceType sequenceType;
    private final String sequenceString;
}

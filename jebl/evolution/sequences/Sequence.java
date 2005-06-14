/*
 * Sequence.java
 *
 * (c) 2005 JEBL Development Team
 *
 * This package is distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.sequences;

import jebl.evolution.taxa.Taxon;

/**
 * A biomolecular sequence.
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 *
 * @version $Id$
 */
public interface Sequence {

    /**
     * @return the type of symbols that this sequence is made up of.
     */
    SequenceType getSequenceType();

    /**
     * @return a human-readable name for this sequence.
     */
    String getName();

    /**
     * @return a string representing the sequence of symbols.
     */
    String getSequenceString();

	/**
	 * @return an array of state objects.
	 */
	State[] getSequenceStates();

    /**
     * @return that taxon that this sequence represents (primarily used to match sequences with tree nodes)
     */
    Taxon getTaxon();
}

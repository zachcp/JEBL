/*
 * Pattern.java
 *
 * (c) 2005 JEBL Development Team
 *
 * This package is distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.alignments;

import jebl.evolution.sequences.SequenceType;
import jebl.evolution.sequences.State;
import jebl.evolution.taxa.Taxon;

import java.util.List;

/**
 * An interface representing a list of states for a list of taxa.
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 *
 * @version $Id$
 */
public interface Pattern {

    /**
     * @return the data type of the states in this pattern.
     */
    SequenceType getSequenceType();

    /**
     * @return the list of taxa that the state values correspond to.
     */
    List<Taxon> getTaxa();

    /**
     * @return the list of state values of this pattern.
     */
    List<State> getStates();

	State getMostFrequentState();
}

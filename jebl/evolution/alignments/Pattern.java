/*
 * Pattern.java
 *
 * (c) 2005 JEBL Development Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.alignments;

import jebl.evolution.datatypes.DataType;

import java.util.List;

/**
 * An interface representing a list of states for a list of taxa.
 *
 * @author rambaut
 * @author Alexei Drummond
 *
 * @version $Id$
 */
public interface Pattern {

    /**
     * @return the data type of the states in this pattern.
     */
    DataType getDataType();

    /**
     * @return the list of taxa that the state values correspond to.
     */
    List getTaxa();

    /**
     * @return the list of state values of this pattern.
     */
    List getStates();
}

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
import jebl.evolution.datatypes.DataType;

/**
 * A biomolecular sequence.
 *
 * @author Alexei Drummond
 * @author rambaut
 *
 * @version $Id$
 */
public interface Sequence {

    /**
     * @return the type of symbols that this sequence is made up of.
     */
    DataType getDataType();

    /**
     * @return a human-readable name for this sequence.
     */
    String getName();

    /**
     * @return a string representing the sequence of symbols.
     */
    String getSequenceString();

    /**
     * @return that taxon that this sequence represents (primarily used to match sequences with tree nodes)
     */
    Taxon getTaxon();
}

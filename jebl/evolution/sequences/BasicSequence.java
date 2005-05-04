/*
 * BasicSequence.java
 *
 * (c) 2005 JEBL Development Team
 *
 * This package is distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.sequences;

import jebl.evolution.datatypes.DataType;
import jebl.evolution.taxa.Taxon;

/**
 * @author Alexei Drummond
 * @author rambaut
 *
 * @version $Id$
 */
public class BasicSequence implements Sequence {

    public BasicSequence(String name, Taxon taxon, DataType dataType, String sequenceString) {
        this.name = name;
        this.taxon = taxon;
        this.dataType = dataType;
        this.sequenceString = sequenceString;
    }

    /**
     * @return the type of symbols that this sequence is made up of.
     */
    public DataType getDataType() {
        return dataType;
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
     * @return that taxon that this sequence represents (primarily used to match sequences with tree nodes)
     */
    public Taxon getTaxon() {
        return taxon;
    }

    private final String name;
    private final Taxon taxon;
    private final DataType dataType;
    private final String sequenceString;
}

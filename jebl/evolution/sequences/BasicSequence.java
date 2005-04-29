/*
 * BasicSequence.java
 *
 * (c) 2005 JEBL Development Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.sequences;

import jebl.evolution.datatypes.DataType;
import jebl.evolution.taxa.Taxon;

/**
 * @author rambaut
 *         Date: Apr 7, 2005
 *         Time: 12:23:51 PM
 */
public class BasicSequence implements Sequence {

    public BasicSequence(String name, Taxon taxon, DataType dataType, String sequenceString) {
        this.name = name;
        this.taxon = taxon;
        this.dataType = dataType;
        this.sequenceString = sequenceString;
    }

    public String getName() {
        return name;
    }

    public Taxon getTaxon() {
        return taxon;
    }

    public DataType getDataType() {
        return dataType;
    }

    public String getSequenceString() {
        return sequenceString;
    }

    private final String name;
    private final Taxon taxon;
    private final DataType dataType;
    private final String sequenceString;
}

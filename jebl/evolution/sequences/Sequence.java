/*
 * Sequence.java
 *
 * (c) 2005 JEBL Development Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.sequences;

import jebl.evolution.taxa.Taxon;
import jebl.evolution.datatypes.DataType;

/**
 * @author rambaut
 *         Date: Apr 6, 2005
 *         Time: 4:55:38 PM
 */
public interface Sequence {

    String getName();
    Taxon getTaxon();
    DataType getDataType();

    String getSequenceString();
}

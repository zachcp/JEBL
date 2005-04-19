/*
 * Pattern.java
 *
 * (c) 2002-2005 BEAST Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.alignments;

import jebl.evolution.datatypes.DataType;

import java.util.List;

/**
 * @author rambaut
 *         Date: Apr 7, 2005
 *         Time: 12:45:48 PM
 */
public interface Pattern {

    DataType getDataType();
    List getTaxa();
    List getStates();
}

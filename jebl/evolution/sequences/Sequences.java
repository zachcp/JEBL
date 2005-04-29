/*
 * Sequences.java
 *
 * (c) 2005 JEBL Development Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.sequences;

import jebl.evolution.taxa.Taxon;

import java.util.Set;

/**
 * @author rambaut
 *         Date: Apr 6, 2005
 *         Time: 5:16:08 PM
 */
public interface Sequences {

    Set getSequences();

    Sequence getSequence(Taxon taxon);
}

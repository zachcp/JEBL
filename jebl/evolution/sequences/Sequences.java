/*
 * Sequences.java
 *
 * (c) 2005 JEBL Development Team
 *
 * This package is distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.sequences;

import jebl.evolution.taxa.Taxon;

import java.util.Set;

/**
 * A set of sequence objects.
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 *
 * @version $Id$
 */
public interface Sequences {

    Set<Sequence> getSequences();

    Sequence getSequence(Taxon taxon);
}

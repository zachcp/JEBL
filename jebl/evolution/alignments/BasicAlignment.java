/*
 * BasicAlignment.java
 *
 * (c) 2005 JEBL Development Team
 *
 * This package is distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.alignments;

import jebl.evolution.sequences.Sequence;
import jebl.evolution.taxa.Taxon;

import java.util.*;

/**
 * A basic implementation of the Alignment interface.
 *
 * @author rambaut
 * @author Alexei Drummond
 *
 * @version $Id$
 */
public class BasicAlignment implements Alignment {

    /**
     * Constructs a basic alignment from a set of sequences. The sequence
     * objects are not copied.
     * @param sequences
     */
    public BasicAlignment(Set sequences) {
        Iterator iter = sequences.iterator();
        while (iter.hasNext()) {
            Sequence seq = (Sequence)iter.next();
            this.sequences.put(seq.getTaxon(), seq);
        }
    }

    /**
     * Constructs a basic alignment from an array of sequences. The sequence
     * objects are not copied.
     * @param sequences
     */
    public BasicAlignment(Sequence[] sequences) {
        for (int i = 0; i < sequences.length; i++) {
            this.sequences.put(sequences[i].getTaxon(), sequences[i]);
        }
    }

    public Set getSequences() {
        return sequences.entrySet();
    }

    public Sequence getSequence(Taxon taxon) {
        return (Sequence)sequences.get(taxon);
    }

    public Set getSitePatterns() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public List getSitePatternList() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private Map sequences = new TreeMap();
}

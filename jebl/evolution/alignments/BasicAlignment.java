/*
 * BasicAlignment.java
 *
 * (c) 2002-2005 BEAST Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.alignments;

import jebl.evolution.sequences.Sequence;
import jebl.evolution.taxa.Taxon;

import java.util.*;

/**
 * @author rambaut
 *         Date: Apr 7, 2005
 *         Time: 12:26:39 PM
 */
public class BasicAlignment implements Alignment {

    public BasicAlignment(Set sequences) {
        Iterator iter = sequences.iterator();
        while (iter.hasNext()) {
            Sequence seq = (Sequence)iter.next();
            this.sequences.put(seq.getTaxon(), seq);
        }
    }

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

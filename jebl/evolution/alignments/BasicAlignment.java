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
     * @param seqs
     */
    public BasicAlignment(Set seqs) {
        Iterator iter = seqs.iterator();
        while (iter.hasNext()) {
            Object obj = iter.next();
            if (obj instanceof Sequence) {
                Sequence sequence = (Sequence)obj;
                this.sequences.put(sequence.getTaxon(), sequence);
            } else throw new IllegalArgumentException("Set must only contain sequences.");
        }
    }

    /**
     * Constructs a basic alignment from a sequence. The sequence
     * object is not copied.
     * @param sequence
     */
    public BasicAlignment(Sequence sequence) {
        sequences.put(sequence.getTaxon(), sequence);
    }

    /**
     * Constructs a basic alignment with no sequences.
     */
    public BasicAlignment() {}
       
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

    /**
     * @return a set containing all the sequences in this alignment.
     */
    public <Sequence>Set getSequences() {
        return new HashSet(sequences.values());
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

    /**
     * Adds a sequence to this alignment
     * @param sequence the new sequence.
     */
    public void addSequence(Sequence sequence) {
        sequences.put(sequence.getTaxon(),sequence);
    }

    private Map sequences = new TreeMap();
}

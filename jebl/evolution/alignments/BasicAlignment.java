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
 * @author Andrew Rambaut
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
    public BasicAlignment(Set<Sequence> sequences) {
        for (Sequence sequence : sequences) {
            this.sequences.put(sequence.getTaxon(), sequence);
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
    public Set<Sequence> getSequences() {
        return new HashSet<Sequence>(sequences.values());
    }

    public Sequence getSequence(Taxon taxon) {
        return sequences.get(taxon);
    }

    public List<Pattern> getSitePatterns() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Adds a sequence to this alignment
     * @param sequence the new sequence.
     */
    public void addSequence(Sequence sequence) {
        sequences.put(sequence.getTaxon(), sequence);
    }

    private Map<Taxon, Sequence> sequences = new HashMap<Taxon, Sequence>();
}

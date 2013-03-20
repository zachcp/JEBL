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
import jebl.evolution.sequences.SequenceType;
import jebl.evolution.sequences.State;
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
     * Constructs a basic alignment with no sequences.
     */
    public BasicAlignment() {}

    /**
     * Constructs a basic alignment from a collection of sequences. The sequence
     * objects are not copied.
     * @param sequences
     */
    public BasicAlignment(Collection<? extends Sequence> sequences) {
        for (Sequence sequence : sequences) {
            put(sequence);
        }
        constructPatterns();
    }

    /**
     * Constructs a basic alignment from an array of sequences. The sequence
     * objects are not copied.
     * @param sequences
     */
    public BasicAlignment(Sequence[] sequences) {
        for (Sequence sequence : sequences) {
            put(sequence);
        }
        constructPatterns();
    }

    /**
     * @return a set containing all the sequences in this alignment.
     */
    public Set<Sequence> getSequences() {
        return new LinkedHashSet<Sequence>(sequences.values());
    }

    public List<Sequence> getSequenceList() {
        List<Sequence> seqs = new ArrayList<Sequence>();
        for (Taxon taxon : taxonList) {
            seqs.add(sequences.get(taxon));
        }
        return seqs;
    }

	public SequenceType getSequenceType() {
		return sequenceType;
	}

	public Sequence getSequence(Taxon taxon) {
	    return sequences.get(taxon);
	}

	public int getSiteCount() {
	    return patterns.size();
	}

    public int getPatternCount() {
        return patterns.size();
    }

	public int getPatternLength() {
		return taxonList.size();
	}

    public List<Pattern> getPatterns() {
        return patterns;
    }

	/**
	 * @return the list of taxa that the state values correspond to.
	 */
	public List<Taxon> getTaxa() {
	    return taxonList;
	}

	/**
	 * Adds a sequence to this alignment
	 * @param sequence the new sequence.
	 */
	public void addSequence(Sequence sequence) {
	    put(sequence);
	    constructPatterns();
	}

    private void put(Sequence sequence) {
        if (sequenceType == null) {
            sequenceType = sequence.getSequenceType();
        }
        if (sequenceType != sequence.getSequenceType()) {
            throw new IllegalArgumentException(
                    "Type of sequence " + sequence.getTaxon().getName() +
                            " does not match that of other sequences in the alignment" +
                            " (data type = " + sequence.getSequenceType().getName() +
                            ", but expected " + sequenceType.getName() + ").");
        }

        if( taxonList.indexOf(sequence.getTaxon()) >= 0 ) {
           throw new IllegalArgumentException("duplicate sequence name " + sequence.getTaxon());
        }
        
        sequences.put(sequence.getTaxon(), sequence);
        taxonList.add(sequence.getTaxon());
    }

    private void constructPatterns() {
        patterns.clear();

        int maxLen = 0;
        List<Sequence> sequences = getSequenceList();
        for (Sequence seq : sequences) {
            if (seq.getLength() > maxLen) {
                maxLen = seq.getLength();
            }
        }

        for (int j = 0; j < maxLen; j++) {
            List<State> states = new ArrayList<State>();
            for (Sequence seq : sequences) {
                if (j < seq.getLength()) {
                    states.add(seq.getState(j));
                } else {
                    states.add(sequenceType.getGapState());
                }
            }
            patterns.add(new BasicPattern(states));
        }
    }

    private SequenceType sequenceType = null;
    private List<Taxon> taxonList = new ArrayList<Taxon>();
    private Map<Taxon, Sequence> sequences = new LinkedHashMap<Taxon, Sequence>();
    private List<Pattern> patterns = new ArrayList<Pattern>();

    private class BasicPattern implements Pattern {

        public BasicPattern(List<State> states) {
            this.states = states;
        }

        /**
         * @return the data type of the states in this pattern.
         */
        public SequenceType getSequenceType() {
            return sequenceType;
        }

        public int getLength() {
            return states.size();
        }

        /**
         * @return the list of taxa that the state values correspond to.
         */
        public List<Taxon> getTaxa() {
            return taxonList;
        }

	    public State getState(int index) {
		    return states.get(index);
	    }

	    /**
	     * @return the list of state values of this pattern.
	     */
	    public List<State> getStates() {
	        return states;
	    }

	    /**
	     * @return the set of state values of this pattern.
	     */
	    public Set<State> getStateSet() {
	        return new HashSet<State>(states);
	    }

	    public double getWeight() {
		    return 1.0;
	    }

	    /**
	     * Get the most frequent state in this pattern.
	     * @return the most frequent state
	     */
	    public State getMostFrequentState() {
            return getMostFrequentState(false);
	    }

        /**
         * Get the most frequent state in this pattern.
         * @param includeAmbiguous whether ambiguous states and gaps are included
         * @return the most frequent state
         */
        public State getMostFrequentState(boolean includeAmbiguous) {
            int maxCount = 0;
            State mostFrequentState = null;
            int[] counts = new int[sequenceType.getStateCount()];
            for (State state : states) {
                counts[state.getIndex()] += 1;
                if ((includeAmbiguous || !state.isAmbiguous()) && counts[state.getIndex()] > maxCount) {
                    maxCount = counts[state.getIndex()];
                    mostFrequentState = state;
                }
            }
            return mostFrequentState;
        }

	    public double getStateFrequency(State state) {
		    double count = 0;
		    for (State s : states) {
			    if (s == state) {
				    count += 1;
			    }
		    }
		    return count / states.size();
	    }

	    private final List<State> states;
    }

}

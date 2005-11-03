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
import jebl.evolution.sequences.State;
import jebl.evolution.sequences.SequenceType;
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
    public BasicAlignment(Collection<Sequence> sequences) {
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
        return new HashSet<Sequence>(sequences.values());
    }

    public List<Sequence> getSequenceList() {
        return new ArrayList<Sequence>(sequences.values());
    }

	public SequenceType getSequenceType() {
		return sequenceType;
	}

	public Sequence getSequence(Taxon taxon) {
	    return sequences.get(taxon);
	}

    public List<Pattern> getSitePatterns() {
        return patterns;
    }

	public int getPatternLength() {
		return taxonList.size();
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
        if (sequenceType != sequence.getSequenceType()) throw new IllegalArgumentException("Type of sequence " + sequence.getTaxon().getName() + " does not match that of other sequences in the alignment.");
        sequences.put(sequence.getTaxon(), sequence);
        taxonList.add(sequence.getTaxon());
    }

    private void constructPatterns() {
        patterns.clear();

        State[][] seqs = new State[sequences.size()][];
        int i = 0;
        int maxLen = 0;
        for (Sequence seq : getSequenceList()) {
            seqs[i] = seq.getStates();
            if (seqs[i].length > maxLen) {
                maxLen = seqs[i].length;
            }
            i++;
        }

        for (int j = 0; j < maxLen; j++) {
            List<State> states = new ArrayList<State>();
            for (i = 0; i < seqs.length; i++) {
                if (j < seqs[i].length) {
                    states.add(seqs[i][j]);
                } else {
                    states.add(sequenceType.getGapState());
                }
            }
            patterns.add(new BasicPattern(states));
        }
    }

    private SequenceType sequenceType = null;
    private List<Taxon> taxonList = new ArrayList<Taxon>();
    private Map<Taxon, Sequence> sequences = new HashMap<Taxon, Sequence>();
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
		    int maxCount = 0;
		    State mostFrequentState = null;
		    int[] counts = new int[sequenceType.getStateCount()];
		    for (State state : states) {
				counts[state.getIndex()] += 1;
			    if (counts[state.getIndex()] > maxCount) {
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

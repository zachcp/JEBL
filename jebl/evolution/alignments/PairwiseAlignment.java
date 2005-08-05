/*
 * PairwiseAlignment.java
 *
 * (c) 2002-2005 JEBL Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.alignments;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import jebl.evolution.sequences.Sequence;
import jebl.evolution.taxa.Taxon;

public class PairwiseAlignment implements Alignment {
	
	/*
	 * Constructs a pairwise alignment containing the two given sequences, score, the scoring matrix name
	 * that was used to obtain the alignment and the name of the algorithm used.
	 */
	public PairwiseAlignment(Sequence sequence1, Sequence sequence2, float score, String matrixName) {
		if((sequence1.getString()).length() != (sequence2.getString()).length()) throw new IllegalArgumentException("Sequences must be of same length.");
		addSequence(sequence1);
		addSequence(sequence2);
		this.score = score;
		this.matrixName = matrixName;

        // TODO set the identity of this pairwise alignment
	}
	
	private void addSequence(Sequence sequence) {
		if(sequences.size() > 2) throw new IllegalArgumentException("PairwiseAlignment can only contain 2 sequences.");
        sequences.put(sequence.getTaxon(),sequence);
    }
	
	public Set<Sequence> getSequences() {
		 return new HashSet<Sequence>(sequences.values());
	}
	
	public Sequence getSequence(Taxon taxon) {
        return (Sequence)sequences.get(taxon);
    }
	
	public List<Pattern> getSitePatterns() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
	
	/*
	 * @return the name of the scoring matrix used to obtain the alignment.
	 */
	public String getScoringMatrix() {
		return matrixName;
	}
	
	 /*
	  * @return the percent identity for this pairwise alignment.
	  */
	public float getIdentity() {
		return identity;
	}

	/*
	 * @return the score for this pairwise alignment.
	 */
	public float getScore() {
		return score;
	}
	
	public String toString() {
		String str = "";
		for(Sequence seq : getSequences()) {
			str.concat(seq.getString() + "\n");
		}
		return str;
	}
	
	private Map sequences = new TreeMap();
	private float score;
	private float identity;
	private String matrixName;
}
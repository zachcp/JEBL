package jebl.evolution.align;

/**
 * Shuffles a sequence and aligns it again multiple times to give mean and variance of
 * alignments on random sequences.
 * 
 * @author Richard Moir
 */
public class SequenceShuffler {
	
	private float mean;			//mean score for the set of shuffled alignments
	private float variance;		//variance of the scores for the set of shuffled alignments
	
	public SequenceShuffler() {}
	
	public void shuffle(Align algorithm, String sq1, String sq2, int numShuffles) {
		float[] scores = new float[numShuffles];
		float sumScores = 0;
		String shuffled1 = shuffleSeq(sq1);
		String shuffled2 = shuffleSeq(sq2);
		for(int i = 0; i < numShuffles; i++) {
			shuffled1 = shuffleSeq(shuffled1);
			shuffled2 = shuffleSeq(shuffled2);
			algorithm.doAlignment(shuffled1,shuffled2);
			scores[i] = algorithm.getScore();
			sumScores += algorithm.getScore();
		}
		
		mean = sumScores / numShuffles;
		float sqDiffSum = 0;
		for(int i = 0; i < numShuffles; i++) {
			sqDiffSum += Math.pow(scores[i] - mean, 2);
		}
		variance = sqDiffSum / numShuffles;
	}
	
	/**
	 * Note: not to sure how good this shuffling algorithm is.
	 * 
	 * @param s string to shuffle
	 * @return shuffled string
	 */
	private String shuffleSeq(String s) {
		char[] a = s.toCharArray();
		char swap;
		for (int i = 0; i < a.length; i++) {
			int r = (int) (Math.random() * (i+1));
	        swap = a[r];
	        a[r] = a[i];
	        a[i] = swap;
		}
		return String.valueOf(a);
	}
	
	/**
	 * 
	 * @return the mean score of the shuffled alignments.
	 */
	public float getMean() {
		return mean;
	}
	
	/**
	 * 
	 * @return the variance of scores for the shuffled alignments.
	 */
	public double getStdev() {
		return Math.sqrt(variance);
	}
}
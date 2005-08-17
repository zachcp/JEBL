package jebl.evolution.align.scores;

/**
 * Jukes Cantor assumes equal substitution frequencies and equal nucleotide
 * equilibrium frequencies.
 * 
 * @author Richard Moir
 *
 * @version $Id$
 */
public class JukesCantor extends NucleotideScores {
	
	/**
	 * 
	 * @param d evolutionary distance used to calculate values
	 */
	public JukesCantor(float d) {
		
		double p = (0.25 + 0.75 * Math.exp(-4.0/3.0 * d));	        //diagonal values on the substitution matrix.
		match = (float)(Math.log(p/0.25)/Math.log(2.0)); 			//convert to log-odds (binomial logarithm).
		
		double q = (1.0 - p)/3.0;                                   //off-diagonal values.
		mismatch = (float)(Math.log(q/0.25)/Math.log(2.0));
		
		buildScores(match, mismatch);
	}
}
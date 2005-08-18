package jebl.evolution.align.scores;

import java.lang.reflect.Constructor;

public class ScoresFactory {
	
	/**
	 * For any matrix.
	 * 
	 * @param nameVal name and value of the matrix in String form. (eg Blosum45).
	 * @return	substitution matrix of given name.
	 */
	public static Scores generateScores(String nameVal) {
		int i = 0;
		while((int)nameVal.charAt(i) > 64) { //while(charAt(i) is a letter)
			i++;
		}
		Scores sub = null;
		String name = nameVal.substring(0, i);
		String val = nameVal.substring(i, nameVal.length());
		try {
			if(val.contains("."))
				sub = generateScores(name, Float.parseFloat(val));
			else
				sub = generateScores(name, Integer.parseInt(val));
		}
		catch(Exception e) {
			System.out.println("no such substitution matrix!\n" + e);
		}
		return sub;
	}

	/**
	 * For Blosum and Pam matrices
	 * 
	 * @param name "Blosum" or "Pam"
	 * @param val currently 45 - 90 or 100 - 250
	 * @return	substitution matrix given by name and val.
	 */
	public static Scores generateScores(String name, int val) {
		
		Scores sub = null;
		try {
			Class c = Class.forName("jebl.evolution.align.scores." + name + val);
			sub = (Scores)(c.newInstance());
		}
		catch(Exception e) {
			System.out.println("no such substitution matrix!\n" + e);
		}

		return sub;
		
	}
	
	/**
	 * For calculated nucleotide matrices.
	 * 
	 * @param name Currently only JukesCantor
	 * @param val val used to calculate matrix. eg. evolutionary distance d.
	 * @return substitution matrix calculated using val.
	 */
	public static Scores generateScores(String name, float val) {
		
		Scores sub = null;
		try {
			Class c = Class.forName("jebl.evolution.align.scores." + name);
			Constructor con[] = c.getConstructors();
			sub = (Scores)(con[0].newInstance(new Object[] {new Float(val)}));
		}
		catch(Exception e) {
			System.out.println("no such substitution matrix!\n" + e);
		}

		return sub;
	}
}
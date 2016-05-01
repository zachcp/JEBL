/*
 * IntervalGenerator.java
 *
 * (c) 2005 JEBL Development Team
 *
 * This package is distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.treesimulation;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 */
public interface IntervalGenerator {

	/**
	 * Calculates the waiting time to the next coalescent for a given critical value
	 * (an intensity).
	 * @param lineageCount the number of lineages present
	 * @param currentHeight the starting height
	 * @return the interval time
	 */
    double getInterval(double criticalValue, int lineageCount, double currentHeight);
}

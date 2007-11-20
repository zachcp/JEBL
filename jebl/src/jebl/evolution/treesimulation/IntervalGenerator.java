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
 * @version $Id$
 */
public interface IntervalGenerator {
	/**
	 * Returns the integral of the coalescent intensity from current height to
	 * limitHeight. LimitHeight will be where the number of lineages changes (due
	 * to sampling for example). This function is used to see if the drawn deviate
	 * would go beyond this limit and needs adjusting.
	 *
	 * @param lineageCount  the number of lineages
	 * @param currentHeight the current height in the simulation
	 * @param limitHeight   the limit to which the integral is calculated
	 * @return the intensity
	 */
	double getTotalIntensity(int lineageCount, double currentHeight, double limitHeight);

	/**
	 * Calculates the waiting time to the next coalescent for a given critical value
	 * (an intensity).
	 * @param criticalValue the critical value = -ln(U) where U ~ [0,1]
	 * @param lineageCount the number of lineages present
	 * @param currentHeight the starting height
	 * @return the interval time
	 */
    double getInterval(double criticalValue, int lineageCount, double currentHeight);
}

/*
 * DemographicFunction.java
 *
 * (c) 2002-2005 BEAST Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */

package jebl.evolution.coalescent;

/**
 * This interface provides methods that describe a demographic function.
 *
 * Parts of this class were derived from C++ code provided by Oliver Pybus.
 *
 * @version $Id$
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @author Korbinian Strimmer
 */
public interface DemographicFunction {

	/**
	 * Gets the value of the demographic function N(t) at time t.
	 */
	double getDemographic(double t);

	/**
	 * Returns value of demographic intensity function at time t
	 * (= integral 1/N(x) dx from 0 to t).
	 */
	double getIntensity(double t);

	/**
	 * Returns value of inverse demographic intensity function
	 * (returns time, needed for simulation of coalescent intervals).
	 */
	double getInverseIntensity(double x);

    /**
     * returns whether an analytical expression for the integral is implemented
     * @return a boolean
     */
    boolean hasIntegral();

    /**
     * Calculates the integral 1/N(x) dx between start and finish
     */
    double getIntegral(double start, double finish);

    public static class Utils
	{

		/**
		 * This function tests the consistency of the
		 * getIntensity and getInverseIntensity methods
		 * of this demographic model. If the model is
		 * inconsistent then a RuntimeException will be thrown.
		 * @param demographicFunction the demographic model to test.
		 * @param steps the number of steps between 0.0 and maxTime to test.
		 * @param maxTime the maximum time to test.
		 */
		public static void testConsistency(DemographicFunction demographicFunction, int steps, double maxTime) {

			double delta = maxTime / (double)steps;

			for (int i =0; i <= steps; i++) {
				double time = (double)i * delta;
				double intensity = demographicFunction.getIntensity(time);
				double newTime = demographicFunction.getInverseIntensity(intensity);

				if (Math.abs(time-newTime) > 1e-12) {
					throw new RuntimeException(
						"Demographic model not consistent! error size = " +
						Math.abs(time-newTime));
				}
			}
		}
	};


}
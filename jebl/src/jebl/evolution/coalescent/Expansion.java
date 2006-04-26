/*
 * Expansion.java
 *
 * (c) 2002-2005 BEAST Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */

package jebl.evolution.coalescent;

/**
 * This class models exponential growth from an initial ancestral population size.
 * (Parameters: N0=present-day population size; N1=ancestral population size; r=growth rate).
 * This model is nested with the exponential-growth population size model (N1=0).
 *
 * @version $Id$
 *
 * @author Alexei Drummond
 * @author Andrew Rambaut
 */
public class Expansion extends ExponentialGrowth {

	/**
	 * Construct demographic model with default settings
	 */
	public Expansion() {

		super();
	}

	public double getN1() { return N1; }
	public void setN1(double N1) { this.N1 = N1; }

	public void setProportion(double p) { this.N1 = getN0() * p; }

	// Implementation of abstract methods

	public double getDemographic(double t) {

		double N0 = getN0();
		double N1 = getN1();
		double r = getGrowthRate();

		if (N1 > N0) throw new IllegalArgumentException("N0 must be greater than N1!");

		return N1 + ((N0 - N1) * Math.exp(-r*t));
	}

	/**
	 * Returns value of demographic intensity function at time t
	 * (= integral 1/N(x) dx from 0 to t).
	 */
	public double getIntensity(double t) {

		throw new RuntimeException("Not implemented!");
	}

	public double getInverseIntensity(double x) {

		/* AER - I think this is right but until someone checks it...
		double nZero = getN0();
		double nOne = getN1();
		double r = getGrowthRate();

		if (r == 0) {
			return nZero*x;
		} else if (alpha == 0) {
			return Math.log(1.0+nZero*x*r)/r;
		} else {
			return Math.log(-(nOne/nZero) + Math.exp(nOne*x*r))/r;
		}
		*/
		throw new RuntimeException("Not implemented!");
	}

	public double getIntegral(double start, double finish) {

		return getNumericalIntegral(start, finish);
	}

	public int getNumArguments() {
		return 3;
	}

	public String getArgumentName(int n) {
		switch (n) {
			case 0: return "N0";
			case 1: return "r";
			case 2: return "N1";
		}
		throw new IllegalArgumentException("Argument " + n + " does not exist");
	}

	public double getArgument(int n) {
		switch (n) {
			case 0: return getN0();
			case 1: return getGrowthRate();
			case 2: return getN1();
		}
		throw new IllegalArgumentException("Argument " + n + " does not exist");
	}

	public void setArgument(int n, double value) {
		switch (n) {
			case 0: setN0(value); break;
			case 1: setGrowthRate(value); break;
			case 2: setN1(value); break;
			default: throw new IllegalArgumentException("Argument " + n + " does not exist");

		}
	}

	public double getLowerBound(int n) {
		return 0.0;
	}

	public double getUpperBound(int n) {
		return Double.POSITIVE_INFINITY;
	}

	//
	// private stuff
	//

	private double N1 = 0.0;
}

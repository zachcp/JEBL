/*
 * ExponentialGrowth.java
 *
 * (c) 2002-2005 BEAST Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */

package jebl.evolution.coalescent;

/**
 * This class models an exponentially growing (or shrinking) population
 * (Parameters: N0=present-day population size; r=growth rate).
 * This model is nested with the constant-population size model (r=0).
 *
 * @version $Id$
 *
 * @author Alexei Drummond
 * @author Andrew Rambaut
 */
public class ExponentialGrowth extends ConstantPopulation {

	/**
	 * Construct demographic model with default settings
	 */
	public ExponentialGrowth(double N0, double r) {

		super(N0);
        this.r = r;
    }

	/**
	 * returns growth rate.
	 */
	public final double getGrowthRate() { return r; }

	/**
	 * sets growth rate.
	 */
	public void setGrowthRate(double r) { this.r = r; }

	/**
	 * An alternative parameterization of this model. This
	 * function sets growth rate for a given doubling time.
	 */
	public void setDoublingTime(double doublingTime) {
		setGrowthRate( Math.log(2) / doublingTime );
	}


	// Implementation of abstract methods

	public double getDemographic(double t) {

		double r = getGrowthRate();
		if (r == 0) {
			return getN0();
		} else {
			return getN0() * Math.exp(-t * r);
		}
	}

	public double getIntensity(double t)
	{
		double r = getGrowthRate();
		if (r == 0.0) {
			return t/getN0();
		} else {
			return (Math.exp(t*r)-1.0)/getN0()/r;
		}
	}

	public double getInverseIntensity(double x) {

		double r = getGrowthRate();
		if (r == 0.0) {
			return getN0()*x;
		} else {
			return Math.log(1.0+getN0()*x*r)/r;
		}
	}

	//
	// private stuff
	//

	private double r;
}

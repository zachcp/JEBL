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
	public Expansion(double N0, double r, double N1) {

		super(N0, r);
        this.N1 = N1;
    }

	public double getN1() { return N1; }
	public void setN1(double N1) { this.N1 = N1; }

	public void setProportion(double p) { this.N1 = getN0() * p; }

	// Implementation of abstract methods

	public double getDemographic(double t) {

		double N0 = getN0();
		double N1 = getN1();
		double r = getGrowthRate();

		assert (N1 > N0);

		return N1 + ((N0 - N1) * Math.exp(-r*t));
	}

	public double getIntensity(double t) {
        throw new UnsupportedOperationException();
	}

	public double getInverseIntensity(double x) {
        throw new UnsupportedOperationException();
	}

    public boolean hasIntegral() {
        return false;
    }

	public double getIntegral(double start, double finish) {
        throw new UnsupportedOperationException();
	}

	//
	// private stuff
	//

	private double N1 = 0.0;
}

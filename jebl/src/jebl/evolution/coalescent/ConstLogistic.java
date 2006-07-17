// ConstLogistic.java
//
// (c) 2002-2004 BEAST Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package jebl.evolution.coalescent;

/**
 * This class models logistic growth from an initial population size.
 *
 * @author Alexei Drummond
 * @author Andrew Rambaut
 *
 * @version $Id$
 *
 */
public class ConstLogistic extends LogisticGrowth {

	/**
	 * Construct demographic model with default settings
	 */
	public ConstLogistic(double N0, double r, double c, double N1) {

		super(N0, r, c);
        this.N1 = N1;
    }

	public double getN1() { return N1; }
	public void setN1(double N1) { this.N1 = N1; }

	// Implementation of abstract methods

	public double getDemographic(double t) {

		double nZero = getN0();
		double nOne = getN1();
		double r = getGrowthRate();
		double c = getShape();

		double common = Math.exp(-r*t);
		return nOne + ((nZero - nOne) * (1 + c) * common) / (c + common);
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

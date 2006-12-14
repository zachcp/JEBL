// MachineAccuracy.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package jebl.math;


/**
 * determines machine accuracy
 *
 * @version $Id$
 *
 * @author Korbinian Strimmer
 * @author Alexei Drummond
 */
public class MachineAccuracy
{
	//
	// Public stuff
	//

	/** machine accuracy constant */
	public static double EPSILON = 2.220446049250313E-16;
	
	public static double SQRT_EPSILON = 1.4901161193847656E-8;
	public static double SQRT_SQRT_EPSILON = 1.220703125E-4;

	/** compute EPSILON from scratch */
	public static double computeEpsilon()
	{
		double eps = 1.0;

		while( eps + 1.0 != 1.0 )
		{
			eps /= 2.0;
		}
		eps *= 2.0;
		
		return eps;
	}

	/**
	 * @return true if the relative difference between the two parameters
	 * is no larger than SQRT_EPSILON.
     * (TT: I think this means (1-SQRT_EPSILON) * b <= a <= (1+SQRT_EPSILON) * b )
	 */
	public static boolean same(double a, double b) {
        // the following incorrectly returns false for (0,0)
        //return Math.abs((a/b)-1.0) <= SQRT_EPSILON;
        return ((1-SQRT_EPSILON) * b <= a) && (a <= (1+SQRT_EPSILON) * b);
    }
}

// RateMatrix.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package jebl.evolution.substmodel;

import java.io.*;

import jebl.evolution.sequences.SequenceType;


/**
 * abstract base class for all rate matrices
 *
 * @version $Id$
 *
 * @author Korbinian Strimmer
 * @author Alexei Drummond
 * @author Matthew Goode
 */
public interface RateMatrix extends  Cloneable, Serializable {

    /**
     * @return a short unique human-readable identifier for this rate matrix.
     */
    String getUniqueName();

    /**
     * @return the dimension of this rate matrix.
     */
    int getDimension();

    /**
     * @return stationary frequencies (sum = 1.0)
     */
    double[] getEquilibriumFrequencies();

    /**
     * @return stationary frequency (sum = 1.0) for ith state
     * Preferred method for infrequent use.
     */
    double getEquilibriumFrequency(int i);

    /**
     * Get the data type of this rate matrix
     */
    SequenceType getSequenceType();

    /**
     * @return rate matrix (transition: from 1st index to 2nd index)
     */
    double[][] getRelativeRates();

    /**
     * @return the probability of going from one state to another
     * given the current distance
     * @param fromState The state from which we are starting
     * @param toState The resulting state
     */
    double getTransitionProbability(int fromState, int toState);

    /** A utility method for speed, transfers trans prob information quickly
     *	into store
     */
    void getTransitionProbabilities(double[][] probabilityStore);

    /** Sets the distance (such as time/branch length) used when calculating
     *	the probabilities. This method may well take the most time!
     */
    void setDistance(double distance);

    /** Sets the distance (such as time/branch length) used when calculating
     *	the probabilities. The resulting transition probabilities will be in reverse
     *  (that is in the matrix instead of [from][to] it's [to][from])
     */
    void setDistanceTranspose(double distance);

    public double setParametersNoScale(double[] parameters);

    public void scale(double scaleValue);

}

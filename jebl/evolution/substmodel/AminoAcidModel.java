// AminoAcidModel.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package jebl.evolution.substmodel;

import jebl.evolution.sequences.SequenceType;


/**
 * base class of rate matrices for amino acids
 *
 * @version $Id$
 *
 * @author Korbinian Strimmer
 */
public abstract class AminoAcidModel extends AbstractRateMatrix
{

    //
    // Protected stuff
    //

    // Constructor
    protected AminoAcidModel(double[] f)
    {
        // Dimension = 20
        super(20);

        setSequenceType(SequenceType.AMINO_ACID);
        setFrequencies(f);
    }
}

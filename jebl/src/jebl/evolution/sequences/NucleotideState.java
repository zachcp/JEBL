/*
 * NucleotideState.java
 *
 * (c) 2002-2005 JEBL Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.sequences;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 *
 * @version $Id$
 */
public final class NucleotideState extends State {

    NucleotideState(String name, String stateCode, int index) {
        super(name, stateCode, index);
    }

    NucleotideState(String name, String stateCode, int index, NucleotideState[] ambiguities) {
        super(name, stateCode, index, ambiguities);
    }

    @Override
    public int compareTo(Object o) {
        // throws ClassCastException on across-class comparison
        NucleotideState that = (NucleotideState) o;
        return super.compareTo(that);
    }

    public boolean isGap() {
		return this == Nucleotides.GAP_STATE;
	}

    /**
     * Determine how much in common these potentially ambigous states have as a fraction between 0 and 1
     * 2 non-ambiguous states will return 0.
     * 2 identical non-ambigoues states will 1.
     * R,A = 0.5
     * R,G = 0.5
     * R,M = 0.25
     * @param other another state to compare with
     * @return the fraction of canonical states that the 2 potentially ambiguous states have in common between 0 and 1.
     */
    public double fractionEqual(State other) {
        int totalStates= 0;
        int sameStates = 0;
        if (isGap() || other.isGap()) {
            return 0;
        }
        for (State state : getCanonicalStates()) {
            for (State state1 : other.getCanonicalStates()) {
                totalStates++;
                if (state.equals(state1)) {
                    sameStates++;
                }
            }
        }
        return ((double)sameStates)/totalStates;
    }


}

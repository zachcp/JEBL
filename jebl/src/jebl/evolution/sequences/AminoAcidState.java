/*
 * AminoAcidState.java
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
public final class AminoAcidState extends State {

    AminoAcidState(String name, String stateCode, int index) {
        super(name, stateCode, index);
    }

    AminoAcidState(String name, String stateCode, int index, AminoAcidState[] ambiguities) {
        super(name, stateCode, index, ambiguities);
    }

    public int compareTo(Object o) {
        // throws ClassCastException on across-class comparison
        AminoAcidState that = (AminoAcidState) o;
        return super.compareTo(that);
    }

    public boolean equals(Object o) {
        if (!(o instanceof AminoAcidState))
          return false;
        return super.equals(o);
    }

    public int hashCode() {
        return 7 * super.hashCode() + 23;
    }

    public boolean isGap() {
		return this == AminoAcids.GAP_STATE;
	}

	public boolean isStop() {
		return this == AminoAcids.STOP_STATE;
	}
}

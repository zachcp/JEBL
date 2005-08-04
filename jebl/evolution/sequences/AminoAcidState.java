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

    public AminoAcidState(String name, String stateCode, int index) {
        super(name, stateCode, index);
    }

    public AminoAcidState(String name, String stateCode, int index, AminoAcidState[] ambiguities) {
        super(name, stateCode, index, ambiguities);
    }


	public boolean isGap() {
		return this == AminoAcids.GAP_STATE;
	}
}

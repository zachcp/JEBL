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

    public NucleotideState(String name, String stateCode, int index) {
        super(name, stateCode, index);
    }

    public NucleotideState(String name, String stateCode, int index, NucleotideState[] ambiguities) {
        super(name, stateCode, index, ambiguities);
    }

	public boolean isGap() {
		return this == Nucleotides.GAP_STATE;
	}

}

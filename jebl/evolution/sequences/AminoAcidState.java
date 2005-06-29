/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package jebl.evolution.sequences;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 *
 * @version $Id$
 */
public final class AminoAcidState extends State<AminoAcidState> {

    public AminoAcidState(String name, String stateCode, int index) {
        super(name, stateCode, index);
    }

    public AminoAcidState(String name, String stateCode, int index, AminoAcidState[] ambiguities) {
        super(name, stateCode, index, ambiguities);
    }

}

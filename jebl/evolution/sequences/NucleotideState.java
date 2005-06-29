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
public final class NucleotideState extends State<NucleotideState> {
    
    public NucleotideState(String name, String stateCode, int index) {
        super(name, stateCode, index);
    }

    public NucleotideState(String name, String stateCode, int index, NucleotideState[] ambiguities) {
        super(name, stateCode, index, ambiguities);
    }
    
}

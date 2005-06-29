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
public final class CodonState extends State<CodonState> {
    
    public CodonState(String name, String stateCode, int index) {
        super(name, stateCode, index);
    }

    public CodonState(String name, String stateCode, int index, CodonState[] ambiguities) {
        super(name, stateCode, index, ambiguities);
    }
    
}

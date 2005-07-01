/*
 * CodonState.java
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
public final class CodonState extends State<CodonState> {
    
    public CodonState(String name, String stateCode, int index) {
        super(name, stateCode, index);
    }

    public CodonState(String name, String stateCode, int index, CodonState[] ambiguities) {
        super(name, stateCode, index, ambiguities);
    }
    
}

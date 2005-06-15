/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package jebl.evolution.sequences;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 *
 * @version $Id$
 */
public final class Codons {
    public static final int CANONICAL_STATE_COUNT = 64;
    public static final int AMBIGUOUS_STATE_COUNT = 66;

    public static final State[] CODON_CANONICAL_STATES;
    public static final State[] CODON_STATES;

    // This bit of static code creates the 64 canonical codon states
    static {
        CODON_CANONICAL_STATES = new State[CANONICAL_STATE_COUNT];
        char[] nucs = new char[] { 'A', 'C', 'G', 'T' };
        int x = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < 4; k++) {
                    String code = "" + nucs[i] + nucs[j] + nucs[k];
                    CODON_CANONICAL_STATES[x] = new State(code, code, x);
                    x++;
                }
            }
        }

    }

    public static final State UNKNOWN_STATE = new State("?", "???", 15, CODON_CANONICAL_STATES);
    public static final State GAP_STATE = new State("-", "---", 16, CODON_CANONICAL_STATES);

    public static State getState(State nucleotide1, State nucleotide2, State nucleotide3) {
        String code = nucleotide1.getCode() + nucleotide2.getCode() + nucleotide3.getCode();
        return statesByCode.get(code);
    }

    public static State getState(String code) {
        return statesByCode.get(code);
    }

    public static State getState(int index) {
        return CODON_STATES[index];
    }

    private static final Map<String, State> statesByCode;

    // now create the complete codon state array
    static {
        CODON_STATES = new State[AMBIGUOUS_STATE_COUNT];
        for (int i = 0; i < 64; i++) {
            CODON_STATES[i] = CODON_CANONICAL_STATES[i];
        }
        CODON_STATES[64] = UNKNOWN_STATE;
        CODON_STATES[65] = GAP_STATE;

        statesByCode = new HashMap<String, State>();
        for (int i = 0; i < CODON_STATES.length; i++) {
            statesByCode.put(CODON_STATES[i].getCode(), CODON_STATES[i]);
        }
    }

}

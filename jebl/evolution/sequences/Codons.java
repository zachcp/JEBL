/*
 * Codons.java
 *
 * (c) 2002-2005 JEBL Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
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

    public static final CodonState[] CODON_CANONICAL_STATES;
    public static final CodonState[] CODON_STATES;

    // This bit of static code creates the 64 canonical codon states
    static {
        CODON_CANONICAL_STATES = new CodonState[CANONICAL_STATE_COUNT];
        char[] nucs = new char[] { 'A', 'C', 'G', 'T' };
        int x = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < 4; k++) {
                    String code = "" + nucs[i] + nucs[j] + nucs[k];
                    CODON_CANONICAL_STATES[x] = new CodonState(code, code, x);
                    x++;
                }
            }
        }

    }

    public static final CodonState UNKNOWN_STATE = new CodonState("?", "???", 15, CODON_CANONICAL_STATES);
    public static final CodonState GAP_STATE = new CodonState("-", "---", 16, CODON_CANONICAL_STATES);

    public static CodonState getState(NucleotideState nucleotide1, NucleotideState nucleotide2, NucleotideState nucleotide3) {
        String code = nucleotide1.getCode() + nucleotide2.getCode() + nucleotide3.getCode();
        return statesByCode.get(code);
    }

    public static CodonState getState(String code) {
        return statesByCode.get(code);
    }

    public static CodonState getState(int index) {
        return CODON_STATES[index];
    }

    private static final Map<String, CodonState> statesByCode;

    // now create the complete codon state array
    static {
        CODON_STATES = new CodonState[AMBIGUOUS_STATE_COUNT];
        for (int i = 0; i < 64; i++) {
            CODON_STATES[i] = CODON_CANONICAL_STATES[i];
        }
        CODON_STATES[64] = UNKNOWN_STATE;
        CODON_STATES[65] = GAP_STATE;

        statesByCode = new HashMap<String, CodonState>();
        for (int i = 0; i < CODON_STATES.length; i++) {
            statesByCode.put(CODON_STATES[i].getCode(), CODON_STATES[i]);
        }
    }

}

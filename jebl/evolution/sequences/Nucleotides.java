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
public final class Nucleotides {
    public static final int CANONICAL_STATE_COUNT = 4;
    public static final int AMBIGUOUS_STATE_COUNT = 17;

    public static final NucleotideState A_STATE = new NucleotideState("A", "A", 0);
    public static final NucleotideState C_STATE = new NucleotideState("C", "C", 1);
    public static final NucleotideState G_STATE = new NucleotideState("G", "G", 2);
    public static final NucleotideState T_STATE = new NucleotideState("T", "T", 3);
    public static final NucleotideState U_STATE = new NucleotideState("U", "U", 3);
    public static final NucleotideState R_STATE = new NucleotideState("R", "R", 4, new NucleotideState[] {A_STATE, G_STATE});
    public static final NucleotideState Y_STATE = new NucleotideState("Y", "Y", 5, new NucleotideState[] {C_STATE, T_STATE});
    public static final NucleotideState M_STATE = new NucleotideState("M", "M", 6, new NucleotideState[] {A_STATE, C_STATE});
    public static final NucleotideState W_STATE = new NucleotideState("W", "W", 7, new NucleotideState[] {A_STATE, T_STATE});
    public static final NucleotideState S_STATE = new NucleotideState("S", "S", 8, new NucleotideState[] {C_STATE, G_STATE});
    public static final NucleotideState K_STATE = new NucleotideState("K", "K", 9, new NucleotideState[] {G_STATE, T_STATE});
    public static final NucleotideState B_STATE = new NucleotideState("B", "B", 10, new NucleotideState[] {C_STATE, G_STATE, T_STATE});
    public static final NucleotideState D_STATE = new NucleotideState("D", "D", 11, new NucleotideState[] {A_STATE, G_STATE, T_STATE});
    public static final NucleotideState H_STATE = new NucleotideState("H", "H", 12, new NucleotideState[] {A_STATE, C_STATE, T_STATE});
    public static final NucleotideState V_STATE = new NucleotideState("V", "V", 13, new NucleotideState[] {A_STATE, C_STATE, G_STATE});
    public static final NucleotideState N_STATE = new NucleotideState("N", "N", 14, new NucleotideState[] {A_STATE, C_STATE, G_STATE, T_STATE});

    public static final NucleotideState UNKNOWN_STATE = new NucleotideState("?", "?", 15, new NucleotideState[] {A_STATE, C_STATE, G_STATE, T_STATE});
    public static final NucleotideState GAP_STATE = new NucleotideState("-", "-", 16, new NucleotideState[] {A_STATE, C_STATE, G_STATE, T_STATE});

    public static final NucleotideState[] DNA_CANONICAL_STATES = new NucleotideState[] {
            A_STATE, C_STATE, G_STATE, T_STATE
    };

    public static final NucleotideState[] RNA_CANONICAL_STATES = new NucleotideState[] {
            A_STATE, C_STATE, G_STATE, U_STATE
    };

    public static final NucleotideState[] DNA_STATES = new NucleotideState[] {
        A_STATE, C_STATE, G_STATE, T_STATE,
        R_STATE, Y_STATE, M_STATE, W_STATE,
        S_STATE, K_STATE, B_STATE, D_STATE,
        H_STATE, V_STATE, N_STATE, UNKNOWN_STATE, GAP_STATE
    };

    public static final NucleotideState[] RNA_STATES = new NucleotideState[] {
        A_STATE, C_STATE, G_STATE, U_STATE,
        R_STATE, Y_STATE, M_STATE, W_STATE,
        S_STATE, K_STATE, B_STATE, D_STATE,
        H_STATE, V_STATE, N_STATE, UNKNOWN_STATE, GAP_STATE
    };

    public static NucleotideState getState(char code) {
        return statesByCode[code];
    }

    public static NucleotideState getState(String code) {
        return statesByCode[code.charAt(0)];
    }

    public static NucleotideState getState(int index) {
        return DNA_STATES[index];
    }

    private static final NucleotideState[] statesByCode;

    static {
        statesByCode = new NucleotideState[128];
        for (int i = 0; i < DNA_STATES.length; i++) {
            if (i >= 'A' && i <= 'z') {
                // Undefined letters are mapped to UNKOWN_STATE
                statesByCode[i] = Nucleotides.UNKNOWN_STATE;
            } else {
                // Undefined punctuations are mapped to GAP_STATE
                statesByCode[i] = Nucleotides.GAP_STATE;
            }
        }

        for (NucleotideState state : DNA_STATES) {
            statesByCode[state.getCode().charAt(0)] = state;
            statesByCode[Character.toLowerCase(state.getCode().charAt(0))] = state;
        }
    }
}

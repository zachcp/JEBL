/*
 * Nucleotides.java
 *
 * (c) 2005 JEBL Development Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.datatypes;

/**
 * @author rambaut
 *         Date: Apr 29, 2005
 *         Time: 12:05:51 PM
 */
public final class Nucleotides {
    public static final int CANONICAL_STATE_COUNT = 4;
    public static final int AMBIGUOUS_STATE_COUNT = 17;

    public static final State A_STATE = new State("A", "A", 0);
    public static final State C_STATE = new State("C", "C", 1);
    public static final State G_STATE = new State("G", "G", 2);
    public static final State T_STATE = new State("T", "T", 3);
    public static final State U_STATE = new State("U", "U", 3);
    public static final State R_STATE = new State("R", "R", 4, new State[] {A_STATE, G_STATE});
    public static final State Y_STATE = new State("Y", "Y", 5, new State[] {C_STATE, T_STATE});
    public static final State M_STATE = new State("M", "M", 6, new State[] {A_STATE, C_STATE});
    public static final State W_STATE = new State("W", "W", 7, new State[] {A_STATE, T_STATE});
    public static final State S_STATE = new State("S", "S", 8, new State[] {C_STATE, G_STATE});
    public static final State K_STATE = new State("K", "K", 9, new State[] {G_STATE, T_STATE});
    public static final State B_STATE = new State("B", "B", 10, new State[] {C_STATE, G_STATE, T_STATE});
    public static final State D_STATE = new State("D", "D", 11, new State[] {A_STATE, G_STATE, T_STATE});
    public static final State H_STATE = new State("H", "H", 12, new State[] {A_STATE, C_STATE, T_STATE});
    public static final State V_STATE = new State("V", "V", 13, new State[] {A_STATE, C_STATE, G_STATE});
    public static final State N_STATE = new State("N", "N", 14, new State[] {A_STATE, C_STATE, G_STATE, T_STATE});

    public static final State UNKNOWN_STATE = new State("?", "?", 15, new State[] {A_STATE, C_STATE, G_STATE, T_STATE});
    public static final State GAP_STATE = new State("-", "-", 16, new State[] {A_STATE, C_STATE, G_STATE, T_STATE});

    public static final State[] DNA_CANONICAL_STATES = new State[] {
            A_STATE, C_STATE, G_STATE, T_STATE
    };

    public static final State[] RNA_CANONICAL_STATES = new State[] {
            A_STATE, C_STATE, G_STATE, U_STATE
    };

    public static final State[] DNA_STATES = new State[] {
        A_STATE, C_STATE, G_STATE, T_STATE,
        R_STATE, Y_STATE, M_STATE, W_STATE,
        S_STATE, K_STATE, B_STATE, D_STATE,
        H_STATE, V_STATE, N_STATE, UNKNOWN_STATE, GAP_STATE
    };

    public static final State[] RNA_STATES = new State[] {
        A_STATE, C_STATE, G_STATE, U_STATE,
        R_STATE, Y_STATE, M_STATE, W_STATE,
        S_STATE, K_STATE, B_STATE, D_STATE,
        H_STATE, V_STATE, N_STATE, UNKNOWN_STATE, GAP_STATE
    };



}

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
public final class AminoAcids {
    public static final int CANONICAL_STATE_COUNT = 20;
    public static final int AMBIGUOUS_STATE_COUNT = 25;

    public static final AminoAcidState A_STATE = new AminoAcidState("A", "A", 0);
    public static final AminoAcidState C_STATE = new AminoAcidState("C", "C", 1);
    public static final AminoAcidState D_STATE = new AminoAcidState("D", "D", 2);
    public static final AminoAcidState E_STATE = new AminoAcidState("E", "E", 3);
    public static final AminoAcidState F_STATE = new AminoAcidState("F", "F", 4);
    public static final AminoAcidState G_STATE = new AminoAcidState("G", "G", 5);
    public static final AminoAcidState H_STATE = new AminoAcidState("H", "H", 6);
    public static final AminoAcidState I_STATE = new AminoAcidState("I", "I", 7);
    public static final AminoAcidState K_STATE = new AminoAcidState("K", "K", 8);
    public static final AminoAcidState L_STATE = new AminoAcidState("L", "L", 9);
    public static final AminoAcidState M_STATE = new AminoAcidState("M", "M", 10);
    public static final AminoAcidState N_STATE = new AminoAcidState("N", "N", 11);
    public static final AminoAcidState P_STATE = new AminoAcidState("P", "P", 12);
    public static final AminoAcidState Q_STATE = new AminoAcidState("Q", "Q", 13);
    public static final AminoAcidState R_STATE = new AminoAcidState("R", "R", 14);
    public static final AminoAcidState S_STATE = new AminoAcidState("S", "S", 15);
    public static final AminoAcidState T_STATE = new AminoAcidState("T", "T", 16);
    public static final AminoAcidState V_STATE = new AminoAcidState("V", "V", 17);
    public static final AminoAcidState W_STATE = new AminoAcidState("W", "W", 18);
    public static final AminoAcidState Y_STATE = new AminoAcidState("Y", "Y", 19);

    public static final AminoAcidState[] CANONICAL_STATES = new AminoAcidState[] {
            A_STATE, C_STATE, D_STATE, E_STATE, F_STATE,
            G_STATE, H_STATE, I_STATE, K_STATE, L_STATE,
            M_STATE, N_STATE, P_STATE, Q_STATE, R_STATE,
            S_STATE, T_STATE, V_STATE, W_STATE, Y_STATE
    };

    public static final AminoAcidState B_STATE = new AminoAcidState("B", "B", 20, new AminoAcidState[] {D_STATE, N_STATE});
    public static final AminoAcidState Z_STATE = new AminoAcidState("Z", "Z", 21, new AminoAcidState[] {E_STATE, Q_STATE});
    public static final AminoAcidState X_STATE = new AminoAcidState("X", "X", 22, CANONICAL_STATES);
    public static final AminoAcidState UNKNOWN_STATE = new AminoAcidState("?", "?", 23, CANONICAL_STATES);
    public static final AminoAcidState STOP_STATE = new AminoAcidState("*", "*", 24, CANONICAL_STATES);
    public static final AminoAcidState GAP_STATE = new AminoAcidState("-", "-", 25, CANONICAL_STATES);

    public static final AminoAcidState[] AMINO_ACID_STATES = new AminoAcidState[] {
            A_STATE, C_STATE, D_STATE, E_STATE, F_STATE,
            G_STATE, H_STATE, I_STATE, K_STATE, L_STATE,
            M_STATE, N_STATE, P_STATE, Q_STATE, R_STATE,
            S_STATE, T_STATE, V_STATE, W_STATE, Y_STATE,
            B_STATE, Z_STATE, X_STATE, UNKNOWN_STATE,
            STOP_STATE, GAP_STATE
    };


    /**
     * This character represents the amino acid equivalent of a stop codon to cater for
     * situations arising from converting coding DNA to an amino acid sequences.
     */
    /**
     * A table to map state numbers (0-25) to their three letter codes.
     */
    private static final String[] AMINOACID_TRIPLETS = {
    //		A		C		D		E		F		G		H		I		K
          "Ala",  "Cys",  "Asp",  "Glu",  "Phe",  "Gly",  "His",  "Ile",  "Lys",
    //		L		M		N		P		Q		R		S		T		V
          "Leu",  "Met",  "Asn",  "Pro",  "Gln",  "Arg",  "Ser",  "Thr",  "Val",
    //		W		Y		B		Z		X		*		?		-
          "Trp",  "Tyr",  "Asx",  "Glx",  " X ",  " * ",  " ? ",  " - "
    };

    public static AminoAcidState getState(char code) {
        return statesByCode[code];
    }

    public static AminoAcidState getState(String code) {
        return statesByCode[code.charAt(0)];
    }

    public static AminoAcidState getState(int index) {
        return AMINO_ACID_STATES[index];
    }

    private static final AminoAcidState[] statesByCode;

    static {
        statesByCode = new AminoAcidState[128];
        for (int i = 0; i < AMINO_ACID_STATES.length; i++) {
            if (i >= 'A' && i <= 'z') {
                // Undefined letters are mapped to UNKOWN_STATE
                statesByCode[i] = AminoAcids.UNKNOWN_STATE;
            } else {
                // Undefined punctuations are mapped to GAP_STATE
                statesByCode[i] = AminoAcids.GAP_STATE;
            }
        }

        for (AminoAcidState state : AMINO_ACID_STATES) {
            statesByCode[state.getCode().charAt(0)] = state;
            statesByCode[Character.toLowerCase(state.getCode().charAt(0))] = state;
        }
    }
}

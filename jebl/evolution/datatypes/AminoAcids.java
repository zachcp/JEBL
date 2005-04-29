/*
 * AminoAcids.java
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
public final class AminoAcids {
    public static final int CANONICAL_STATE_COUNT = 20;
    public static final int AMBIGUOUS_STATE_COUNT = 25;

    public static final State A_STATE = new State("A", "A", 0);
    public static final State C_STATE = new State("C", "C", 1);
    public static final State D_STATE = new State("D", "D", 2);
    public static final State E_STATE = new State("E", "E", 3);
    public static final State F_STATE = new State("F", "F", 4);
    public static final State G_STATE = new State("G", "G", 5);
    public static final State H_STATE = new State("H", "H", 6);
    public static final State I_STATE = new State("I", "I", 7);
    public static final State K_STATE = new State("K", "K", 8);
    public static final State L_STATE = new State("L", "L", 9);
    public static final State M_STATE = new State("M", "M", 10);
    public static final State N_STATE = new State("N", "N", 11);
    public static final State P_STATE = new State("P", "P", 12);
    public static final State Q_STATE = new State("Q", "Q", 13);
    public static final State R_STATE = new State("R", "R", 14);
    public static final State S_STATE = new State("S", "S", 15);
    public static final State T_STATE = new State("T", "T", 16);
    public static final State V_STATE = new State("V", "V", 17);
    public static final State W_STATE = new State("W", "W", 18);
    public static final State Y_STATE = new State("Y", "Y", 19);

    public static final State[] CANONICAL_STATES = new State[] {
            A_STATE, C_STATE, D_STATE, E_STATE, F_STATE,
            G_STATE, H_STATE, I_STATE, K_STATE, L_STATE,
            M_STATE, N_STATE, P_STATE, Q_STATE, R_STATE,
            S_STATE, T_STATE, V_STATE, W_STATE, Y_STATE
    };

    public static final State B_STATE = new State("B", "B", 20, new State[] {D_STATE, N_STATE});
    public static final State Z_STATE = new State("Z", "Z", 21, new State[] {E_STATE, Q_STATE});
    public static final State X_STATE = new State("X", "X", 22, CANONICAL_STATES);
    public static final State UNKNOWN_STATE = new State("?", "?", 23, CANONICAL_STATES);
    public static final State GAP_STATE = new State("-", "-", 24, CANONICAL_STATES);

    public static final State[] AMINO_ACID_STATES = new State[] {
            A_STATE, C_STATE, D_STATE, E_STATE, F_STATE,
            G_STATE, H_STATE, I_STATE, K_STATE, L_STATE,
            M_STATE, N_STATE, P_STATE, Q_STATE, R_STATE,
            S_STATE, T_STATE, V_STATE, W_STATE, Y_STATE,
            B_STATE, Z_STATE, X_STATE, UNKNOWN_STATE, GAP_STATE
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

}

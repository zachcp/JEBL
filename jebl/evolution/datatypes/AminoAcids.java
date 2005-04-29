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
    public static final State H_STATE = new State("H", "H", 12, new State[] {A_STATE, C_STATE, T_STATE});
    public static final State V_STATE = new State("V", "V", 13, new State[] {A_STATE, C_STATE, G_STATE});
    public static final State N_STATE = new State("N", "N", 14, new State[] {A_STATE, C_STATE, G_STATE, T_STATE});

    public static final State UNKNOWN_STATE = new State("?", "?", 15, new State[] {A_STATE, C_STATE, G_STATE, T_STATE});
    public static final State GAP_STATE = new State("-", "-", 16, new State[] {A_STATE, C_STATE, G_STATE, T_STATE});


    public static final String DESCRIPTION = "amino acid";

    /**
     * The only instance of the AminoAcids class.
     */
    public static final AminoAcids INSTANCE = new AminoAcids();

    public static final int STATE_COUNT = 20;
    public static final int AMBIGUOUS_STATE_COUNT = 25;

    /**
     * This character represents the amino acid equivalent of a stop codon to cater for
     * situations arising from converting coding DNA to an amino acid sequences.
     */
    public static final char STOP_CHARACTER = '*';

    public static final byte STOP_STATE = 23;

    /**
     * This state represents a amino acid residue of unknown type.
     */
    public static final byte UNKNOWN_STATE = 24;

    /**
     * This state represents a gap in an amino acid sequences.
     */
    public static final byte GAP_STATE = 25;

    /**
     * Unique integer identifier for the amino acid data type.
     */
    public static final byte AMINOACIDS = 1;

    /**
     * A table to translate state numbers (0-25) into one letter codes.
     */
    public static final char[] AMINOACID_CHARS= {
        'A','C','D','E','F','G','H','I','K','L','M','N','P','Q','R',
        'S','T','V','W','Y','B','Z','X',AminoAcids.STOP_CHARACTER,DataType.UNKNOWN_CHARACTER,DataType.GAP_CHARACTER
    };

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

    /**
     * This table maps amino acid characters into state codes (0-25).
     * Amino Acids go ACDEFGHIKLMNPQRSTVWYBZX*?-,
     * Other letters; j, o, and u are mapped to ?
     * *, ? and - are mapped to themselves
     * All other chars are mapped to -
     */
    public static final byte[] AMINOACID_STATES = {
        25,25,25,25,25,25,25,25,25,25,25,25,25,25,25,25,	// 0-15
        25,25,25,25,25,25,25,25,25,25,25,25,25,25,25,25,	// 16-31
    //                                 *        -
        25,25,25,25,25,25,25,25,25,25,23,25,25,25,25,25,	// 32-47
    //                                                ?
        25,25,25,25,25,25,25,25,25,25,25,25,25,25,25,24,	// 48-63
    //		A  B  C  D  E  F  G  H  I  j  K  L  M  N  o
        25, 0,20, 1, 2, 3, 4, 5, 6, 7,24, 8, 9,10,11,24,	// 64-79
    //	 P  Q  R  S  T  u  V  W  X  Y  Z
        12,13,14,15,16,24,17,18,22,19,21,25,25,25,25,25,	// 80-95
    //		A  B  C  D  E  F  G  H  I  j  K  L  M  N  o
        25, 0,20, 1, 2, 3, 4, 5, 6, 7,24, 8, 9,10,11,24,	// 96-111
    //	 P  Q  R  S  T  u  V  W  X  Y  Z
        12,13,14,15,16,24,17,18,22,19,21,25,25,25,25,25		// 112-127
    };

    /**
     * A table to map state numbers (0-25) to their ambiguities.
     */
    private static final String[] AMINOACID_AMBIGUITIES = {
    //	   A	C	 D	  E	   F	G	 H	  I	   K
          "A", "C", "D", "E", "F", "G", "H", "I", "K",
    //	   L	M	 N	  P	   Q	R	 S	  T	   V
          "L", "M", "N", "P", "Q", "R", "S", "T", "V",
    //	   W	Y	 B	   Z
          "W", "Y", "DN", "EQ",
    //	   X					   *	?						-
          "ACDEFGHIKLMNPQRSTVWY", "*", "ACDEFGHIKLMNPQRSTVWY", "ACDEFGHIKLMNPQRSTVWY"
    };




}

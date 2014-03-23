/*
 * GeneticCode.java
 *
 * (c) 2002-2005 JEBL Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */

package jebl.evolution.sequences;

import jebl.util.MaybeBoolean;

import java.util.*;

/**
 * A set of standard genetic codes.
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 *
 * @version $Id$
 */

public final class GeneticCode {
    private final Map<CodonState, AminoAcidState> translationMap;
    private final Map<CodonState, AminoAcidState> firstCodonTranslationMap;

    private static int[] ncbiToJeblTranslationIndex;
    static {
        ncbiToJeblTranslationIndex = new int[64];
        for(int i=0;i<4;i++) {
            for(int j=0;j<4;j++) {
                for(int k=0;k<4;k++) {
                    int ncbiIndex = i*16+j*4+k;
                    int jeblIndex=ncbiToJeblNucleotideIndex(i)*16+ncbiToJeblNucleotideIndex(j)*4+ncbiToJeblNucleotideIndex(k);
                    ncbiToJeblTranslationIndex[ncbiIndex]=jeblIndex;
                }
            }
        }
    }

    private static int ncbiToJeblNucleotideIndex(int nucleotideIndex) {
        if (nucleotideIndex==0)
            return 3;//T
        if (nucleotideIndex==1)
            return 1;//C
        if (nucleotideIndex==2)
            return 0;//A
        if (nucleotideIndex==3)
            return 2;//G
        throw new IllegalArgumentException();
    }

    public static final GeneticCode
            UNIVERSAL = new GeneticCode("universal", "Standard", "KNKNTTTTRSRSIIMIQHQHPPPPRRRRLLLLEDEDAAAAGGGGVVVV*Y*YSSSS*CWCLFLF", 1, reorderTranslation("---M---------------M---------------M----------------------------")),
            VERTEBRATE_MT = new GeneticCode("vertebrateMitochondrial", "Vertebrate Mitochondrial", "KNKNTTTT*S*SMIMIQHQHPPPPRRRRLLLLEDEDAAAAGGGGVVVV*Y*YSSSSWCWCLFLF", 2,reorderTranslation("--------------------------------MMMM---------------M------------")),
            YEAST = new GeneticCode("yeast", "Yeast Mitochondrial",  "KNKNTTTTRSRSMIMIQHQHPPPPRRRRTTTTEDEDAAAAGGGGVVVV*Y*YSSSSWCWCLFLF", 3,reorderTranslation("----------------------------------MM----------------------------")),
            MOLD_PROTOZOAN_MT = new GeneticCode("moldProtozoanMitochondrial", "Mold Protozoan Mitochondrial", "KNKNTTTTRSRSIIMIQHQHPPPPRRRRLLLLEDEDAAAAGGGGVVVV*Y*YSSSSWCWCLFLF", 4,reorderTranslation("--MM---------------M------------MMMM---------------M------------")),
            MYCOPLASMA = new GeneticCode("mycoplasma", "Mycoplasma", "KNKNTTTTRSRSIIMIQHQHPPPPRRRRLLLLEDEDAAAAGGGGVVVV*Y*YSSSSWCWCLFLF",-1,reorderTranslation("--MM---------------M------------MMMM---------------M------------")), // this code isn't defined at NCBI, so I used table 4 translations from http://www.ncbi.nlm.nih.gov/Taxonomy/Utils/wprintgc.cgi which has Mycoplasma in it's name. I guess those 2 tables were merged at some point
            INVERTEBRATE_MT = new GeneticCode("invertebrateMitochondrial", "Invertebrate Mitochondrial", "KNKNTTTTSSSSMIMIQHQHPPPPRRRRLLLLEDEDAAAAGGGGVVVV*Y*YSSSSWCWCLFLF", 5,reorderTranslation("---M----------------------------MMMM---------------M------------")),
            CILIATE = new GeneticCode("ciliate", "Ciliate", "KNKNTTTTRSRSIIMIQHQHPPPPRRRRLLLLEDEDAAAAGGGGVVVVQYQYSSSS*CWCLFLF", 6,reorderTranslation("-----------------------------------M----------------------------")),
            ECHINODERM_MT = new GeneticCode("echinodermMitochondrial", "Echinoderm Mitochondrial", "NNKNTTTTSSSSIIMIQHQHPPPPRRRRLLLLEDEDAAAAGGGGVVVV*Y*YSSSSWCWCLFLF", 9,reorderTranslation("-----------------------------------M---------------M------------")),
            EUPLOTID_NUC = new GeneticCode("euplotidNuclear", "Euplotid Nuclear", "KNKNTTTTRSRSIIMIQHQHPPPPRRRRLLLLEDEDAAAAGGGGVVVV*Y*YSSSSCCWCLFLF", 10,reorderTranslation("-----------------------------------M----------------------------")),
            BACTERIAL = new GeneticCode("bacterial", "Bacterial", "KNKNTTTTRSRSIIMIQHQHPPPPRRRRLLLLEDEDAAAAGGGGVVVV*Y*YSSSS*CWCLFLF", 11,reorderTranslation("---M---------------M------------MMMM---------------M------------")),
            ALT_YEAST = new GeneticCode("alternativeYeast", "Alternative Yeast", "KNKNTTTTRSRSIIMIQHQHPPPPRRRRLLSLEDEDAAAAGGGGVVVV*Y*YSSSS*CWCLFLF", 12,reorderTranslation("-------------------M---------------M----------------------------")),
            ASCIDIAN_MT = new GeneticCode("ascidianMitochondrial", "Ascidian Mitochondrial", "KNKNTTTTGSGSMIMIQHQHPPPPRRRRLLLLEDEDAAAAGGGGVVVV*Y*YSSSSWCWCLFLF", 13,reorderTranslation("---M------------------------------MM---------------M------------")),
            FLATWORM_MT = new GeneticCode("flatwormMitochondrial", "Flatworm Mitochondrial", "NNKNTTTTSSSSIIMIQHQHPPPPRRRRLLLLEDEDAAAAGGGGVVVVYY*YSSSSWCWCLFLF", 14,reorderTranslation("-----------------------------------M----------------------------")),
            BLEPHARISMA_NUC = new GeneticCode("blepharismaNuclear", "Blepharisma Nuclear", "KNKNTTTTRSRSIIMIQHQHPPPPRRRRLLLLEDEDAAAAGGGGVVVV*YQYSSSS*CWCLFLF", 15,reorderTranslation("-----------------------------------M----------------------------")),
            CHLOROPHYCEAN_MITOCHONDRIAL = new GeneticCode("chlorophyceanMitochondrial", "Chlorophycean Mitochondrial", reorderTranslation("FFLLSSSSYY*LCC*WLLLLPPPPHHQQRRRRIIIMTTTTNNKKSSRRVVVVAAAADDEEGGGG"), 16,reorderTranslation("-----------------------------------M----------------------------")),
            TREMATODE_MITOCHONDRIAL = new GeneticCode("trematodeMitochondrial", "Trematode Mitochondrial", reorderTranslation("FFLLSSSSYY**CCWWLLLLPPPPHHQQRRRRIIMMTTTTNNNKSSSSVVVVAAAADDEEGGGG"), 21,reorderTranslation("-----------------------------------M---------------M------------")),
            SCENEDESMUS_OBLIQUUS_MITOCHONDRIAL = new GeneticCode("scenedesmusObliquusMitochondrial", "Scenedesmus Obliquus Mitochondrial", reorderTranslation("FFLLSS*SYY*LCC*WLLLLPPPPHHQQRRRRIIIMTTTTNNKKSSRRVVVVAAAADDEEGGGG"), 22,reorderTranslation("-----------------------------------M----------------------------")),
            THRAUSTOCHYTRIUM_MITOCHONDRIAL = new GeneticCode("thraustochytriumMitochondrial", "Thraustochytrium Mitochondrial", reorderTranslation("FF*LSSSSYY**CC*WLLLLPPPPHHQQRRRRIIIMTTTTNNKKSSRRVVVVAAAADDEEGGGG"), 23,reorderTranslation("--------------------------------M--M---------------M------------")),
            PTEROBRANCHIA_MITOCHONDRIAL = new GeneticCode("pterobranchiaMitochondrial", "Pterobranchia Mitochondrial", reorderTranslation("FFLLSSSSYY**CCWWLLLLPPPPHHQQRRRRIIIMTTTTNNKKSSSKVVVVAAAADDEEGGGG"), 24,reorderTranslation("---M---------------M---------------M---------------M------------")),
            CANDIDATE_DIVISION_SR1_AND_GRACILIBACTERIA = new GeneticCode("candidateDivisionSR1andGracilibacteria", "Candidate Division SR1 and Gracilibacteria", reorderTranslation("FFLLSSSSYY**CCGWLLLLPPPPHHQQRRRRIIIMTTTTNNKKSSRRVVVVAAAADDEEGGGG"), 25,reorderTranslation("---M-------------------------------M---------------M------------"));

    private static final List<GeneticCode> GENETIC_CODES_LIST = Collections.unmodifiableList(Arrays.asList(
            UNIVERSAL, VERTEBRATE_MT, YEAST, MOLD_PROTOZOAN_MT, MYCOPLASMA, INVERTEBRATE_MT,
            CILIATE, ECHINODERM_MT, EUPLOTID_NUC, BACTERIAL, ALT_YEAST, ASCIDIAN_MT,
            FLATWORM_MT, BLEPHARISMA_NUC,CHLOROPHYCEAN_MITOCHONDRIAL,TREMATODE_MITOCHONDRIAL,SCENEDESMUS_OBLIQUUS_MITOCHONDRIAL,THRAUSTOCHYTRIUM_MITOCHONDRIAL,
            PTEROBRANCHIA_MITOCHONDRIAL,CANDIDATE_DIVISION_SR1_AND_GRACILIBACTERIA
    ));

    private static List<GeneticCode> customGeneticCodes = Collections.emptyList();

    public static List<GeneticCode> getCustomGeneticCodes() {
        return Collections.unmodifiableList(customGeneticCodes);
    }

    public static void setCustomGeneticCodes(List<GeneticCode> geneticCodes) {
        customGeneticCodes = new ArrayList<GeneticCode>(geneticCodes);
    }

    private static List<GeneticCode> getGeneticCodesList() {
        List<GeneticCode> geneticCodes = new ArrayList<GeneticCode>(GENETIC_CODES_LIST);
        geneticCodes.addAll(getCustomGeneticCodes());
        return geneticCodes;
    }

    public static List<GeneticCode> getStandardGeneticCodes() {
        return GENETIC_CODES_LIST;
    }

    private static String reorderTranslation(String translation) {
        // jebls ordering of nucleotides is different to that used by NCBI at http://www.ncbi.nlm.nih.gov/Taxonomy/Utils/wprintgc.cgi
        char[] result = new char[64];
        if (translation.length()!=64)
            throw new IllegalArgumentException();

        for(int i=0;i<64;i++) {
            int jeblIndex = ncbiToJeblTranslationIndex[i];
            result[jeblIndex]=translation.charAt(i);
        }
        return new String(result);
    }

    /**
     * Returns an iterable that allows you to iterate over all the standard genetic codes
     * @return An iterable over the genetic codes
     */
    public static Iterable<GeneticCode> getGeneticCodes() {
        return getGeneticCodesList();
    }

    public static GeneticCode[] getGeneticCodesArray() {
        return getGeneticCodesList().toArray(new GeneticCode[getGeneticCodesList().size()]);
    }

    /**
     * Use of this field is deprecated because being an array it is mutable, i.e. an attacker could
     * potentially replace values in this array.
     * @deprecated use {@link #getGeneticCodes()} instead
     */
    @Deprecated
    public static final GeneticCode[] GENETIC_CODES = GENETIC_CODES_LIST.toArray(new GeneticCode[GENETIC_CODES_LIST.size()]);

    private int ncbiTranslationTableNumber;
    private final Set<CodonState> startCodons;
    private final String name, description, codeTable, firstCodonTranslationTable;

    /**
     * @param name the name of the genetic code
     * @return the genetic code such that {@link #getDescription()} equals name
     */
    public static GeneticCode valueOf(String name) {
        for(GeneticCode code : getGeneticCodesList()) {
            if(code.getDescription().equals(name)){
                return code;
            }
        }
        return null;
    }

    /**
     * @param NCBITranslationTableNumber the name of the genetic code
     * @return the genetic code such that {@link #getNcbiTranslationTableNumber()} equals NCBITranslationTableNumber
     */
    public static GeneticCode valueOf(int NCBITranslationTableNumber) {
        for(GeneticCode code : GENETIC_CODES_LIST) {
            if(code.getNcbiTranslationTableNumber() == NCBITranslationTableNumber){
                return code;
            }
        }
        return null;
    }

    /**
     * Constructs a new GeneticCode.
     * @param name Name of the genetic code (from GENBANK)
     * @param description Description of the genetic code (from GENBANK)
     * @param codeTable A length-64 string of uppercase amino acid characters (see {@link jebl.evolution.sequences.AminoAcids#getState(char)}),
     *        each character representing the translation of one triplet, with the triplet translations being in the order
     *        AAA, AAC, AAG, AAT, ACA etc. (i.e. first codon position is most significant, and nucleotides come in the
     *        order A, C, G, T) (Note: This is not the order used by the Genbank website).
     * @param ncbiTranslationTableNumber the number used by NCBI to represent this genetic code or -1 if none. Eg. 1 = Standard...
     */
    public GeneticCode(final String name, final String description, final String codeTable, int ncbiTranslationTableNumber, String firstCodonTranslationTable) {
        this.name = name;
        this.description = description;
        this.codeTable = codeTable;
        this.firstCodonTranslationTable = firstCodonTranslationTable;
        this.ncbiTranslationTableNumber = ncbiTranslationTableNumber;

        if (codeTable.length() != 64) {
            throw new IllegalArgumentException("Code Table length does not match number of codon states");
        }

        this.translationMap = createTranslationMap(codeTable);
        this.startCodons = new HashSet<CodonState>();
        StringBuilder fullFirstCodonTranslationMap = new StringBuilder();
        for(int i=0;i<firstCodonTranslationTable.length();i++) {
            char c = firstCodonTranslationTable.charAt(i);
            if (c !='-') {
                fullFirstCodonTranslationMap.append(c);
                CodonState state = Codons.getState(i);
                startCodons.add(state);
            }
            else {
                fullFirstCodonTranslationMap.append(codeTable.charAt(i));
            }
        }
        this.firstCodonTranslationMap = createTranslationMap(fullFirstCodonTranslationMap.toString());
    }

    private static Map<CodonState, AminoAcidState> createTranslationMap(String codeTable) {
        Map<CodonState, AminoAcidState> translationMap = new TreeMap<CodonState, AminoAcidState>();
        for (int i = 0; i < codeTable.length(); i++) {
            CodonState codonState = Codons.CANONICAL_STATES[i];
            AminoAcidState aminoAcidState = AminoAcids.getState(codeTable.substring(i, i + 1));
            translationMap.put(codonState, aminoAcidState);
        }
        translationMap.put(Codons.getGapState(), AminoAcids.getGapState());
        translationMap.put(Codons.getUnknownState(), AminoAcids.getUnknownState());
        translationMap = Collections.unmodifiableMap(translationMap);
        return translationMap;
    }

    /**
	 * Returns the name of the genetic code
     * @return the name of this genetic code
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the description of the genetic code
     * @return the description of this genetic code
	 */
	public String getDescription() {
		return description;
	}

    /**
     * Returns a length-64 string that for each nucleotide triplet contains the single-character
     * amino acid code (see {@link AminoAcids} to which that triplet is translated in this genetic code.
     * @return the string passed to the constructor as the <code>codeTable</code> argument.
     */
    public String getCodeTable() {
        return codeTable;
    }

    /**
     * Returns a length-64 string that for each nucleotide triplet contains the single-character
     * amino acid code (see {@link AminoAcids} to which that triplet is translated in this genetic code when it occurs as the first codon
     * @return the string passed to the constructor as the <code>firstCodonTranslationTable</code> argument.
     */
    public String getCodeTableForFirstCodon() {
        return firstCodonTranslationTable;
    }

    /**
     * equivalent to {@link #getTranslation(CodonState, boolean) getTranslation(codonState,false)}
     */
    public AminoAcidState getTranslation(CodonState codonState) {
        return getTranslation(codonState, false);
    }
	/**
	 * Returns the state associated with AminoAcid represented by codonState.
	 * Note that the state is the canonical state (generated combinatorially)
     * @param isFirstCodon true to get the translation if these nucleotides are the first codon in a CDS (for some codons at the start of the CDS, they will translate to M even though they would translate to something different if in the middle of the CDS)
	 * @see AminoAcids
	 * @see Codons
	 * @return '?' if codon unknown
	 */
	public AminoAcidState getTranslation(CodonState codonState, boolean isFirstCodon) {
        //System.out.println(codonState.getCode());
        if (isFirstCodon)
            return firstCodonTranslationMap.get(codonState);
        else
            return translationMap.get(codonState);
	}

    /**
     * equivalent to {@link #getTranslation(NucleotideState, NucleotideState, NucleotideState, boolean) getTranslation(nucleotide1,nucleotide2,nucleotide3,false)}
     */
    public AminoAcidState getTranslation(NucleotideState nucleotide1, NucleotideState nucleotide2, NucleotideState nucleotide3){
        return getTranslation(nucleotide1, nucleotide2, nucleotide3, false);
    }
    /**
	 * Returns the state associated with AminoAcid represented by the three nucleotides.
	 * If one or more of the nucleotides are ambiguous, and all combinations translate to the
     * same protein, then this method will return that protein
     * @param isFirstCodon true to get the translation if these nucleotides are the first codon in a CDS (for some codons at the start of the CDS, they will translate to M even though they would translate to something different if in the middle of the CDS)
	 * @see AminoAcids
	 * @see Codons
	 * @return '?' if codon unknown
	 */
    public AminoAcidState getTranslation(NucleotideState nucleotide1, NucleotideState nucleotide2, NucleotideState nucleotide3, boolean isFirstCodon){
        Map<CodonState, AminoAcidState> translationMap = isFirstCodon?this.firstCodonTranslationMap:this.translationMap;
        CodonState translateState = null;
        if (nucleotide1.isGap() && nucleotide2.isGap() && nucleotide3.isGap()) {
			translateState = Codons.GAP_STATE;
		}

		if (nucleotide1.isAmbiguous() || nucleotide2.isAmbiguous() || nucleotide3.isAmbiguous()) {
            for(State a : nucleotide1.getCanonicalStates()){
                for(State b : nucleotide2.getCanonicalStates()){
                    for(State c : nucleotide3.getCanonicalStates()){
                        CodonState thisDisambiguation = Codons.getState(a.getCode() + b.getCode() + c.getCode());
                        //initial setup
                        if(translateState == null)
                            translateState = thisDisambiguation;
                        // If different nucleotide disambiguations yield different amino acids, translation is unknown
                        if(!translationMap.get(translateState).equals(translationMap.get(thisDisambiguation)))
                            return translationMap.get(Codons.UNKNOWN_STATE);
                    }
                }
            }
            return translationMap.get(translateState);
        } else {
            String code = nucleotide1.getCode() + nucleotide2.getCode() + nucleotide3.getCode();
            translateState = Codons.getState(code);
            return translationMap.get(translateState);
        }
    }

    /**
     * Similar to {@link #getTranslation(NucleotideState, NucleotideState, NucleotideState, boolean)}
     * except instead of returning ambiguous states we return all possible translations when the ambiguities are resolbes
     * same protein, then this method will return that protein
     * @param isFirstCodon true to get the translation if these nucleotides are the first codon in a CDS (for some codons at the start of the CDS, they will translate to M even though they would translate to something different if in the middle of the CDS)
     * @return all possible translations
     */
   public Set<AminoAcidState> getTranslations(NucleotideState nucleotide1, NucleotideState nucleotide2, NucleotideState nucleotide3, boolean isFirstCodon){
        Map<CodonState, AminoAcidState> translationMap = isFirstCodon?this.firstCodonTranslationMap:this.translationMap;
        if (nucleotide1.isGap() && nucleotide2.isGap() && nucleotide3.isGap()) {
			return Collections.singleton(AminoAcids.GAP_STATE);
		}

		if (nucleotide1.isAmbiguous() || nucleotide2.isAmbiguous() || nucleotide3.isAmbiguous()) {
            Set<AminoAcidState> states = new LinkedHashSet<AminoAcidState>();
            for(State a : nucleotide1.getCanonicalStates()){
                for(State b : nucleotide2.getCanonicalStates()){
                    for(State c : nucleotide3.getCanonicalStates()){
                        CodonState thisDisambiguation = Codons.getState(a.getCode() + b.getCode() + c.getCode());
                        states.add(translationMap.get(thisDisambiguation));
                    }
                }
            }
            return states;
        } else {
            String code = nucleotide1.getCode() + nucleotide2.getCode() + nucleotide3.getCode();
            CodonState translateState = Codons.getState(code);
            return Collections.singleton(translationMap.get(translateState));
        }
    }

    /**
     * Equivalent to {@link #getTranslation(String, boolean) getTranslation(nucleotides, false)}
	 */
    public AminoAcidState getTranslation(String nucleotides) {
        return getTranslation(nucleotides, false);
    }

    /**
	 * Returns the state associated with AminoAcid represented by the three nucleotides.
	 * If one or more of the nucleotides are ambiguous, and all combinations translate to the
     * same protein, then this method will return that protein
     * @param nucleotides a string consisting of exactly 3 residues in any case.
     * @param isFirstCodon true to get the translation if these nucleotides are the first codon in a CDS (for some codons at the start of the CDS, they will translate to M even though they would translate to something different if in the middle of the CDS)
	 * @see AminoAcids
	 * @see Codons
	 * @return '?' if codon unknown
	 */
    public AminoAcidState getTranslation(String nucleotides, boolean isFirstCodon) {
        if (nucleotides.length()!=3) throw new IllegalArgumentException("getTranslation requires a nucleotide triplet. (given "+nucleotides.length()+" characters)");
        NucleotideState n1=Nucleotides.getState(nucleotides.charAt(0));
        NucleotideState n2=Nucleotides.getState(nucleotides.charAt(1));
        NucleotideState n3=Nucleotides.getState(nucleotides.charAt(2));
        if (n1==null) {
            n1=Nucleotides.UNKNOWN_STATE;
        }
        if (n2==null) {
            n2=Nucleotides.UNKNOWN_STATE;
        }
        if (n3==null) {
            n3=Nucleotides.UNKNOWN_STATE;
        }
        return getTranslation(n1,n2,n3, isFirstCodon);
    }

    /**
     * Extracts the three nucleotide or ambiguity states from a nucleotide triplet string
     * @param tripletString The string to be checked
     * @return an array containing the three NucleotideStates corresponding to the tripletString
     * @throws IllegalArgumentException if tripletString doesn't consist of 3 nucleotide or ambiguity symbols
     * @throws NullPointerException if tripletString is null
     */
    private State[] getTripletStates(String tripletString) throws IllegalArgumentException {
        boolean isValidTriplet = (tripletString.length() == 3);
        State[] states = new State[3];
        for (int i = 0; i < 3; i++) {
            states[i] = Nucleotides.getState(tripletString.charAt(i));
            isValidTriplet &= (states[i] != null);
        }
        if (!isValidTriplet) {
            throw new IllegalArgumentException("Expected valid nucleotide triplet, got '" + tripletString + "'");
        } else {
            return states;
        }
    }

    /**
     * Checks whether all possible disambiguations of a given nucleotide triplet
     * string represents a start codon.
     *
     * @param tripletString A string of length 3, with each character representing one nucleotide or ambiguity symbol
     * @return Whether all possible disambiguations of tripletString represent a start codon.
     * @throws IllegalArgumentException if tripletString doesn't consist of 3 nucleotide or ambiguity symbols
     * @throws NullPointerException if tripletString is null
     */
    public MaybeBoolean isStartCodonString(String tripletString) throws IllegalArgumentException{
        State[] states = getTripletStates(tripletString);
        boolean startFound = false, nonStartFound = false;
        for (State a : states[0].getCanonicalStates()) {
            for (State b : states[1].getCanonicalStates()) {
                for (State c : states[2].getCanonicalStates()) {
                    CodonState codonState = Codons.getState(a.getCode() + b.getCode() + c.getCode());
                    boolean isStart = startCodons.contains(codonState);
                    startFound = (startFound || isStart);
                    nonStartFound = (nonStartFound || !isStart);
                    // IntelliJ 6.0.5 claims the following expression is always false, but this is not true.
                    if (startFound && nonStartFound) {
                        return MaybeBoolean.Maybe;
                    }
                }
            }
        }
        return startFound ? MaybeBoolean.True : MaybeBoolean.False;
    }

    /**
     * As of 2007-07-30, {@link jebl.evolution.sequences.CodonState}s exist only
     * for nonambiguous nucleotide triplets. Therefore, this method cannot be
     * used to check if an ambiguous triplet of nucleotides codes for a transcription,
     * start and therefore this method is deprecated.
     * @param codonState
     * @return True if the specified codonState codes for a transcription start under this genetic code.
     */
    @Deprecated
    public boolean isStartCodon(CodonState codonState) {
        return isStartCodonString(codonState.getCode()) == MaybeBoolean.True;
    }

    /**
     * As of 2007-07-30, {@link jebl.evolution.sequences.CodonState}s exist only
     * for nonambiguous nucleotide triplets. Therefore, this method cannot be
     * used to check if an ambiguous triplet of nucleotides codes for a stop,
     * and therefore this method is deprecated.
     * @param codonState
     * @return True if the specified codonState codes for a stop under this genetic code.
     */
    @Deprecated
    public boolean isStopCodon(CodonState codonState) {
        return isStopCodonString(codonState.getCode()) == MaybeBoolean.True;
    }

    /**
	 * Checks whether a given String represents a stop codon.
     * @param tripletString A string of length 3, with each character representing one nucleotide or ambiguity symbol
     * @return true if tripletString represents a stop codon.
     * @throws IllegalArgumentException if tripletString doesn't consist of 3 nucleotide or ambiguity symbols
     * @throws NullPointerException if tripletString is null
	 */
	public MaybeBoolean isStopCodonString(String tripletString) throws IllegalArgumentException {
        State[] states = getTripletStates(tripletString);
        boolean stopFound = false, nonStopFound = false;
        // For non-ambiguous states, each of these loops will be over a single element
        for (State a : states[0].getCanonicalStates()) {
            for (State b : states[1].getCanonicalStates()) {
                for (State c : states[2].getCanonicalStates()) {
                    CodonState codonState = Codons.getState(a.getCode() + b.getCode() + c.getCode());
                    boolean isStop = translationMap.get(codonState).equals(AminoAcids.STOP_STATE);
                    stopFound = (stopFound || isStop);
                    nonStopFound = (nonStopFound || !isStop);
                    // IntelliJ 6.0.5 claims the following expression is always false, but this is not true.
                    if (stopFound && nonStopFound) {
                        return MaybeBoolean.Maybe;
                    }
                }
            }
        }
        return stopFound ? MaybeBoolean.True : MaybeBoolean.False;
    }

	/**
	 * @return all the possible codons for a given amino acid
	 */
	public Set<CodonState> getCodonsForAminoAcid(AminoAcidState aminoAcidState) {
        Set<CodonState> aaSet = new HashSet<CodonState>();
        for (CodonState state : translationMap.keySet()) {
            if (translationMap.get(state) == aminoAcidState) {
                aaSet.add(state);
            }
        }
        return aaSet;
	}

    /**
     * @return the codon states of starts
     */
    public Set<CodonState> getStartCodons() {
        return Collections.unmodifiableSet(startCodons);
    }

    /**
	 * @return the codon states of stops.
	 */
	public Set<CodonState> getStopCodons() {
        Set<CodonState> stopSet = new HashSet<CodonState>();
        for (CodonState state : translationMap.keySet()) {
            if (isStopCodonString(state.getCode()) == MaybeBoolean.True) {
                stopSet.add(state);
            }
        }
        return stopSet;
	}

	/**
	 * Returns the number of terminator amino acids.
	 */
	public int getStopCodonCount() {
        int count = 0;
        for (AminoAcidState state : translationMap.values()) {
            if (state == AminoAcids.STOP_STATE) {
                count++;
            }
        }
		return count;
	}

    /**
     *
     * @return the number used by NCBI to represent this genetic code or -1 if none. Eg. 1 = Standard...
     */
    public int getNcbiTranslationTableNumber() {
        return ncbiTranslationTableNumber;
    }

    /**
     * Same as getDescription() (so that GeneticCode objects can be used e.g. in a JComboBox).
     * @return the description of this genetic code
     */
    public String toString() {
        return getDescription();
    }
}

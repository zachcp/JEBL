/*
 * Utils.java
 *
 * (c) 2002-2005 JEBL Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.sequences;

import jebl.evolution.taxa.Taxon;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 *
 * @version $Id$
 */
public class Utils {

    public static AminoAcidState[] translate(final State[] states, GeneticCode geneticCode) {
        if (states == null) throw new NullPointerException("States array is null");
        if (states.length == 0) return new AminoAcidState[0];

        if (states[0] instanceof NucleotideState) {
            AminoAcidState[] translation = new AminoAcidState[states.length / 3];
            for (int i = 0; i < translation.length; i++) {
                CodonState state = Codons.getState((NucleotideState)states[i * 3],
                                                    (NucleotideState)states[(i * 3) + 1],
                                                    (NucleotideState)states[(i * 3) + 2]);
                translation[i] = geneticCode.getTranslation(state);
            }
            return translation;
        } else if (states[0] instanceof CodonState) {
            AminoAcidState[] translation = new AminoAcidState[states.length];
            for (int i = 0; i < translation.length; i++) {
                translation[i] = geneticCode.getTranslation((CodonState)states[i]);
            }
            return translation;
        } else {
            throw new IllegalArgumentException("Given states are not nucleotides or codons so cannot be translated");
        }
    }

    public static String reverseComplement(final String nucleotideSequence) {
        Sequence seq = new BasicSequence(SequenceType.NUCLEOTIDE, Taxon.getTaxon("x"), nucleotideSequence);
        seq = new GaplessSequence(seq);
        State[] states = seq.getStates();
        NucleotideState[] nucleotideStates = new NucleotideState[states.length];
        for (int i = 0; i < states.length; i++) {
            nucleotideStates[i] = (NucleotideState) states[i];
        }
        nucleotideStates = reverseComplement(nucleotideStates);
        seq = new BasicSequence(SequenceType.NUCLEOTIDE, Taxon.getTaxon("x"), nucleotideStates);
        return seq.getString();
    }

    public static String translate(final String nucleotideSequence, GeneticCode geneticCode) {
        Sequence seq = new BasicSequence(SequenceType.NUCLEOTIDE, Taxon.getTaxon("x"), nucleotideSequence);
        seq = new GaplessSequence(seq);
        State[] states = seq.getStates();

        states = translate(states, geneticCode);
        seq = new BasicSequence(SequenceType.AMINO_ACID, Taxon.getTaxon("x"), states);
        return seq.getString();
    }

    public static State[] stripGaps(final State[] sequence) {
        int count = 0;
        for (State state : sequence) {
            if (!state.isGap()) {
                count++;
            }
        }

        State[] stripped = new State[count];
        int index = 0;
        for (State state : sequence) {
            if (!state.isGap()) {
                stripped[index] = state;
                index += 1;
            }
        }

        return stripped;
    }

    public static State[] reverse(final State[] sequence) {
        State[] reversed = new State[sequence.length];
        for (int i = 0; i < sequence.length; i++) {
            reversed[i] = sequence[sequence.length - i - 1];
        }
        return reversed;
    }

    public static NucleotideState[] complement(final NucleotideState[] sequence) {
        NucleotideState[] complemented = new NucleotideState[sequence.length];
        for (int i = 0; i < sequence.length; i++) {
            complemented[i] = Nucleotides.COMPLEMENTARY_STATES[sequence[i].getIndex()];
        }
        return complemented;
    }

    public static NucleotideState[] reverseComplement(final NucleotideState[] sequence) {
        NucleotideState[] reverseComplemented = new NucleotideState[sequence.length];
        for (int i = 0; i < sequence.length; i++) {
            reverseComplemented[i] = Nucleotides.COMPLEMENTARY_STATES[sequence[sequence.length - i - 1].getIndex()];
        }
        return reverseComplemented;
    }

    public static int[] getStateIndices(final State[] sequence) {
        int[] indices = new int[sequence.length];
        int i = 0;
        for (State state : sequence) {
            indices[i] = state.getIndex();
        }

        return indices;
    }

    /**
     * Gets the site location index for this sequence excluding
     * any gaps. The location is indexed from 0.
     * @param sequence the sequence
     * @param gappedLocation the location including gaps
     * @return the location without gaps.
     */
    public static int getGaplessLocation(Sequence sequence, int gappedLocation) {
        int gapless = 0;
        int gapped = 0;
        for (State state : sequence.getStates()) {
            if (gapped == gappedLocation) return gapless;
            if (!state.isGap()) {
                gapless ++;
            }
            gapped ++;
        }
        return gapless;
    }

    /**
     * Gets the site location index for this sequence that corresponds
     * to a location given excluding all gaps. The first non-gapped site
     * in the sequence has a gaplessLocation of 0.
     * @param sequence the sequence
     * @param gaplessLocation
     * @return the site location including gaps
     */
    public static int getGappedLocation(Sequence sequence, int gaplessLocation) {
        int gapless = 0;
        int gapped = 0;
        for (State state : sequence.getStates()) {
            if (gapless == gaplessLocation) return gapped;
            if (!state.isGap()) {
                gapless ++;
            }
            gapped ++;
        }
        return gapped;
    }

    /**
     * Guess type of sequence from contents
     * @param seq the sequence
     * @return type of sequence, null if sequence contains illegal characters.
     */
    public static SequenceType guessSequenceType(final String seq) {

        int canonicalNucStates = 0;
        boolean isAminoSequence = false;
        final int canonicalStateCount = Nucleotides.getCanonicalStateCount();

        for( char c : seq.toCharArray() ) {
            final NucleotideState nucState = Nucleotides.getState(c);
            final boolean isNucState = nucState != null;
            final boolean isAminoState = AminoAcids.getState(c) != null;

            if( ! (isNucState || isAminoState) ) {
                return null;
            }
            if (! isAminoSequence) {
                if (!isNucState) {
                    // must be amino
                    isAminoSequence = true;
                } else {

                    if (nucState.getIndex() < canonicalStateCount) {
                        ++canonicalNucStates;
                    }
                }
            }
        }

        final double threshold = 0.7;

        if (canonicalNucStates >= seq.length() * threshold) {
            return SequenceType.NUCLEOTIDE;
        }
        return SequenceType.AMINO_ACID;
    }
}

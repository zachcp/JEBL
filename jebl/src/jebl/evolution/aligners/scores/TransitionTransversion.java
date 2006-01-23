package jebl.evolution.aligners.scores;

import jebl.evolution.sequences.SequenceType;

public class TransitionTransversion extends AbstractScores {

    public TransitionTransversion(float transversionBias) {
        String nucleotides = "ACGT";
        float[][] scores = {
                /*  A   C   G   T   */
                {   5},
                {  -transversionBias,  5},
                {  -1,  -transversionBias,  5},
                {  -transversionBias, -1,  -transversionBias,  5}};
        constructScores(SequenceType.NUCLEOTIDE, nucleotides, scores);
    }


}

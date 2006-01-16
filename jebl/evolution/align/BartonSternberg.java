package jebl.evolution.align;

import jebl.evolution.align.scores.Scores;
import jebl.evolution.align.scores.Blosum60;
import jebl.evolution.io.FastaImporter;
import jebl.evolution.io.ImportException;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.sequences.BasicSequence;
import jebl.evolution.sequences.SequenceType;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Matt Kearse
 * @version $Id$
 *
 * Implements the BartonSternberg multiple sequence alignment algorithm.
 *
 * Note: this is not yet complete, it does not create an initial ordering
 * in which to add sequences to the profile.
 *
 * Also, after creating the profile, it just removes and adds each sequence back into
 * the profile a fixed number of times(currently two).
 */
public class BartonSternberg {
    private float gapOpen,gapExtend;
    Scores scores;
    NeedlemanWunschLinearSpaceAffine aligner;

    public BartonSternberg(Scores scores, float gapOpen, float gapExtend) {
        this.gapOpen = gapOpen;
        this.gapExtend = gapExtend;
        this.scores = Scores.includeGaps(scores, 0);
        aligner =new NeedlemanWunschLinearSpaceAffine(this.scores,gapOpen,gapExtend);

    }

    private AlignmentProgressListener progress;
    private boolean cancelled = false;
    private int sectionsCompleted;
    private int totalSections;
    AlignmentProgressListener minorProgress = new AlignmentProgressListener() {
        public boolean setProgress(double fractionCompleted) {
            double totalProgress = (sectionsCompleted + fractionCompleted)/totalSections;
            if(progress.setProgress(totalProgress)) cancelled = true;
            return cancelled;
        }
    };

    public String[] align(String sourceSequences[], AlignmentProgressListener progress) {

        cancelled = false;
        this.progress = progress;
        int count = sourceSequences.length;
        String[] sequences =new String[count];
        Profile[] sequenceProfiles=new Profile[count];
        for (int i = 0; i < count; i++) {
            sequences[i]= Align.strip(sourceSequences[i], scores.getAlphabet());
            sequenceProfiles[i] = new Profile(i, sequences[i]);
        }
        Profile profile =new Profile(scores.getAlphabet().length());
        int order[] =new int[count];
        for (int i = 0; i < count; i++) {
            order[i]=i; //todo: use an ordering based on similarity
        }
        totalSections = count*3;
        sectionsCompleted = 0;
        profile.addSequence(order [0],sequences [order [0]]);
        for (int i : order) {
            if(order[0] ==i) continue;
            AlignmentResult results[] =aligner.doAlignment (profile, sequenceProfiles[i], minorProgress);
            if(cancelled) return null;
            sectionsCompleted ++;
//            System.out.println("result =" + results[0].size + "," + results[1].size + " from " + profile.length + "," + sequenceProfile.length);
            profile = Profile.combine(profile, sequenceProfiles[i], results[0], results[1]);
//            profile.print ();
        }
        //now remove a single sequence, and we
        for (int j = 0; j < 2; j++) {
            for (int i : order) {
//                if(j> 0&& i!= 8) continue;
//                Profile sequenceProfile = sequenceProfiles[i];
                boolean display = false;

                String sequence = profile.paddedSequences.get(i);
                if(j>= 0 && i== 8) {
//                    display = true;
                }
                if(display) {
                    System.out.println("remove sequence =" + sequence);
                    profile.print (true);
                }
                Profile sequenceProfile = new Profile(i, sequence);
                profile.remove(sequenceProfile);
//                aligner.setDebug(display);
                AlignmentResult results[] = aligner.doAlignment(profile, sequenceProfiles[i], minorProgress);
//                aligner.setDebug(false);
                if (cancelled) return null;
                sectionsCompleted ++;
                if(display){
                    profile.print(false);

                    System.out.println("result =" + results[0].size + "," + results[1].size + " from " + profile.length() + "," + sequenceProfile.length());
                }
                profile = Profile.combine(profile, sequenceProfiles[i], results[0], results[1]);
                if(display) {
                    profile.print(true);
                }
            }
        }


        String[] results =new String[count];
        for (int i = 0; i < count; i++) {
            results[i]= profile.paddedSequences.get(i);
        }
        return results;
    }

    public static void main(String[] arguments) throws IOException, ImportException {
        BufferedReader reader = new BufferedReader(new FileReader(arguments[0]));
        SequenceType sequenceType = SequenceType.AMINO_ACID;

        FastaImporter importer = new FastaImporter(reader,sequenceType);
        List<Sequence> xsequences = importer.importSequences();
        List<String> sequenceStrings = new ArrayList<String>();
        int count = 0;
        int maximum = 10;
        for (Sequence sequence : xsequences) {
            BasicSequence basic = (BasicSequence) sequence;
            String string = basic.getCleanString();
            sequenceStrings.add(string);
            System.out.println(string);
            if(count++ >= maximum) break;
        }
        System.out.println ();
        count = 0;
        for (Sequence sequence : xsequences) {
            BasicSequence basic = (BasicSequence) sequence;
            String string = basic.getString();
            System.out.println(string);
            if (count++ >= maximum) break;
        }
        long start = System.currentTimeMillis();
        BartonSternberg alignment =new BartonSternberg( new Blosum60(), 20, 1);
        String[] sequences = sequenceStrings.toArray(new String[0]);
        System.out.println("aligning " + sequences.length);
        String results[] =alignment.align(sequences, null);
        for (String result : results) {
            System.out.println(result);
        }
        System.out.println ("took " +(System.currentTimeMillis() - start) + " milliseconds");
    }
}

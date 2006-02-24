package jebl.evolution.align;

import jebl.evolution.align.scores.Scores;
import jebl.evolution.align.scores.Blosum60;
import jebl.evolution.io.FastaImporter;
import jebl.evolution.io.ImportException;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.sequences.BasicSequence;
import jebl.evolution.sequences.SequenceType;
import jebl.evolution.trees.TreeBuilder;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.Utils;
import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;

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
    private int refinementIterations;
    private boolean freeGapsAtEnds;

    public BartonSternberg(Scores scores, float gapOpen, float gapExtend, int refinementIterations, boolean freeGapsAtEnds) {
        this.gapOpen = gapOpen;
        this.gapExtend = gapExtend;
        this.scores = Scores.includeGaps(scores, 0, 0);
        this.refinementIterations= refinementIterations;
        this.freeGapsAtEnds=freeGapsAtEnds;
        aligner = new NeedlemanWunschLinearSpaceAffine(this.scores,gapOpen,gapExtend, freeGapsAtEnds);

    }

    CompoundAlignmentProgressListener compoundProgress;
    /*private AlignmentProgressListener progress;
    private boolean cancelled = false;
    private int sectionsCompleted;
    private int totalSections;
    AlignmentProgressListener minorProgress = new AlignmentProgressListener() {
        public boolean setProgress(double fractionCompleted) {
            double totalProgress = (sectionsCompleted + fractionCompleted)/totalSections;
            if(progress.setProgress(totalProgress)) cancelled = true;
            return cancelled;
        }
    };*/

    private Profile align(RootedTree tree, Node node, List<Sequence> seqs) {
        if( tree.isExternal(node) ) {
            Taxon tax = tree.getTaxon(node);
            int iSeq = Integer.parseInt(tax.getName());

            Profile profile = new Profile(scores.getAlphabet().length());
            profile.addSequence(iSeq, seqs.get(iSeq).getString());
            return profile;
        }

        List<Node> children = tree.getChildren(node);   assert( children.size() == 2 );
        Profile left = align(tree, children.get(0), seqs);
        if (compoundProgress.isCancelled()) return null;
        Profile right = align(tree, children.get(1), seqs);
        if (compoundProgress.isCancelled()) return null;

        AlignmentResult results[] = aligner.doAlignment(left, right, compoundProgress.getMinorProgress(), false);
        compoundProgress.incrementSectionsCompleted(1);
        if(compoundProgress.isCancelled()) return null;
        return Profile.combine(left, right, results[0], results[1]);
    }


    public String[] align(List<Sequence> sourceSequences, AlignmentProgressListener progress) {
        int count = sourceSequences.size();

        String[] sequences = new String[count];

        Profile[] sequenceProfiles= new Profile[count];
        List<Sequence> seqs = new ArrayList<Sequence>(sourceSequences.size());
        for (int i = 0; i < count; i++) {
            sequences[i]= Align.strip(sourceSequences.get(i).getString(), scores.getAlphabet());
            sequenceProfiles[i] = new Profile(i, sequences[i]);

            Sequence s = sourceSequences.get(i);
            seqs.add(new BasicSequence(s.getSequenceType(), Taxon.getTaxon("" + i), sequences[i]));
        }
        int treeWork = count*(count - 1)/2;

        compoundProgress =
                new CompoundAlignmentProgressListener(progress,treeWork +count* refinementIterations+ count- 1);

        compoundProgress.setSectionSize(treeWork);
        // We want a binary rooted tree
        RootedTree guideTree = Utils.rootTreeAtCenter(TreeBuilder.build(seqs, TreeBuilder.Method.NEIGHBOR_JOINING,
                                                                   aligner, compoundProgress.getMinorProgress()).tree);
        compoundProgress.incrementSectionsCompleted(treeWork);
        compoundProgress.setSectionSize(1);

        Profile profile = align(guideTree, guideTree.getRootNode(), seqs);
        if (compoundProgress.isCancelled()) return null;


        //now remove a single sequence, and we
        for (int j = 0; j < refinementIterations; j++) {
            for (int i = 0; i < count; ++i) {
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

                AlignmentResult results[] = aligner.doAlignment(profile, sequenceProfiles[i], compoundProgress.getMinorProgress(), false);
//                aligner.setDebug(false);
                if (compoundProgress.isCancelled()) return null;
                compoundProgress.incrementSectionsCompleted(1);
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
        BartonSternberg alignment =new BartonSternberg( new Blosum60(), 20, 1, 2, true);
        String[] sequences = sequenceStrings.toArray(new String[0]);
        System.out.println("aligning " + sequences.length);
        String results[] =alignment.align(xsequences, null);
        for (String result : results) {
            System.out.println(result);
        }
        System.out.println ("took " +(System.currentTimeMillis() - start) + " milliseconds");
    }
}
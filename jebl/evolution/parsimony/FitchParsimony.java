/*
 * FitchParsimony.java
 *
 * (c) 2002-2005 BEAST Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */

package jebl.evolution.parsimony;

import jebl.evolution.trees.Tree;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.Utils;
import jebl.evolution.alignments.Pattern;
import jebl.evolution.alignments.SitePatterns;
import jebl.evolution.graphs.Node;
import jebl.evolution.sequences.State;
import jebl.evolution.sequences.SequenceType;
import jebl.evolution.taxa.Taxon;

import java.util.*;

/**
 * Class for reconstructing characters using Fitch parsimony. This is intended to be much faster
 * than the static methods in the utility "Parsimony" class.
 *
 * @version $Id$
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 */
public class FitchParsimony implements ParsimonyCriterion {

    private final SequenceType sequenceType;
    private final int stateCount;
    private final boolean gapsAreStates;

    private Map<Node, boolean[][]> stateSets = new HashMap<Node, boolean[][]>();
    private Map<Node, State[]> states = new HashMap<Node, State[]>();

    private boolean[][] union;
    private boolean[][] intersection;

    private RootedTree tree = null;
    private final SitePatterns patterns;

    private boolean hasCalculatedSteps = false;
    private boolean hasRecontructedStates = false;

    private final double[] siteScores;

    public FitchParsimony(SitePatterns patterns, boolean gapsAreStates) {
        if (patterns == null) {
            throw new IllegalArgumentException("The patterns cannot be null");
        }

        this.sequenceType = patterns.getSequenceType();
        this.gapsAreStates = gapsAreStates;

        if (gapsAreStates) {
            stateCount = sequenceType.getCanonicalStateCount() + 1;
        } else {
            stateCount = sequenceType.getCanonicalStateCount();

        }

        this.patterns = patterns;

        this.siteScores = new double[patterns.getPatternCount()];
    }

    /**
     * Calculates the minimum number of siteScores for the parsimony reconstruction of a
     * a set of character patterns on a tree. This only does the first pass of the
     * Fitch algorithm so it does not store ancestral state reconstructions.
     * @param tree a tree object to reconstruct the characters on
     * @return number of parsimony siteScores
     */
    public double[] getSiteScores(Tree tree) {

        if (tree == null) {
            throw new IllegalArgumentException("The tree cannot be null");
        }

        if (!(tree instanceof RootedTree)) {
            throw new IllegalArgumentException("The tree must be an instance of rooted tree");
        }

        if (this.tree == null || this.tree != tree) {
            this.tree = (RootedTree)tree;

            if (!Utils.isBinary(this.tree)) {
                throw new IllegalArgumentException("The Fitch algorithm can only reconstruct ancestral states on binary trees");
            }

            initialize();
        }

        if (!hasCalculatedSteps) {
            for (int i = 0; i < siteScores.length; i++) {
                siteScores[i] = 0;
            }
            calculateSteps(this.tree.getRootNode());
            hasCalculatedSteps = true;
        }


        return siteScores;
    }

    public double getScore(Tree tree) {

        getSiteScores(tree);

        double score = 0;

        int i = 0;
        for (Pattern pattern : patterns.getSitePatterns()) {
            score += siteScores[i] * pattern.getWeight();
            i++;
        }
        return score;
    }

    /**
     * Returns the reconstructed character states for a given node in the tree. If this method is repeatedly
     * called with the same tree and patterns then only the first call will reconstruct the states and each
     * subsequent call will return the stored states.
     * @param tree a tree object to reconstruct the characters on
     * @param node the node of the tree
     * @return an array containing the reconstructed states for this node
     */
    public State[] getStates(Tree tree, Node node) {

        getSiteScores(tree);

        if (!hasRecontructedStates) {
            reconstructStates(this.tree.getRootNode(), null);
            hasRecontructedStates = true;
        }

        return states.get(node);
    }

    private void initialize() {
        hasCalculatedSteps = false;
        hasRecontructedStates = false;

        for (Node node : tree.getNodes()) {
            boolean[][] stateSet = new boolean[patterns.getPatternCount()][sequenceType.getCanonicalStateCount()];
            stateSets.put(node, stateSet);

            if (!tree.isExternal(node)) {
                State[] stateArray = new State[patterns.getPatternCount()];
                states.put(node, stateArray);
            }
        }

        List<Taxon> taxa = patterns.getTaxa();
        for (Node node : tree.getExternalNodes()) {
            boolean stateSet[][] = stateSets.get(node);

            int i = 0;
            for (Pattern pattern : patterns.getSitePatterns()) {

	            Taxon taxon = tree.getTaxon(node);
	            int index = taxa.indexOf(taxon);

	            if (index == -1) throw new IllegalArgumentException("Unknown taxon, " + taxon.getName() + " in tree");

                State state = pattern.getState(index);

                if (gapsAreStates && state.isGap()) {
                    stateSet[i][stateCount - 1] = true;
                } else {
                    for (State canonicalState : state.getCanonicalStates()) {
                        stateSet[i][canonicalState.getIndex()] = true;
                    }

                }
                i++;
            }
        }

        union = new boolean[patterns.getPatternCount()][sequenceType.getCanonicalStateCount()];
        intersection = new boolean[patterns.getPatternCount()][sequenceType.getCanonicalStateCount()];

    }

    /**
     * This is the first pass of the Fitch algorithm. This calculates the set of states
     * at each node and counts the total number of siteScores (the score). If that is all that
     * is required then the second pass is not necessary.
     * @param node
     */
    private void calculateSteps(Node node) {

        List<Node> children = tree.getChildren(node);
        if (children.size() > 0) {

            Iterator<Node> iter = children.iterator();

            Node child = iter.next();
            boolean[][] stateSet = stateSets.get(child);
            for (int i = 0; i < patterns.getPatternCount(); i++) {
                copyOf(stateSet[i], union[i]);
                copyOf(stateSet[i], intersection[i]);
            }

            while (iter.hasNext()) {
                child = iter.next();
                stateSet = stateSets.get(child);

                for (int i = 0; i < patterns.getPatternCount(); i++) {
                    unionOf(union[i], stateSet[i], union[i]);
                    intersectionOf(intersection[i], stateSet[i], intersection[i]);
                }
            }

            boolean[][] nodeStateSet = stateSets.get(node);
            for (int i = 0; i < patterns.getPatternCount(); i++) {
                if (sizeOf(intersection[i]) > 0) {
                    copyOf(intersection[i], nodeStateSet[i]);
                } else {
                    copyOf(union[i], nodeStateSet[i]);
                    siteScores[i] ++;
                }
            }
        }
    }

    /**
     * The second pass of the Fitch algorithm. This reconstructs the ancestral states at
     * each node.
     * @param node
     * @param parentStates
     */
    private void reconstructStates(Node node, State[] parentStates) {

        boolean[][] nodeStateSet = stateSets.get(node);
        State[] nodeStates = states.get(node);

        for (int i = 0; i < patterns.getPatternCount(); i++) {

            if (parentStates != null && nodeStateSet[i][parentStates[i].getIndex()]) {
                nodeStates[i] = parentStates[i];
            } else {
                int first = firstIndexOf(nodeStateSet[i]);
                nodeStates[i] = sequenceType.getState(first);
            }
        }

        for (Node child : tree.getChildren(node)) {
            reconstructStates(child, nodeStates);
        }
    }

    private static void copyOf(boolean[] s, boolean[] d) {

        for (int i = 0; i < d.length; i++) {
            d[i] = s[i];
        }
    }

    private static void unionOf(boolean[] s1, boolean[] s2, boolean[] d) {

        for (int i = 0; i < d.length; i++) {
            d[i] = s1[i] || s2[i];
        }
    }

    private static void intersectionOf(boolean[] s1, boolean[] s2, boolean[] d) {

        for (int i = 0; i < d.length; i++) {
            d[i] = s1[i] && s2[i];
        }
    }

    private static int firstIndexOf(boolean[] s1) {

        for (int i = 0; i < s1.length; i++) {
            if (s1[i]) {
                return i;
            }
        }
        return -1;
    }

    private static int sizeOf(boolean[] s1) {

        int count = 0;
        for (int i = 0; i < s1.length; i++) {
            if (s1[i]) count += 1;
        }
        return count;
    }

}
package jebl.evolution.trees;

import jebl.evolution.taxa.Taxon;
import jebl.util.ProgressListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * Build a consensus tree for a set of trees. Base class just check for consistency
 * Work in progress.
 *
 * @author Joseph Heled
 * @version $Id$
 */

public abstract class ConsensusTreeBuilder<T extends Tree> implements TreeBuilder<T> {

    /** Name of attribute specifing amount of support for branch */
    final static public String supportAttributeName = "Consensus support(%)";

    /**
     * Supported consesus methods.
     */
    public enum Method { GREEDY("Greedy"), MRCAC("MRCA Clustering");

        Method(String name) {
           this.name = name;
        }

        public String toString() {
            return getName();
        }

        public String getName() {
            return name;
        }

        private String name;
    }

    /** Number of external nodes/taxa */
    protected final int nExternalNodes;

    /** List of common taxa in all trees */
    protected List<Taxon> taxons;


    /**
     * Check for consistmcy and establish the common taxa
     * @param trees
     */
    ConsensusTreeBuilder(Tree[] trees) {
        Tree first = trees[0];

        nExternalNodes = first.getExternalNodes().size();

        final Set<Taxon> taxa = first.getTaxa();
        taxons = new ArrayList<Taxon>(taxa);

        for (Tree t : trees) {
            final int nExternal = t.getExternalNodes().size();
            if (nExternal != nExternalNodes || !t.getTaxa().containsAll(taxa)) {
                throw new IllegalArgumentException("Non compatible trees");
            }
        }
    }

    public void addProgressListener(ProgressListener listener) {
        listeners.add(listener);
    }

    public void removeProgressListener(ProgressListener listener) {
        listeners.remove(listener);
    }

    protected void fireSetProgress(double fractionCompleted) {
        for (ProgressListener listener : listeners) {
            listener.setProgress(fractionCompleted);
        }
    }

    private final List<ProgressListener> listeners = new ArrayList<ProgressListener>();


}
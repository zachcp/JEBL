package jebl.evolution.trees;

import jebl.evolution.taxa.Taxon;

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

public abstract class ConsensusTreeBuilder {
    /** Number of external nodes/taxa */
    protected final int nExternalNodes;

    /** List of common taxa in all trees */
    protected List<Taxon> taxons;

    /** Name of attribute specifing amount of support for branch */
    final static public String supportAttributeName = "Consensus support(%)";

    ConsensusTreeBuilder(Tree[] trees) {
        Tree first = trees[0];

        nExternalNodes = first.getExternalNodes().size();

        final Set<Taxon> taxa = first.getTaxa();
        taxons = new ArrayList<Taxon>(taxa);

        for(int iTree = 0; iTree < trees.length; ++iTree) {
             Tree t = trees[iTree];

            final int nExternal = t.getExternalNodes().size();
            if( nExternal != nExternalNodes || !t.getTaxa().containsAll(taxa) ) {
                throw new IllegalArgumentException("Non compatible trees");
            }
        }
    }

    /**
     *
     * @return  The consensus tree
     */
    abstract public Tree build();
}
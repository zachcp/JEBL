package jebl.evolution.io;

import jebl.evolution.alignments.Alignment;
import jebl.evolution.trees.Tree;

import java.io.IOException;
import java.util.Collection;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 *
 * @version $Id$
 */
public interface TreeExporter {

    /**
     * Export a single tree
     * @param tree
     * @throws IOException
     */
    void exportTree(Tree tree) throws IOException;

    /**
     * Export a collection of trees
     * @param trees
     * @throws IOException
     */
    void exportTrees(Collection<? extends Tree> trees) throws IOException;
}

/*
 * TreeImporter.java
 *
 * (c) 2002-2005 JEBL Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */

package jebl.evolution.io;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import jebl.evolution.trees.Tree;
import jebl.evolution.taxa.Taxon;

/**
 * Interface for importers that do trees
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 *
 * @version $Id$
 */
public interface TreeImporter {

	/**
     * This can be used to read one tree at a time in a loop:
     * <code>
     * List<Tree> trees = new ArrayList<Tree>();
     *  while (hasTree()) {
     *  trees.add(importNextTree());
     *  }
     * </code>
	 * return whether another tree is available.
	 */
	boolean hasTree() throws IOException, ImportException;

    /**
     * Import a single tree
     * @return the tree
     * @throws IOException
     * @throws ImportException
     */
	Tree importNextTree() throws IOException, ImportException;

    /**
     * Import all the trees
     * @return the list of trees
     * @throws IOException
     * @throws ImportException
     */
	List<Tree> importTrees() throws IOException, ImportException;
}

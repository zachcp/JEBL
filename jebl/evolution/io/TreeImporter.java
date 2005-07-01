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
	 * return whether another tree is available.
	 */
	boolean hasTree() throws IOException, ImportException;

	/**
	 * import the next tree.
	 * return the tree or null if no more trees are available
	 */
	Tree importNextTree() throws IOException, ImportException;

	/**
	 * import a single tree.
	 */
	Tree importTree(Set<Taxon> taxa) throws IOException, ImportException;

	/**
	 * import a list of all trees.
	 */
	List<Tree> importTrees(Set<Taxon> taxa) throws IOException, ImportException;
}

/*
 * (c) 2002-2005 JEBL Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.trees;

import jebl.evolution.taxa.Taxon;
import jebl.evolution.graphs.Graph;
import jebl.evolution.graphs.Node;

import java.util.Set;

/**
 * An unrooted tree.
 *
 * @author rambaut
 * @author Alexei Drummond
 *
 * @version $Id$
 */
public interface Tree extends Graph {

    /**
     * @return a set of all nodes that have degree 1.
     * These nodes are often refered to as 'tips'.
     */
    Set getExternalNodes();

    /**
     * @return a set of all nodes that have degree 2 or more.
     * These nodes are often refered to as internal nodes.
     */
    Set getInternalNodes();

    /**
     * @return the set of taxa associated with the external
     * nodes of this tree. The size of this set should be the
     * same as the size of the external nodes set.
     */
    Set getTaxa();

    /**
     * @param node the node whose associated taxon is being requested.
     * @return the taxon object associated with the given node, or null
     * if the node is an internal node.
     */
    Taxon getTaxon(Node node);

    /**
     * @param node the node
     * @return true if the node is of degree 1.
     */
    boolean isExternal(Node node);

    /**
     * @param taxon the taxon
     * @return the external node associated with the given taxon, or null
     * if the taxon is not a member of the taxa set associated with this tree.
     */
    Node getNode(Taxon taxon);
}

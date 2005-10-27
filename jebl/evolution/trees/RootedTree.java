/*
 * RootedTree.java
 *
 * (c) 2005 JEBL Development Team
 *
 * This package is distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.trees;

import jebl.evolution.graphs.Node;

import java.util.Set;
import java.util.List;

/**
 * A tree with a root (node with maximum height). This interface
 * provides the concept of a direction of time that flows from the
 * root to the tips. Each node in the tree has a node height that is
 * less than its parent's height and greater than it children's heights.
 *
 * @author rambaut
 * @author Alexei Drummond
 *
 * @version $Id$
 */
public interface RootedTree extends Tree {

    /**
     * @param node the node whose children are being requested.
     * @return the list of nodes that are the children of the given node.
     * The set may be empty for a terminal node (a tip).
     */
    List<Node> getChildren(Node node);

    /**
     * @param node the node whose height is being requested.
     * @return the height of the given node. The height will be
     * less than the parent's height and greater than it children's heights.
     */
    double getHeight(Node node);

    /**
     * @param node the node whose branch length (to its parent) is being requested.
     * @return the length of the branch to the parent node (0.0 if the node is the root).
     */
    double getLength(Node node);

    /**
     * @param node the node whose parent is requested
     * @return the parent node of the given node, or null
     * if the node is the root node.
     */
    Node getParent(Node node);

    /**
     * The root of the tree has the largest node height of
     * all nodes in the tree.
     * @return the root of the tree.
     */
    Node getRootNode();
}

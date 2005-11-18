/*
 * Utils.java
 *
 * (c) 2005 JEBL Development Team
 *
 * This package is distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.trees;

import jebl.evolution.graphs.Node;

import java.util.*;

/**
 * A collection of utility functions for trees.
 *
 * @author rambaut
 * @author Alexei Drummond
 *
 * @version $Id$
 */
public final class Utils {

    /**
     * @param tree
     * @return the rooted tree as a newick format string
     */
    public static String toNewick(RootedTree tree) {
        StringBuffer buffer = new StringBuffer();
        toNewick(tree,tree.getRootNode(), buffer);
        return buffer.toString();
    }

    private static void toNewick(RootedTree tree, Node node, StringBuffer buffer) {
        if (tree.isExternal(node)) {
            buffer.append(tree.getTaxon(node).getName());
            buffer.append(':');
            buffer.append(tree.getLength(node));
        } else {
            buffer.append('(');
            List<Node> children = tree.getChildren(node);
            for (int i = 0; i < children.size()-1; i++) {
                toNewick(tree, children.get(i), buffer);
                buffer.append(',');
            }
            toNewick(tree, children.get(children.size()-1), buffer);
            buffer.append("):");
            double edgeLength = 0.0;
            if (tree.getParent(node) != null) {
                edgeLength = tree.getLength(node);
            }
            buffer.append(edgeLength);
        }
    }

    /**
     * @param tree the tree
     * @param node1
     * @param node2
     * @return the path length between the two nodes
     */
    public static double getPathLength(Tree tree, Node node1, Node node2) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * @param rootedTree the rooted tree
     * @return true if all internal nodes in the given tree are of degree 3, except the root
     * which must have a degree of 2.
     */
    public static boolean isBinary(RootedTree rootedTree) {

        return (rootedTree.getNodes(3).size() == (rootedTree.getInternalNodes().size() - 1))
                && (Tree.Utils.getDegree(rootedTree, rootedTree.getRootNode()) == 2);
    }

    /**
     * @param rootedTree the rooted tree
     * @return true if all the external nodes in the tree have a height of 0.0
     */
    public static boolean isUltrametric(RootedTree rootedTree) {

        Set externalNodes = rootedTree.getExternalNodes();
        for (Object externalNode : externalNodes) {
            Node node = (Node) externalNode;
            if (rootedTree.getHeight(node) != 0.0) return false;
        }
        return true;
    }

    /**
     * Return the number of external nodes under this node.
     * @param tree
     * @param node
     * @return the number of external nodes under this node.
     */
    public static int getExternalNodeCount(RootedTree tree, Node node) {

        List<Node> children = tree.getChildren(node);
        if (children.size() == 0) return 1;

        int externalNodeCount = 0;
        for (Node child : children) {
            externalNodeCount += getExternalNodeCount(tree, child);
        }

        return externalNodeCount;
    }

    /**
     *
     * @param tree
     * @param node
     * @return the minimum node height
     */
    public static double getMinNodeHeight(RootedTree tree, Node node) {

        List<Node> children = tree.getChildren(node);
        if (children.size() == 0) return tree.getHeight(node);

        double minNodeHeight = Double.MAX_VALUE;
        for (Node child : children) {
            double height = getMinNodeHeight(tree, child);
            if (height < minNodeHeight) {
                minNodeHeight = height;
            }
        }
        return minNodeHeight;
    }

    public static Comparator<Node> createNodeDensityComparator(final RootedTree tree) {

        return new Comparator<Node>() {

            public int compare(Node node1, Node node2) {
                return getExternalNodeCount(tree, node2) - getExternalNodeCount(tree, node1);
            }

            public boolean equals(Node node1, Node node2) {
	            return compare(node1, node2) == 0;
            }
        };
    }

    public static Comparator<Node> createNodeDensityMinNodeHeightComparator(final RootedTree tree) {

        return new Comparator<Node>() {

            public int compare(Node node1, Node node2) {
                int larger = getExternalNodeCount(tree, node1) - getExternalNodeCount(tree, node2);

                if (larger != 0) return larger;

                double tipRecent = getMinNodeHeight(tree, node2) - getMinNodeHeight(tree, node1);
                if (tipRecent > 0.0) return 1;
                if (tipRecent < 0.0) return -1;
                return 0;
            }

            public boolean equals(Node node1, Node node2) {
                return compare(node1, node2) == 0;
            }
        };
    }

}

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

    private static void branchesMinMax(RootedTree tree, Node node, double[] bounds) {
        if (tree.isExternal(node)) {
            return;
        }

        final List<Node> children = tree.getChildren(node);
        for( Node n : children ) {
            final double len = tree.getLength(n);
            bounds[0] = Math.min(bounds[0], len);
            bounds[1] = Math.max(bounds[1], len);
            branchesMinMax(tree, n, bounds);
        }
    }

   private static String[] asText(RootedTree tree, Node node, final double factor) {
          if (tree.isExternal(node)) {
             String name = tree.getTaxon(node).getName();
             return new String[] { ' ' + name };
          }

         final List<Node> children = tree.getChildren(node);
         List< String[] > a = new ArrayList< String[] >(children.size());
         int[] branches = new int[children.size()];
         int tot = 0;
         int maxHeight = -1;

         int k = 0;
         for( Node n : children ) {
             String[] s = asText(tree, n, factor);
             tot += s.length;
             final double len = tree.getLength(n);
             // set 1 as lower bound for branch since the vertical connector (which theoretically has zero
             // width) takes one line.
             final int branchLen = Math.max((int)Math.round(len * factor), 1);
             branches[k] = branchLen; ++k;
             maxHeight = Math.max(maxHeight, s[0].length() + branchLen);
             a.add(s);
         }
         // one empty line between sub trees
         tot += children.size() - 1;
         int ltop = a.get(0).length;
         int lbot = a.get(a.size()-1).length;

         ArrayList<String> x = new ArrayList<String>(tot);
         for(int i = 0; i < a.size(); ++i) {
             String[] s = a.get(i);
             int branchIndex = s.length / 2;
             boolean isLast = i == a.size()-1;
             for(int j = 0; j < s.length; ++j) {
                 char c = (j == branchIndex) ? '=' : ' ';
                 char l = ( i == 0 && j < branchIndex || isLast && j > branchIndex ) ? ' ' :
                         (j == branchIndex ? '+' : '|');
                 String l1 = l + rep(c, branches[i]-1) + s[j];
                 x.add( l1 + rep(' ', maxHeight - l1.length()));
             }
             if( !isLast ) {
                 x.add( '|' + rep(' ', maxHeight-1) );
             }
         }

         for( String ss : x ) {
             assert(ss.length() == x.get(0).length() );
         }

         return x.toArray(new String[]{});

    }

     private static String rep(char c, int count) {
         StringBuffer b = new StringBuffer();
         while( count > 0 ) {
             b.append(c);
             --count;
         }
         return b.toString();
     }

     public static String[] asText(RootedTree tree, int widthGuide) {
         Node root = tree.getRootNode();
         double[] bounds = new double[2];
         bounds[0] = java.lang.Double.MAX_VALUE;
         bounds[1] = -1;

         branchesMinMax(tree, root, bounds);
         double lowBound = 2 / bounds[0];
         double treeHeight = tree.getHeight(root);
         double treeHieghtWithLowBound = treeHeight * lowBound;

         double scale;
         if( treeHieghtWithLowBound > widthGuide ) {
            scale = widthGuide / treeHeight;
         } else {
            lowBound = (5 / bounds[0]);
            if( treeHeight * lowBound <= widthGuide ) {
                scale = lowBound;
            } else {
                scale = widthGuide / treeHeight;
            }
         }
         return asText(tree, root, scale);
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

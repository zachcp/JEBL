/*
 * Utils.java
 *
 * (c) 2005 JEBL Development Team
 *
 * This package is distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.trees;

import jebl.evolution.graphs.Graph;
import jebl.evolution.graphs.Node;

import java.util.*;

/**
 * A collection of utility functions for trees.
 *
 * @author rambaut
 * @author Alexei Drummond
 * @version $Id$
 */
public final class Utils {

    /**
     * @param tree
     * @return the rooted tree as a newick format string
     */
    public static String toNewick(RootedTree tree) {
        StringBuilder buffer = new StringBuilder();
        toNewick(tree, tree.getRootNode(), buffer);
        return buffer.toString();
    }

    private static void addMetaComment(Node node, StringBuilder buffer) {
        Map<String, Object> map = node.getAttributeMap();
        if (map.size() == 0) {
            return;
        }
        buffer.append(" [&");
        boolean first = true;
        for (Map.Entry<String, Object> o : map.entrySet()) {
            if (! first) buffer.append(",");
            first = false;
            String val = o.getValue().toString();
            // we have no way to quote commas right now, throw them away if inside value.
            val = val.replace(',', ' ');
            buffer.append(o.getKey() + "=" + val);
        }
        buffer.append("] ");
    }

    private static void toNewick(RootedTree tree, Node node, StringBuilder buffer) {
        if (tree.isExternal(node)) {
            String name = tree.getTaxon(node).getName();
            if (!name.matches("^\\w+$")) {
                name = "\"" + name + "\"";
            }
            buffer.append(name);
            buffer.append(':');
            buffer.append(tree.getLength(node));
            addMetaComment(node, buffer);
        } else {
            buffer.append('(');
            List<Node> children = tree.getChildren(node);
            for (int i = 0; i < children.size() - 1; i++) {
                toNewick(tree, children.get(i), buffer);
                buffer.append(',');
            }
            toNewick(tree, children.get(children.size() - 1), buffer);
            buffer.append(")");

            Node parent = tree.getParent(node);
            // Don't write root length. This is ignored elsewhere and the nexus importer fails
            // whet it is present.
            if (parent != null) {
                buffer.append(":" + tree.getLength(node));
            }
            addMetaComment(node, buffer);
        }
    }

    private static void branchesMinMax(RootedTree tree, Node node, double[] bounds) {
        if (tree.isExternal(node)) {
            return;
        }

        if (! tree.hasLengths()) {
            bounds[0] = bounds[1] = 1;
            return;
        }

        final List<Node> children = tree.getChildren(node);
        for (Node n : children) {
            final double len = tree.getLength(n);
            bounds[0] = Math.min(bounds[0], len);
            bounds[1] = Math.max(bounds[1], len);
            branchesMinMax(tree, n, bounds);
        }
    }

    private static String[] asText(RootedTree tree, Node node, final double factor) {
        if (tree.isExternal(node)) {
            String name = tree.getTaxon(node).getName();
            return new String[]{' ' + name};
        }

        final List<Node> children = tree.getChildren(node);
        List<String[]> a = new ArrayList<String[]>(children.size());
        int[] branches = new int[children.size()];
        int tot = 0;
        int maxHeight = -1;

        int k = 0;
        for (Node n : children) {
            String[] s = asText(tree, n, factor);
            tot += s.length;
            final double len = tree.hasLengths() ? tree.getLength(n) : 1.0;
            // set 1 as lower bound for branch since the vertical connector
            // (which theoretically has zero width) takes one line.
            final int branchLen = Math.max((int) Math.round(len * factor), 1);
            branches[k] = branchLen;
            ++k;
            maxHeight = Math.max(maxHeight, s[0].length() + branchLen);
            a.add(s);
        }
        // one empty line between sub trees
        tot += children.size() - 1;

        ArrayList<String> x = new ArrayList<String>(tot);
        for (int i = 0; i < a.size(); ++i) {
            String[] s = a.get(i);
            int branchIndex = s.length / 2;
            boolean isLast = i == a.size() - 1;
            for (int j = 0; j < s.length; ++j) {
                char c = (j == branchIndex) ? '=' : ' ';
                char l = (i == 0 && j < branchIndex || isLast && j > branchIndex) ? ' ' :
                        (j == branchIndex ? '+' : '|');
                String l1 = l + rep(c, branches[i] - 1) + s[j];
                x.add(l1 + rep(' ', maxHeight - l1.length()));
            }
            if (!isLast) {
                x.add('|' + rep(' ', maxHeight - 1));
            }
        }

        for (String ss : x) {
            assert(ss.length() == x.get(0).length());
        }

        return x.toArray(new String[]{});

    }

    private static String rep(char c, int count) {
        StringBuilder b = new StringBuilder();
        while (count > 0) {
            b.append(c);
            --count;
        }
        return b.toString();
    }

    // NUmber of branches from node to most remote tip.
    private static int nodeDistance(final RootedTree tree, final Node node) {
        if (tree.isExternal(node)) {
            return 0;
        }

        int d = 0;
        for (Node n : tree.getChildren(node)) {
            d = Math.max(d, nodeDistance(tree, n));
        }
        return d + 1;
    }

    private static double safeTreeHeight(final RootedTree tree) {
        final Node root = tree.getRootNode();
        if (tree.hasHeights()) {
            return tree.getHeight(root);
        }
        return nodeDistance(tree, root);
    }

    public static String[] asText(Tree tree, int widthGuide) {
        RootedTree rtree = rootTheTree(tree);

        Node root = rtree.getRootNode();
        double[] bounds = new double[2];
        bounds[0] = java.lang.Double.MAX_VALUE;
        bounds[1] = -1;

        branchesMinMax(rtree, root, bounds);
        double lowBound = 2 / bounds[0];
        double treeHeight = safeTreeHeight(rtree);
        double treeHieghtWithLowBound = treeHeight * lowBound;

        double scale;
        if (treeHieghtWithLowBound > widthGuide) {
            scale = widthGuide / treeHeight;
        } else {
            lowBound = (5 / bounds[0]);
            if (treeHeight * lowBound <= widthGuide) {
                scale = lowBound;
            } else {
                scale = widthGuide / treeHeight;
            }
        }
        return asText(rtree, root, scale);
    }

    private static double dist(Tree tree, Node root, Node node, Map<HashPair<Node>, Double> dists) throws Graph.NoEdgeException {
        HashPair<Node> p = new HashPair<Node>(root, node);
        if (dists.containsKey(p)) {
            return dists.get(p);
        }

        // assume positive branches
        double maxDist = 0;
        for (Node n : tree.getAdjacencies(node)) {
            if (n != root) {
                double d = dist(tree, node, n, dists);
                maxDist = Math.max(maxDist, d);
            }
        }
        double dist = tree.getEdgeLength(node, root) + maxDist;

        dists.put(p, dist);
        return dist;
    }

    public static RootedTree rootTheTree(Tree tree) {
        // If already rooted, do nothing
        if (tree instanceof RootedTree) {
            return (RootedTree) tree;
        }

        // If a natural root exists, root there
        Set<Node> d2 = tree.getNodes(2);
        if (d2.size() == 1) {
            return new RootedFromUnrooted(tree, d2.iterator().next());
        }

        RootedTree rtree = rootTreeAtCenter(tree);
        if (Graph.Utils.getDegree(rtree, rtree.getRootNode()) > 2) {
            return rtree;
        }

        // Root at central internal node. The root of the tree has at least 3 children.
        // WARNING: using the implementation fact that childern of RootedFromUnrooted are in fact nodes from tree.
        return new RootedFromUnrooted(tree, rtree.getChildren(rtree.getRootNode()).get(0));
    }

    public static RootedTree rootTreeAtCenter(Tree tree) {
        try {
            HashMap<HashPair<Node>, Double> dists = new HashMap<HashPair<Node>, Double>();

            double minOfMaxes = Double.MAX_VALUE;
            HashPair<Node> best = null;
            for (Node i : tree.getInternalNodes()) {
                double maxDist = -Double.MAX_VALUE;
                HashPair<Node> maxDirection = null;
                for (Node n : tree.getAdjacencies(i)) {
                    HashPair<Node> p = new HashPair<Node>(i, n);
                    double d = dist(tree, p.first, p.second, dists);
                    if (maxDist < d) {
                        maxDist = d;
                        maxDirection = p;
                    }
                }

                if (maxDist < minOfMaxes) {
                    minOfMaxes = maxDist;
                    best = maxDirection;
                }
            }

            if (best == null) {
                minOfMaxes = Double.MAX_VALUE;
                best = null;
                for (Node i : tree.getInternalNodes()) {
                    double maxDist = -Double.MAX_VALUE;
                    HashPair<Node> maxDirection = null;
                    for (Node n : tree.getAdjacencies(i)) {
                        HashPair<Node> p = new HashPair<Node>(i, n);
                        double d = dist(tree, p.first, p.second, dists);
                        if (maxDist < d) {
                            maxDist = d;
                            maxDirection = p;
                        }
                    }

                    if (maxDist < minOfMaxes) {
                        minOfMaxes = maxDist;
                        best = maxDirection;
                    }

                    if (best == null) {
                        maxDist = -Double.MAX_VALUE;
                        maxDirection = null;
                        for (Node n : tree.getAdjacencies(i)) {
                            HashPair<Node> p = new HashPair<Node>(i, n);
                            double d = dist(tree, p.first, p.second, dists);
                            if (maxDist < d) {
                                maxDist = d;
                                maxDirection = p;
                            }
                        }

                        if (maxDist < minOfMaxes) {
                            minOfMaxes = maxDist;
                            best = maxDirection;
                        }
                    }
                }
            }

            double distToSecond = -Double.MAX_VALUE;
            for (Node n : tree.getAdjacencies(best.first)) {
                if (n != best.second) {
                    double d1 = dists.get(new HashPair<Node>(best.first, n));
                    if (d1 > distToSecond) {
                        distToSecond = d1;
                    }
                }
            }

            double d = (minOfMaxes - distToSecond) / 2;
            if (d > tree.getEdgeLength(best.first, best.second) ||
                    Graph.Utils.getDegree(tree, best.first) < 3 || Graph.Utils.getDegree(tree, best.second) == 2) {
                return new RootedFromUnrooted(tree, best.first);
            }

            return new RootedFromUnrooted(tree, best.first, best.second, d);
        } catch (Graph.NoEdgeException e1) {
            return null; // bug
        }
    }

    /**
     * @param tree  the tree
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
     *         which must have a degree of 2.
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
     *
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
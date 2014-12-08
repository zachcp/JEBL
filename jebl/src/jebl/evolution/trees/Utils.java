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
import jebl.evolution.taxa.Taxon;
import jebl.util.HashPair;

import java.util.*;

/**
 * A collection of utility functions for trees.
 *
 * @author rambaut
 * @author Alexei Drummond
 * @version $Id$
 */
public final class Utils {
    private Utils() { }  // make class uninstantiable

    /**
     * @param tree
     * @return the rooted tree as a newick format string
     */
    public static String toNewick(RootedTree tree) {
        return toNewick(tree, false);
    }

    /**
     * @param tree
     * @param exportWithMetacomments   we'll try to get the bootstrap values included
     * @return
     */
    public static String toNewick(RootedTree tree, boolean exportWithMetacomments) {
        StringBuilder buffer = new StringBuilder();
        toNewick(tree, tree.getRootNode(), buffer, exportWithMetacomments);
        buffer.append(";");
        return buffer.toString();
    }


    /**
     * Constructs a unique newick representation of a tree
     *
     * @param tree
     */
    public static String toUniqueNewick(RootedTree tree) {
        return toUniqueNewick(tree, tree.getRootNode());
    }

    /**
     * Constructs a unique newick representation of a tree print only an attribute
     *
     * @param tree
     */
    public static String toUniqueNewickByAttribute(RootedTree tree, String attribute) {
        return toUniqueNewickByAttribute(tree, tree.getRootNode(), attribute);
    }

    /**
     * Adds a metadata (eg. Bootstrap, Posterior Value) to nodes where it is available.
     * eg. ((A:0.1,B:0.1)75:0.1,C:0.1); where 0.1 = branch length and 75 = added metadata.
     * Checks that there is only one such value to append, otherwise skip it.
     *
     * @param node
     * @param buffer
     */
    private static void addSimpleMetaComment(Node node, StringBuilder buffer) {
        Map<String, Object> map = node.getAttributeMap();
        if (map.size() != 1) {
            return;
        }
        boolean first = true;
        for (Map.Entry<String, Object> o : map.entrySet()) {
            if (! first) {
                buffer.append(",");
            }
            first = false;

            String val = o.getValue().toString();
            // we have no way to quote commas right now, throw them away if inside value.
            val = val.replace(',', ' ');
            buffer.append(val);
        }
    }

//  Andrew - Comments are not part of the Newick format so should not be included except within
//  a NEXUS file. I have copied the tree writing code (with metacomments) to NexusExport and
//  simplified this on to produce the straight Newick format.

//    Jonas - That's true, officially it's not part of newick. But according to Helen and different users most of the programs out there support an 'inofficial newick format with comments'
//    So let's add them if the user wants to!

    private static void toNewick(RootedTree tree, Node node, StringBuilder buffer, boolean includeMetacomments) {
        if (tree.isExternal(node)) {
            String name = tree.getTaxon(node).getName();
            if (!name.matches("^(\\w|-)+$")) {
                name = "\'" + name + "\'";
            }
            buffer.append(name);
            if (tree.hasLengths()) {
                buffer.append(':');
                buffer.append(tree.getLength(node));
            }
        } else {
            buffer.append('(');
            List<Node> children = tree.getChildren(node);
            final int last = children.size() - 1;
            for (int i = 0; i < children.size(); i++) {
                toNewick(tree, children.get(i), buffer, includeMetacomments);
                buffer.append(i == last ? ')' : ',');
            }

            Node parent = tree.getParent(node);
            // Write information of the joining parent node.
            // Don't write root length. This is ignored elsewhere and the nexus importer fails when it is present.
            if (parent != null && tree.hasLengths()) {
                if (includeMetacomments) addSimpleMetaComment(node, buffer);
                buffer.append(":").append(tree.getLength(node));
            }
        }
    }

    private static void branchesMinMax(RootedTree tree, Node node, double[] bounds) {
        if (tree.isExternal(node)) {
            return;
        }

        if (!tree.hasLengths()) {
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
        final StringBuilder b = new StringBuilder();
        while (count > 0) {
            b.append(c);
            --count;
        }
        return b.toString();
    }

    // Number of branches from node to most remote tip.
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

    public static double safeNodeHeight(final RootedTree tree, final Node node) {
        if (tree.hasHeights()) {
            return tree.getHeight(node);
        }
        return nodeDistance(tree, node);
    }

    private static double safeTreeHeight(final RootedTree tree) {
        return safeNodeHeight(tree, tree.getRootNode());
    }

    public static int maxLevels(final RootedTree tree) {
        return nodeDistance(tree, tree.getRootNode());
    }

    public static String asText(Tree tree) {
        String[] lines=asText(tree,100);
        StringBuilder builder=new StringBuilder();
        for (String line : lines) {
            builder.append(line).append("\n");
        }
        return builder.toString();
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

    private static class NodePair {
        private final List<Node> nodesAdjacentToChild;
        private final Tree tree;
        private final Node parentNode;
        private final Node childNode;
        private int indexOfFirstUnpushedNode;
        private double maxDist = 0;
        private double overrideMaxDist = 0;

        private NodePair(Tree tree, Node parentNode, Node childNode) {
            this.tree = tree;
            this.parentNode = parentNode;
            this.childNode = childNode;
            this.nodesAdjacentToChild = tree.getAdjacencies(childNode);
            this.indexOfFirstUnpushedNode = 0;
        }

        private boolean finished() {
            return indexOfFirstUnpushedNode == nodesAdjacentToChild.size();
        }

        private double getMaxDist() throws Graph.NoEdgeException {
            assert finished();
            if (overrideMaxDist != 0) {
                return overrideMaxDist;
            }
            return tree.getEdgeLength(childNode, parentNode) + maxDist;
        }

        private void addMaxDist(double maxDist) {
            assert overrideMaxDist == 0;
            this.maxDist = Math.max(this.maxDist, maxDist);
        }

        private Node getNextNode() {
            return nodesAdjacentToChild.get(indexOfFirstUnpushedNode++);
        }

        HashPair<Node> geHashPair() {
            return new HashPair<Node>(parentNode, childNode);
        }

        public void setFinishedMaxDist(double dist) {
            assert overrideMaxDist == 0;
            indexOfFirstUnpushedNode = nodesAdjacentToChild.size();
            overrideMaxDist = dist;
        }
    }

    private static class NodePairStack {
        private Stack<NodePair> stack = new Stack<NodePair>();
        private Set<Node> nodesAlreadyInTree = new HashSet<Node>();

        void push(NodePair nodePair) {
            if (!nodesAlreadyInTree.add(nodePair.parentNode)) {
                throw new IllegalStateException("Tree has cycle");
            }
            stack.push(nodePair);
        }

        NodePair pop() {
            NodePair pop = stack.pop();
            nodesAlreadyInTree.remove(pop.parentNode);
            return pop;
        }

        boolean isEmpty() {
            return stack.isEmpty();
        }
    }

    private static class PathLengthCalculator {
        private final Tree tree;
        private Map<HashPair<Node>, Double> cache = new HashMap<HashPair<Node>, Double>();

        PathLengthCalculator(Tree tree){
            this.tree = tree;
        }
        private double calculate(Node node, Node adjacentNode) throws Graph.NoEdgeException {
            NodePairStack stack = new NodePairStack();
            stack.push(new NodePair(tree, node, adjacentNode));
            while (true) {
                NodePair current = stack.pop();
                while (current.finished()) {
                    double maxDist = current.getMaxDist();
                    if (stack.isEmpty()) {
                        return maxDist;
                    }
                    cache.put(current.geHashPair(), maxDist);
                    current = stack.pop();
                    current.addMaxDist(maxDist);
                }
                // now we are at a node that still has something to process
                stack.push(current); // start by putting current back on
                Node next = current.getNextNode();
                NodePair nodePair = new NodePair(tree, current.childNode, next);
                if (!next.equals(current.parentNode)) {
                    Double distance = cache.get(nodePair.geHashPair());
                    if (distance != null) {
                        nodePair.setFinishedMaxDist(distance);
                        // we'll still push it, just so it can get popped immediately next time through.
                    }
                    stack.push(nodePair);
                }
            }
        }
    }

    /**
     * @param tree
     * @return true if this is a RootedTree which is not conceptually unrooted
     */
    public static boolean isRooted(Tree tree) {
        return tree instanceof RootedTree && !((RootedTree)tree).conceptuallyUnrooted();
    }

    public static RootedTree rootTheTree(Tree tree) {
        return rootTheTree(tree, true);
    }

    /**
     * Return a rooted tree from any tree.
     * <p/>
     * If tree already rooted, return it. Otherwise if there is a "natuarl root" (i.e. a node of
     * degree 2) use it as root. Otherwise use an internal node close to the center of the tree as a root.
     *
     * @param tree to root
     * @return rooted representation
     */
    public static RootedTree rootTheTree(Tree tree, boolean conceptuallyUnrooted) {
        // If already rooted, do nothing
        if (tree instanceof RootedTree) {
            if (tree instanceof SimpleRootedTree) {
                ((SimpleRootedTree) tree).setConceptuallyUnrooted(conceptuallyUnrooted);
            }
            return (RootedTree) tree;
        }

        // If a natural root exists, root there
        Set<Node> d2 = tree.getNodes(2);
        if (d2.size() == 1) {
            return new RootedFromUnrooted(tree, d2.iterator().next(), conceptuallyUnrooted);
        }

        RootedTree rtree = rootTreeAtCenter(tree);
        assert Graph.Utils.getDegree(rtree, rtree.getRootNode()) == 2;

        // Root at central internal node. The root of the tree has at least 3 children.
        // WARNING: using the implementation fact that childern of RootedFromUnrooted are in fact nodes from tree.

        Node root = null;
        double minLength = 100;
        for (Node n : rtree.getChildren(rtree.getRootNode())) {
            if (!rtree.isExternal(n)) {
                final double length = rtree.getLength(n);
                if (root == null || length < minLength) {
                    minLength = length;
                    root = n;
                }

            }
        }

        return new RootedFromUnrooted(tree, root, true);
    }

    /**
     * Root any tree by locating the "center" of tree and adding a new root node at that point
     * <p/>
     * for any point on the tree x let D(x) = Max{distance between x and t : for all tips t}
     * The "center" c is the point with the smallest distance, i.e. D(c) = min{ D(x) : x in tree }
     *
     * @param tree to root
     * @return rooted tree
     */
    public static RootedTree rootTreeAtCenter(Tree tree) {
        // Method - find the pair of tips with the longest distance. It is easy to see that the center
        // is at the midpoint of the path between them.

        try {
            double maxDistance = -Double.MAX_VALUE;
            // node on maximal path
            Node current = null;
            // next node on maximal path
            Node direction = null;

            // locate one terminal node of longest path
            PathLengthCalculator pathLengthCalculator = new PathLengthCalculator(tree);
            for (Node e : tree.getExternalNodes()) {
                for (Node n : tree.getAdjacencies(e)) {
                    final double d = pathLengthCalculator.calculate(e, n);
                    if (d > maxDistance) {
                        maxDistance = d;
                        current = e;
                        direction = n;
                    }
                }
            }

            // traverse along maximal path to it's middle
            double distanceLeft = maxDistance / 2.0;

            while (true) {
                final double len = tree.getEdgeLength(current, direction);
                if (distanceLeft <= len) {
                    //System.out.println(toNewick(rtree));
                    return new RootedFromUnrooted(tree, current, direction, distanceLeft);
                }
                distanceLeft -= len;

                maxDistance = -Double.MAX_VALUE;
                Node next = null;
                for (Node n : tree.getAdjacencies(direction)) {
                    if (n == current) continue;
                    final double d = pathLengthCalculator.calculate(direction, n);
                    if (d > maxDistance) {
                        maxDistance = d;
                        next = n;
                    }
                }
                current = direction;
                direction = next;
            }
        } catch (Graph.NoEdgeException e1) {
            return null; // serious bug, should not happen
        }
    }

    /**
     * @param tree  the tree
     * @param node1
     * @param node2
     * @return the path length between the two nodes
     */
    public static double getPathLength(Tree tree, Node node1, Node node2) {
        try {
            return new PathLengthCalculator(tree).calculate(node1, node2);
        } catch (Graph.NoEdgeException e1) {
            return -1.0;
        }
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

        final List<Node> children = tree.getChildren(node);
        if (children.size() == 0) return 1;

        int externalNodeCount = 0;
        for (Node child : children) {
            externalNodeCount += getExternalNodeCount(tree, child);
        }

        return externalNodeCount;
    }

    /**
     * All nodes in subtree - parents before children (pre - order).
     *
     * @param tree
     * @param node
     * @return nodes in pre-order
     */
    public static List<Node> getNodes(RootedTree tree, Node node) {
        final List<Node> nodes = new ArrayList<Node>();
        nodes.add(node);

        for (Node child : tree.getChildren(node)) {
            nodes.addAll(getNodes(tree, child));
        }

        return nodes;
    }

    /**
     * Right Neighbour of a tip (taxon).
     * <p/>
     * When tree is laid with children in given order, this would be the taxon to the right.
     *
     * @param tree
     * @param tipNode
     * @return Right Neighbour. null if node is the rightmost in tree or not a tip.
     */
    public static Node rightNb(RootedTree tree, Node tipNode) {
        if (!tree.isExternal(tipNode)) return null;

        // Go up to the first ancestor of tip so that tip is not in the rightmost (last) sub tree
        List<Node> children;
        int loc;
        Node parent = tipNode;   // start th loop below with correct node
        do {
            tipNode = parent;
            parent = tree.getParent(tipNode);
            if (parent == null) return null; // rightmost in tree
            children = tree.getChildren(parent);
            loc = children.indexOf(tipNode);
        } while (loc == children.size() - 1);

        assert(loc < children.size() - 1);

        // now find the leftmost tip down the sub tree to the right of ancestor
        Node n = children.get(loc + 1);
        while (!tree.isExternal(n)) {
            n = tree.getChildren(n).get(0);
        }
        return n;
    }

    /**
     * Left Neighbour of a tip (taxon).
     * <p/>
     * When tree is laid with children in given order, this would be the taxon to the left.
     *
     * @param tree
     * @param node
     * @return Left Neighbour. null if node is the leftmost in tree or not a tip.
     */
    public static Node leftNb(RootedTree tree, Node node) {
        if (!tree.isExternal(node)) return null;

        // Go up to the first ancestor of tip so that tip is not in the first sub tree
        Node parent = node;
        List<Node> children;
        int loc;
        do {
            node = parent;
            parent = tree.getParent(node);
            if (parent == null) return null; // rightmost in tree
            children = tree.getChildren(parent);
            loc = children.indexOf(node);
        } while (loc == 0);

        assert(loc > 0);

        // now find the rightmost tip down the sub tree to the left of ancestor

        Node n = children.get(loc - 1);
        while (!tree.isExternal(n)) {
            final List<Node> ch = tree.getChildren(n);
            n = ch.get(ch.size() - 1);
        }
        return n;
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

    /**
     * Subtracts a collection from a set and returns the result as a new Set, without modifying either of the parameters.
     * @param a The set from which to subtract the elements of b
     * @param b The elements to be subtracted from b
     * @return An unmodifiable set which contains all of the elements of a except for those which are also in b.
     */
    private static<T> Set<T> setMinus(Set<T> a, Collection<T> b) {
        Set<T> diff = new LinkedHashSet<T>(a);
        diff.removeAll(b);
        return Collections.unmodifiableSet(diff);
    }

    private static<T extends Comparable> List<T> sort(Collection<T> c) {
        List<T> result = new ArrayList<T>(c);
        Collections.sort(result);
        return result;
    }

    /**
     * Checks whether all of the trees passed in have the same external/internal nodes and taxa sets (ignoring
     * order of taxa), and throws an IllegalArgumentException if this is not the case.
     * If no tree or only one tree is passed in, immediately returns without throwing an exception.
     * @param trees Zero or more trees
     * @throws IllegalArgumentException if not all of the trees have the same nodes/taxa
     * @throws NullPointerException if trees is null
     */
    public static void assertAllTreesHaveTheSameProperties(List<? extends Tree> trees) throws IllegalArgumentException {
        if (trees.size() <= 1) {
            return;
        }
        Tree firstTree = trees.get(0);
        final int firstNumExternalNodes = firstTree.getExternalNodes().size();
        final int firstNumInternalNodes = firstTree.getInternalNodes().size();
        final Set<Taxon> firstTaxa = firstTree.getTaxa();

        int currentTreeNumber = 0;
        String nodesNotEqual = "";
        String taxaNotEqual = "";
        for (Tree currentTree : trees) {
            currentTreeNumber++;
            final int numExternalNodes = currentTree.getExternalNodes().size();
            final int numInternalNodes = currentTree.getInternalNodes().size();
            if (numExternalNodes != firstNumExternalNodes) {
                nodesNotEqual = "Tree 1 has "+firstNumExternalNodes+" external nodes. Tree "+currentTreeNumber+" has "+numExternalNodes+" external nodes.\n";
            }
            if (!nodesNotEqual.isEmpty() && numInternalNodes != firstNumInternalNodes) {
            // if externalNodes are different we want to check whether they might have shifted to internal nodes or whether they're completely lost
                nodesNotEqual+= "Tree 1 has "+firstNumInternalNodes+" internal nodes. Tree "+currentTreeNumber+" has "+numInternalNodes+" internal nodes.\n";
            }
            if (!currentTree.getTaxa().containsAll(firstTaxa)) {
                Set<Taxon> firstMinusCurrent = setMinus(firstTree.getTaxa(), currentTree.getTaxa()); // Taxa that occur in the first tree but not in currentTree
                assert !firstMinusCurrent.isEmpty();
                taxaNotEqual = "The following taxa occur in tree 1 but not in tree " + currentTreeNumber + ": " + sort(firstMinusCurrent) +
                            "\nTree 1 has taxa: "+sort(firstTaxa)+" \nTree "+currentTreeNumber+" has taxa: "+sort(currentTree.getTaxa());
            }
            if (!firstTaxa.containsAll(currentTree.getTaxa())) {
                Set<Taxon> currentMinusFirst = setMinus(currentTree.getTaxa(), firstTree.getTaxa());
                assert !currentMinusFirst.isEmpty();
                taxaNotEqual+= "The following taxa occur in tree " + currentTreeNumber + " but not in tree 1: " + sort(currentMinusFirst) +
                            "\nTree "+currentTreeNumber+" has taxa: "+sort(currentTree.getTaxa())+" \nTree 1 has taxa: "+sort(firstTaxa);
            }
            if (!nodesNotEqual.isEmpty() || !taxaNotEqual.isEmpty()) {
                throw new IllegalArgumentException("These " + trees.size() + " trees don't all have the same properties:\n" + nodesNotEqual + taxaNotEqual + "\n\nWe're dealing with a " + currentTree.getClass().getSimpleName());
            }
        }
    }

    /**
     * Generates a unique representation of a node
     *
     * @param tree tree
     * @param node node
     */
    private static String toUniqueNewick(RootedTree tree, Node node) {
        return toUniqueNewickByAttribute(tree, node, null);
    }

    /**
     * Generates a unique representation of a node printing only its attribute
     *
     * @param tree      tree
     * @param node      node
     * @param attribute when not null, use attribute to get taxa name
     * @return tree representation
     */
    private static String toUniqueNewickByAttribute(RootedTree tree, Node node, String attribute) {
        StringBuilder buffer = new StringBuilder();
        if (tree.isExternal(node)) {
            final Taxon taxon = tree.getTaxon(node);
            final String name = attribute != null ? (String) taxon.getAttribute(attribute) : taxon.getName();
            buffer.append(name);
            if (tree.hasLengths()) {
                buffer.append(':');
                buffer.append(tree.getLength(node));
            }
        } else {
            buffer.append('(');
            List<Node> children = tree.getChildren(node);
//        	 if( children.size() == 1)
//        		 return toUniqueNewickByAttribute(tree,children.get(0),attribute);
//


            final int last = children.size() - 1;
            // Generate a uniquely sorted list of children
            List<String> childStrings = new ArrayList<String>();
            for (Node aChildren : children) {
                childStrings.add(toUniqueNewickByAttribute(tree, aChildren, attribute));
            }
            Collections.sort(childStrings,
                    new Comparator<String>() {
                        public int compare(String arg0, String arg1) {
                            return arg1.compareTo(arg0);
                        }
                    });
            for (int i = 0; i <= last; i++) {
                buffer.append(childStrings.get(i));
                buffer.append(i == last ? ')' : ',');
            }

            final Node parent = tree.getParent(node);
            if (parent != null && tree.hasLengths()) {
                buffer.append(":").append(tree.getLength(node));
            }
        }
        return buffer.toString();
    }

    // debug aid - print a representetion of node omitting branches
    static public String DEBUGsubTreeRep(RootedTree tree, Node node) {
        if (tree.isExternal(node)) {
            return tree.getTaxon(node).getName();
        }
        StringBuilder b = new StringBuilder();
        for (Node x : tree.getChildren(node)) {
            if (b.length() > 0) b.append(",");
            b.append(DEBUGsubTreeRep(tree, x));
        }
        return '(' + b.toString() + ')';
    }


    /**
     * This method creates an unattached copy of the given rooted tree such that changes to the copied tree do not affect the original tree.
     * @param treeToCopy the tree to copy
     * @return an equivalent tree to treeToCopy (NB this may not be of the same RootedTree subclass as treeToCopy)
     */
    public static RootedTree copyTree(RootedTree treeToCopy){
        return new CompactRootedTree(treeToCopy);
    }

    // debug aid - unrooted tree printout - un-comment in emergency

//    private static String nodeName(Tree tree, Node n) {
//        if( tree.isExternal(n) ) {
//            return tree.getTaxon(n).getName();
//        }
//        final String s = n.toString();
//        return s.substring(s.lastIndexOf('@'));
//    }
//
//    private static void DEBUGshowTree(Tree tree) {
//       for (Node e : tree.getNodes())  {
//           final String name = nodeName(tree, e);
//           System.out.print(name + ":");
//           for( Node n : tree.getAdjacencies(e) ) {
//               try {
//                   System.out.print(" {" + nodeName(tree, n) + " : " + tree.getEdgeLength(e, n) + "}");
//               } catch (Graph.NoEdgeException e1) {
//                   e1.printStackTrace();
//               }
//           }
//           System.out.println();
//       }
//    }

}
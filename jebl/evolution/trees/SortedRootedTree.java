package jebl.evolution.trees;

import jebl.evolution.taxa.Taxon;
import jebl.evolution.graphs.Node;

import java.util.*;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id$
 */
public class SortedRootedTree implements RootedTree {

    public SortedRootedTree(final RootedTree source) {
        this.source = source;
        this.comparator = new Comparator<Node>() {
            public int compare(Node node1, Node node2) {
                return jebl.evolution.trees.Utils.getExternalNodeCount(source, node2) - jebl.evolution.trees.Utils.getExternalNodeCount(source, node1);
            }

            public boolean equals(Node node1, Node node2) {
                return compare(node1, node2) == 0;
            }
        };
    }

    public SortedRootedTree(RootedTree source, Comparator<Node> comparator) {
        this.source = source;
        this.comparator = comparator;
    }

    public List<Node> getChildren(Node node) {
        List<Node> sourceList = source.getChildren(node);
        Collections.sort(sourceList, comparator);
        return sourceList;
    }

    public double getHeight(Node node) {
        return source.getHeight(node);
    }

    public double getLength(Node node) {
        return source.getLength(node);
    }

    public Node getParent(Node node) {
        return source.getParent(node);
    }

    public Node getRootNode() {
        return source.getRootNode();
    }

    public Set<Node> getExternalNodes() {
        return source.getExternalNodes();
    }

    public Set<Node> getInternalNodes() {
        return source.getInternalNodes();
    }

    public Node getNode(Taxon taxon) {
        return source.getNode(taxon);
    }

    public Set<Taxon> getTaxa() {
        return source.getTaxa();
    }

    public Taxon getTaxon(Node node) {
        return source.getTaxon(node);
    }

    public boolean isExternal(Node node) {
        return source.isExternal(node);
    }

    public Set<Node> getAdjacencies(Node node) {
        return source.getAdjacencies(node);
    }

    public double getEdgeLength(Node node1, Node node2) throws NoEdgeException {
        return source.getEdgeLength(node1, node2);
    }

    public Set<Node> getNodes() {
        return source.getNodes();
    }

    public Set<Node> getNodes(int degree) {
        return source.getNodes(degree);
    }

    private final RootedTree source;
    private final Comparator<Node> comparator;
}

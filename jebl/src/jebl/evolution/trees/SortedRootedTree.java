package jebl.evolution.trees;

import jebl.evolution.graphs.Node;

import java.util.*;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 */
public class SortedRootedTree extends FilteredRootedTree {
	private Map<Node, Integer> externalNodesUnderNodeCounts;
    public enum BranchOrdering {
		INCREASING_NODE_DENSITY("increasing"),
		DECREASING_NODE_DENSITY("decreasing");

		BranchOrdering(String name) {
			this.name = name;
		}

		public String toString() { return name; }

		private String name;
	}

    public SortedRootedTree(final RootedTree source, BranchOrdering branchOrdering) {
        super(source);
		externalNodesUnderNodeCounts = new HashMap<Node, Integer>();

	    switch (branchOrdering) {
		    case INCREASING_NODE_DENSITY:
			    this.comparator = new Comparator<Node>() {
			        public int compare(Node node1, Node node2) {
						return jebl.evolution.trees.Utils.getExternalNodeCount(source, node1, externalNodesUnderNodeCounts) -
								jebl.evolution.trees.Utils.getExternalNodeCount(source, node2, externalNodesUnderNodeCounts);
			        }

			        public boolean equals(Node node1, Node node2) {
			            return compare(node1, node2) == 0;
			        }
			    };
			break;
		    case DECREASING_NODE_DENSITY:
			    this.comparator = new Comparator<Node>() {
			        public int compare(Node node1, Node node2) {
						return jebl.evolution.trees.Utils.getExternalNodeCount(source, node2, externalNodesUnderNodeCounts) -
								jebl.evolution.trees.Utils.getExternalNodeCount(source, node1, externalNodesUnderNodeCounts);
			        }

			        public boolean equals(Node node1, Node node2) {
			            return compare(node1, node2) == 0;
			        }
			    };
			break;
		    default:
			    throw new IllegalArgumentException("Unknown enum value");
	    }
    }

    public SortedRootedTree(RootedTree source, Comparator<Node> comparator) {
	    super(source);
        this.comparator = comparator;
    }

    public List<Node> getChildren(Node node) {
        List<Node> sourceList = source.getChildren(node);
        Collections.sort(sourceList, comparator);
        return sourceList;
    }

	// PRIVATE members

    private final Comparator<Node> comparator;
}
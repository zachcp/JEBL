package jebl.evolution.trees;

import jebl.evolution.graphs.Node;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id$
 */
public class ReRootedTree extends FilteredRootedTree {

    public enum RootingType {
		MID_POINT("midpoint"),
		LEAST_SQUARES("least squares");

		RootingType(String name) {
			this.name = name;
		}

		public String toString() { return name; }

		private String name;
	}

    public ReRootedTree(final RootedTree source, RootingType rootingType) {
        super(source);
	    switch (rootingType) {
		    case MID_POINT:
			    this.comparator = new Comparator<Node>() {
			        public int compare(Node node1, Node node2) {
			            return jebl.evolution.trees.Utils.getExternalNodeCount(source, node1) -
					            jebl.evolution.trees.Utils.getExternalNodeCount(source, node2);
			        }

			        public boolean equals(Node node1, Node node2) {
			            return compare(node1, node2) == 0;
			        }
			    };
			break;
		    case LEAST_SQUARES:
			    this.comparator = new Comparator<Node>() {
			        public int compare(Node node1, Node node2) {
			            return jebl.evolution.trees.Utils.getExternalNodeCount(source, node2) -
					            jebl.evolution.trees.Utils.getExternalNodeCount(source, node1);
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

    public ReRootedTree(RootedTree source, Comparator<Node> comparator) {
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
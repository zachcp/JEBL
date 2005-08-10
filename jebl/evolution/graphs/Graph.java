/*
 * Graph.java
 *
 * (c) 2005 JEBL Development Team
 *
 * This package is distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.graphs;

import java.util.Set;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 *
 * @version $Id$
 */
public interface Graph {

    /**
     * @param node
     * @return the set of nodes that are attached by edges to the given node.
     */
    Set getAdjacencies(Node node);

    /**
     * @param node1
     * @param node2
     * @return the length of the edge connecting node1 and node2.
     * @throws NoEdgeException if the nodes are not directly connected by an edge.
     */
    double getEdgeLength(Node node1, Node node2) throws NoEdgeException;

    /**
     * @return the set of all nodes in this graph.
     */
    Set getNodes();

    /**
     * @param degree the number of edges connected to a node
     * @return a set containing all nodes in this graph of the given degree.
     */
    Set getNodes(int degree);

    /**
     * This class is thrown by getEdgeLength(node1, node2) if node1 and node2
     * are not directly connected by an edge.
     */
    public class NoEdgeException extends Exception {}

    public class Utils {

        /**
         * @param graph
         * @param node
         * @return the number of edges attached to this node.
         */
        public static int getDegree(Graph graph, Node node) {
            return graph.getAdjacencies(node).size();
        }
    }
}

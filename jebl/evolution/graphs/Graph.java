/*
 * Graph.java
 *
 * (c) 2002-2005 BEAST Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.graphs;

import jebl.evolution.taxa.Taxon;

import java.util.Set;

/**
 * @author rambaut
 *         Date: Apr 6, 2005
 *         Time: 12:06:25 PM
 */
public interface Graph {

    Set getNodes();
    Set getNodes(int degree);
    Set getAdjacencies(Node node);
    int getDegree(Node node);

    double getEdgeLength(Node node1, Node node2);

}

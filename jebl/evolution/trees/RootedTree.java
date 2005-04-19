/*
 * RootedTree.java
 *
 * (c) 2002-2005 BEAST Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.trees;

import jebl.evolution.taxa.Taxon;
import jebl.evolution.graphs.Graph;
import jebl.evolution.graphs.Node;

import java.util.Set;

/**
 * @author rambaut
 *         Date: Apr 6, 2005
 *         Time: 12:06:25 PM
 */
public interface RootedTree extends Tree {

    Node getRootNode();

    Set getChildren(Node node);

    double getNodeHeight(Node node);


}

/*
 * Tree.java
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
public interface Tree extends Graph {

    Set getInternalNodes();
    Set getExternalNodes();

    boolean isExternal(Node node);

    Taxon getTaxon(Node node);
    Set getTaxa();

    Node getNode(Taxon taxon);

}

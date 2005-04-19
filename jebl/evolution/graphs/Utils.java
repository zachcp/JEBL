/*
 * Utils.java
 *
 * (c) 2002-2005 BEAST Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.graphs;

/**
 * @author rambaut
 *         Date: Apr 6, 2005
 *         Time: 12:31:58 PM
 */
public class Utils {

    public boolean isAcyclical(Graph graph) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public boolean isConnected(Graph graph) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public boolean isTree(Graph graph) {
        return isAcyclical(graph) && isConnected(graph);
    }
}

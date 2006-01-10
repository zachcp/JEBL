package jebl.evolution.trees;

import jebl.evolution.alignments.Alignment;
import jebl.evolution.distances.JukesCantorDistanceMatrix;

/**
 * Created by IntelliJ IDEA.
 * User: joseph
 * Date: 7/01/2006
 * Time: 10:13:52
 *
 * @author joseph
 * @version $Id$
 *
 * A meeting point for tree building. A very initial form which will develope to encompass more
 * methods and distances.
 */
public class TreeBuilder {

    static public Tree build(Alignment alignment) {
       JukesCantorDistanceMatrix d = new JukesCantorDistanceMatrix(alignment);

       UPGMATreeBuilder c = new UPGMATreeBuilder(d);
       return c.build();
    }
}


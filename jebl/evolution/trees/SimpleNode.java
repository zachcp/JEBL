package jebl.evolution.trees;

import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;
import jebl.util.AttributableHelper;

import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: joseph
 * Date: 6/01/2006
 * Time: 10:19:33
 *
 * @author joseph
 * @version $Id$
 */
class SimpleNode extends BaseNode {

    public SimpleNode(Taxon taxon) {
        super(taxon);
    }

    // Bit of a hack. Use BaseNode adjacencies to store just the children.
    public SimpleNode(Set<Node> children) {
        super(children);
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public Set<Node> getChildren() {
        return super.getAdjacencies();
    }

    public double getHeight() {
        return height;
    }

    // height above latest tip
    public void setHeight(double height) {
        this.height = height;
    }

    // length of branch to parent
    public double getLength() {
        return length >= 0 ? length : 1.0;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public int getDegree() {
        final int degree = (parent == null ? 0 : 1) + super.getDegree();
        return degree;
    }

    public Set<Node> getAdjacencies() {
        Set<Node> adjacencies = new HashSet<Node>();
        Set<Node> children = super.getAdjacencies();
        if (children != null) adjacencies.addAll(children);
        if (parent != null) adjacencies.add(parent);
        return adjacencies;
    }

    // PRIVATE members

   // private AttributableHelper helper = null;

    private Node parent;
    //private final Set<Node> children;
    //private final Taxon taxon;
    private double height;
    private double length;

}

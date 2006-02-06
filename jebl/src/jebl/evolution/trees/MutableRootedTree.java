package jebl.evolution.trees;

import jebl.evolution.graphs.Node;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;

/**
 * A simple rooted tree providing some ability to manipulate the tree.
 *
 *   - Root an unrooted tree using an outgroup.
 *   - Remove internal node: all children of node are adopted by it's parent.
 *   - Split/Refine node by creating two new children and distributing the children to new nodes.
 *   - Re-root a rooted tree given an outgroup.

 * @author Joseph Heled
 * @version $Id$
 *
 */

public class MutableRootedTree extends SimpleRootedTree {
    MutableRootedTree() {  super(); }

    /**
     * Construct a rooted tree from unrooted.
     *
     * @param tree      Unrooted tree to toor
     * @param outGroup  Node in tree assumed to be the outgroup
     */
    public MutableRootedTree(Tree tree, Node outGroup) {
        if( ! tree.isExternal(outGroup) ) throw new IllegalArgumentException("Outgroup must be a tip");

        // Adjacency of node to become new root.
        Node root = tree.getAdjacencies(outGroup).get(0);

        try {
            SimpleRootedNode newRoot = rootAdjaceincesWith(tree, root, outGroup);

            // Add the outgroup in
            SimpleRootedNode out = (SimpleRootedNode)createExternalNode( tree.getTaxon(outGroup) );
            setHeight(out, tree.getEdgeLength(outGroup, root));
            newRoot.addChild( out );

        } catch (NoEdgeException e) {
            // bug
        }
    }

    /**
     *  Remove internal node. Move all children to their grandparent.
     *  @param node  to be removed
     */
    public void removeInternalNode(Node node) {
        assert ! isExternal(node) && getRootNode() != node;

        SimpleRootedNode parent = (SimpleRootedNode)getParent(node);
        for( Node n : getChildren(node) ) {
            parent.addChild((SimpleRootedNode)n);
        }
        parent.removeChild(node);
        internalNodes.remove(node);
    }

    /**
     *
     * @param node     Node to refine
     * @param leftSet  indices of children in the left new subtree.
     */
    public void refineNode(Node node, int[] leftSet) {
        List<Node> allChildren = getChildren(node);

        List<Node> left = new ArrayList<Node>();
        List<Node> right = new ArrayList<Node>();

        for( int n : leftSet ) {
            left.add(allChildren.get(n));
        }
        for( Node n : allChildren ) {
            if( !left.contains(n) ) {
                right.add(n);
            }
        }
        internalNodes.remove(node);
        SimpleRootedNode saveRoot = rootNode;

        SimpleRootedNode lnode = (left.size() > 1) ? createInternalNode(left) : (SimpleRootedNode)left.get(0);
        SimpleRootedNode rnode = (right.size() > 1) ? createInternalNode(right) : (SimpleRootedNode)right.get(0);

        List<SimpleRootedNode> nodes = new ArrayList<SimpleRootedNode>(2);
        nodes.add(lnode);
        nodes.add(rnode);
        ((SimpleRootedNode)node).replaceChildren(nodes);

        rootNode = saveRoot;
    }

    /**
     *  Re-root tree using an outgroup.
     * @param outGroup
     * @param attributeNames Move those attributes (if they exist in node) to their previous parent. The idea is to
     * preserve "branch" attributes which we now store in the child since only "node" properties are supported.
     */
    public void reRootWithOutgroup(Node outGroup, Set<String> attributeNames) {
        assert isExternal(outGroup);
        reRoot((SimpleRootedNode)getAdjacencies(outGroup).get(0), attributeNames);
    }

    /**
     * Construct a rooted sub-tree from unrooted. Done recursivly: Given an internal node N and one adjacency A to become
     * the new parent, recursivly create subtrees for all adjacencies of N (ommiting A) using N as parent, and return
     * an internal node with all subtrees as children. A tip simply creates an external node and returns it.
     *
     * @param tree   Unrooted source tree
     * @param node   span sub-tree from this node
     * @param parent adjacency of node which serves as the parent.
     * @return  rooted subtree.
     * @throws NoEdgeException
     */
    private SimpleRootedNode rootAdjaceincesWith(Tree tree, Node node, Node parent) throws NoEdgeException {
        if( tree.isExternal(node) ) {
            return (SimpleRootedNode)createExternalNode( tree.getTaxon(node) );
        }

        List<Node> ch = new ArrayList<Node>();
        for( Node a : tree.getAdjacencies(node) ) {
            if( a == parent ) continue;
            SimpleRootedNode x = rootAdjaceincesWith(tree, a, node);
            setHeight(x, tree.getEdgeLength(a, node));
            ch.add(x);
        }
        return createInternalNode(ch);
    }

    /**
     * Similar to  rootAdjaceincesWith.
     * @param node
     * @param attributeNames
     */
    private void reRoot(SimpleRootedNode node, Set<String> attributeNames) {
        SimpleRootedNode parent = (SimpleRootedNode)getParent(node);
        if( parent == null) {
            return;
        }
        double len = getLength(node);
        parent.removeChild(node);
        reRoot(parent, attributeNames);
        if( parent == getRootNode() ) {
            rootNode = node;
        }

        if( parent.getChildren().size() == 1 ) {
            parent = (SimpleRootedNode)parent.getChildren().get(0);
            len += parent.getLength();
        }

        node.addChild(parent);
        parent.setLength(len);
        node.setParent(null);

        if( attributeNames != null ) {
            for( String name : attributeNames ) {
                Object s = node.getAttribute(name);
                if( s != null ) {
                    parent.setAttribute(name, s);
                    node.removeAttribute(name);
                }
            }
        }
    }
}

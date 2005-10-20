package jebl.evolution.io;

import jebl.evolution.trees.Tree;
import jebl.evolution.trees.SimpleRootedTree;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.graphs.Node;

import java.io.IOException;
import java.io.EOFException;
import java.io.Reader;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id$
 */
public class NewickImporter implements TreeImporter {

    /**
     * Constructor
     */
    public NewickImporter(Reader reader) {
        helper = new ImportHelper(reader);
    }

    public boolean hasTree() throws IOException, ImportException {
        try {
            helper.skipUntil("(");
            helper.unreadCharacter('(');
        } catch (EOFException e) {
            lastTree = null;
            return false;
        }

        return true;
    }

    public Tree importNextTree() throws IOException, ImportException {
        Tree tree = null;

        try {
            helper.skipUntil("(");
            helper.unreadCharacter('(');

            tree = readTree();
        } catch (EOFException e) { }

        lastTree = tree;

        return tree;
    }

    public List<Tree> importTrees() throws IOException, ImportException {
        List<Tree> trees = new ArrayList<Tree>();

        while (hasTree()) {
            trees.add(importNextTree());
        }

        return trees;
    }

    private Tree lastTree = null;

    private Tree readTree() throws IOException, ImportException {
        SimpleRootedTree tree = new SimpleRootedTree();

        readInternalNode(tree);

        return tree;
    }

    /**
     * Reads a branch in. This could be a node or a tip (calls readNode or readTip
     * accordingly). It then reads the branch length and SimpleNode that will
     * point at the new node or tip.
     */
    private Node readBranch(SimpleRootedTree tree) throws IOException, ImportException
    {
        double length = 0.0;
        Node branch;

        if (helper.nextCharacter() == '(') {
            // is an internal node
            branch = readInternalNode(tree);

        } else {
            // is an external node
            branch = readExternalNode(tree);
        }

        if (helper.getLastDelimiter() == ':') {
            length = helper.readDouble(",():;");
        }

        // This is a bit dirty... we are setting the lengths as node heights and
        // will then go back over the tree to set the real heights.
        tree.setLength(branch, length);

        return branch;
    }

    /**
     * Reads a node in. This could be a polytomy. Calls readBranch on each branch
     * in the node.
     */
    private Node readInternalNode(SimpleRootedTree tree) throws IOException, ImportException
    {
        Set<Node> children = new HashSet<Node>();

        // read the opening '('
        helper.readCharacter();

        // read the first child
        children.add( readBranch(tree) );

        // an internal node must have at least 2 children
        if (helper.getLastDelimiter() != ',') {
            throw new ImportException.BadFormatException("Missing ',' in tree");
        }

        // read subsequent children
        do {
            children.add( readBranch(tree) );

        } while (helper.getLastDelimiter() == ',');

        // should have had a closing ')'
        if (helper.getLastDelimiter() != ')') {
            throw new ImportException.BadFormatException("Missing closing ')' in tree");
        }

        // find the next delimiter
        helper.readToken(":(),;");

        return tree.createInternalNode(children);
    }

    /**
     * Reads an external node in.
     */
    private Node readExternalNode(SimpleRootedTree tree) throws IOException
    {
        String label = helper.readToken(":(),;");
        return tree.createExternalNode(Taxon.getTaxon(label));
    }

    private final ImportHelper helper;

}

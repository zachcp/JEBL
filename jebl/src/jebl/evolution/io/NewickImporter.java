package jebl.evolution.io;

import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.SimpleRootedTree;
import jebl.evolution.trees.Tree;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public RootedTree importNextTree() throws IOException, ImportException {
        RootedTree tree = null;

        try {
            helper.skipUntil("(");
            helper.unreadCharacter('(');

            tree = readTree();
        } catch (EOFException e) {
         }

        lastTree = tree;

        return tree;
    }

    public List<RootedTree> importTrees() throws IOException, ImportException {
        List<RootedTree> trees = new ArrayList<RootedTree>();

        while (hasTree()) {
            final RootedTree t = importNextTree();
            if( t != null ) {
              trees.add(t);
            }
        }

        return trees;
    }

    private Tree lastTree = null;

    private RootedTree readTree() throws IOException, ImportException {
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

        Node branch;

        if (helper.nextCharacter() == '(') {
            // is an internal node
            branch = readInternalNode(tree);

        } else {
            // is an external node
            branch = readExternalNode(tree);
        }

        if (helper.getLastDelimiter() == ':') {
            double length = helper.readDouble(",():;");
	        tree.setLength(branch, length);
        }

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

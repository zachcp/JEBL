package jebl.evolution.io;

import jebl.evolution.graphs.Node;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.SimpleRootedTree;
import jebl.evolution.trees.Tree;
import jebl.util.Attributable;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.awt.*;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id$
 */
public class NewickImporter implements TreeImporter {

    /**
     * Constructor
     */
    public NewickImporter(Reader reader, boolean unquotedLables) {
        helper = new ImportHelper(reader);
        this.unquotedLables = unquotedLables;
    }

    /**
     * Returns an iterator over a set of elements of type T.
     *
     * @return an Iterator.
     */
    public Iterator<Tree> iterator() {
        return new Iterator<Tree>() {

            public boolean hasNext() {
                boolean hasNext = false;
                try {
                    hasNext = hasTree();
                } catch (IOException e) {
                    // deal with errors by stopping the iteration
                } catch (ImportException e) {
                    // deal with errors by stopping the iteration
                }
                return hasNext;
            }

            public Tree next() {
                Tree tree = null;
                try {
                    tree = importNextTree();
                } catch (IOException e) {
                    // deal with errors by stopping the iteration
                } catch (ImportException e) {
                    // deal with errors by stopping the iteration
                }
                if (tree == null) throw new NoSuchElementException("No more trees in this file");
                return tree;
            }

            public void remove() {
                throw new UnsupportedOperationException("operation is not supported by this Iterator");
            }
        };
    }

    public boolean hasTree() throws IOException, ImportException {
        try {
            helper.skipUntil("(");
            helper.unreadCharacter('(');
        } catch (EOFException e) {
            return false;
        }

        return true;
    }

    public Tree importNextTree() throws IOException, ImportException {

        try {
            helper.skipUntil("(");
            helper.unreadCharacter('(');

            return readTree();
        } catch (EOFException e) {
            //
            throw new ImportException("error");
        }
    }

    public List<Tree> importTrees() throws IOException, ImportException {
        List<Tree> trees = new ArrayList<Tree>();

        while (hasTree()) {
            final Tree t = importNextTree();
            if( t != null ) {
              trees.add(t);
            }
        }

        return trees;
    }

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
        List<Node> children = new ArrayList<Node>();

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


        final Node node = tree.createInternalNode(children);

        try {
          // find the next delimiter
          String token = helper.readToken(":(),;");

            if (token.length() > 0) {
                node.setAttribute("label", parseValue(token));
            }

            // If there is a metacomment before the branch length indicator (:), then it is a node attribute
            if (helper.getLastMetaComment() != null) {
                // There was a meta-comment which should be in the form:
                // \[&label[=value][,label[=value]>[,/..]]\]
                parseMetaCommentPairs(helper.getLastMetaComment(), node);

                helper.clearLastMetaComment();
            }

        } catch( EOFException e) {
            // Ok if we just finished
        }

        return node;
    }

    static void parseMetaCommentPairs(String meta, Attributable item) throws ImportException.BadFormatException {
		// This regex should match key=value pairs, separated by commas
		// This can match the following types of meta comment pairs:
		// value=number, value="string", value={item1, item2, item3}
        // (label must be quoted if it contains spaces (i.e. "my label"=label)

        Pattern pattern = Pattern.compile("(\"[^\"]*\"+|[^,=\\s]+)\\s*(=\\s*(\\{[^=}]*\\}|\"[^\"]*\"+|[^,]+))?");
		Matcher matcher = pattern.matcher(meta);

		while (matcher.find()) {
			String label = matcher.group(1);
            if( label.charAt(0) == '\"' ) {
                label = label.substring(1, label.length() - 1);
            }
            if (label == null || label.trim().length() == 0) {
				throw new ImportException.BadFormatException("Badly formatted attribute: '"+ matcher.group()+"'");
			}
			final String value = matcher.group(2);
			if (value != null && value.trim().length() > 0) {
				// there is a specified value so try to parse it
				item.setAttribute(label, parseValue(value.substring(1)));
			} else {
				item.setAttribute(label, Boolean.TRUE);
			}
		}
	}
    /**
	 * This method takes a string and tries to decode it returning the object
	 * that best fits the data. It will recognize command delimited lists enclosed
	 * in {..} and call parseValue() on each element. It will also recognize Boolean,
	 * Integer and Double. If the value starts with a # then it will attempt to decode
	 * the following integer as an RGB colour - see Color.decode(). If nothing else fits
	 * then the value will be returned as a string but trimmed of leading and trailing
	 * white space.
	 * @param value the string
	 * @return the object
	 */
	static Object parseValue(String value) {

		value = value.trim();

		if (value.startsWith("{")) {
			// the value is a list so recursively parse the elements
			// and return an array
			String[] elements = value.substring(1, value.length() - 1).split(",");
			Object[] values = new Object[elements.length];
			for (int i = 0; i < elements.length; i++) {
				values[i] = parseValue(elements[i]);
			}
			return values;
		}

		if (value.startsWith("#")) {
			// I am not sure whether this is a good idea but
			// I am going to assume that a # denotes an RGB colour
			try {
				return Color.decode(value.substring(1));
			} catch (NumberFormatException nfe1) {
				// not a colour
			}
		}

		if (value.equalsIgnoreCase("TRUE") || value.equalsIgnoreCase("FALSE")) {
			return new Boolean(value);
		}

		// Attempt to format the value as an integer
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException nfe1) {
			// not an integer
		}

		// Attempt to format the value as a double
		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException nfe2) {
			// not a double
		}

		// return the trimmed string
		return value;
	}
    /**
     * Reads an external node in.
     */
    private Node readExternalNode(SimpleRootedTree tree) throws IOException
    {
        String label = helper.readToken(":(),;");
        while( unquotedLables && helper.getLastDelimiter() == ' ' ) {
            label = label + " " + helper.readToken(":(),;");
        }
        return tree.createExternalNode(Taxon.getTaxon(label));
    }

    private final ImportHelper helper;
    private boolean unquotedLables;
}

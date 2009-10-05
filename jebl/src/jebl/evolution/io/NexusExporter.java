package jebl.evolution.io;

import jebl.evolution.alignments.Alignment;
import jebl.evolution.distances.DistanceMatrix;
import jebl.evolution.graphs.Node;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.sequences.SequenceType;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.Tree;
import jebl.evolution.trees.Utils;
import jebl.util.Attributable;

import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Export sequences and trees to Nexus format.
 *
 * @author Joseph Heled
 *
 * @version $Id$
 */

public class NexusExporter implements AlignmentExporter, SequenceExporter, TreeExporter {

	public NexusExporter(Writer writer) {
		this(writer, true);
	}

	/**
     *
     * @param writer where export text goes
     */
    public NexusExporter(Writer writer, boolean writeMetaComments) {
		this(writer, writeMetaComments, false);
    }

    /**
     *
     * @param writer where export text goes
     */
    public NexusExporter(Writer writer, boolean writeMetaComments, boolean interleave) {
		this.writeMetaComments = writeMetaComments;
        this.interleave = interleave;
        this.writer = new PrintWriter(writer);
        this.writer.println("#NEXUS");
    }

    /**
     * exportAlignment.
     */
    public void exportAlignment(Alignment alignment) throws IOException {
    	exportSequences(alignment.getSequences());
    }

    /**
     * export alignment.
     */
    public void exportSequences(Collection<? extends Sequence> sequences) throws IOException, IllegalArgumentException {

        establishSequenceTaxa(sequences);

        SequenceType seqType = null;

        int maxLength = 0;
        for (Sequence sequence : sequences) {
            if (sequence.getLength() > maxLength) {
                maxLength = sequence.getLength();
            }
            if (seqType == null) {
                seqType = sequence.getSequenceType();
            } else if( seqType != sequence.getSequenceType() ) {
               throw new IllegalArgumentException("All seqeuences must have the same type");
            }
        }

        writer.println("begin characters;");
        writer.println("\tdimensions nchar=" + maxLength + ";");
        if( seqType != null ) {
            writer.println("\tformat datatype=" + seqType.getNexusDataType() +
                    " missing=" + seqType.getUnknownState().getName() +
                    " gap=" + seqType.getGapState().getCode() + (interleave ? " interleave=yes" : "") + ";");

            writer.println("\tmatrix");
            int maxRowLength = interleave ? MAX_ROW_LENGTH : maxLength;
            for(int n=0; n < Math.ceil((double)maxLength/maxRowLength); n++){
                for (Sequence sequence : sequences) {
                    if( sequence.getSequenceType() != seqType ) {
                        throw new IllegalArgumentException("SequenceTypes of sequences in collection do not match");
                    }
                    StringBuilder builder = new StringBuilder("\t");
                    appendTaxonName(sequence.getTaxon(), builder);
                    String sequenceString = sequence.getString();
                    builder.append("\t").append(sequenceString.subSequence(n*maxRowLength, Math.min((n+1)*maxRowLength, sequenceString.length())));
                    int shortBy = Math.min(Math.min(n*maxRowLength, maxLength) - sequence.getLength(),  maxRowLength);
                    if (shortBy > 0) {
                        for (int i = 0; i < shortBy; i++) {
                            builder.append(seqType.getGapState().getCode());
                        }
                    }
                    writer.println(builder);
                }
                writer.println();
            }
            writer.println(";\nend;");
        }
    }

    /**
     * Export a single tree
     *
     * @param tree
     * @throws java.io.IOException
     */
    public void exportTree(Tree tree) throws IOException {
        List<Tree> trees = new ArrayList<Tree>();
        trees.add(tree);
        exportTrees(trees);
    }

    // The pattern that identifiers in Nexus need to match, see http://www.cs.nmsu.edu/~epontell/nexus/nexus_grammar
    // which says: identifier -->
    //      A token satisfing the regular expression [_\w]+[\d\w\._]*. Note that an single
    //      _ is considered a valid identifier. In most contexts a single _ means a
    //      "don't care identifier", simmilar to the _ meaning in prolog.
    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("[_\\w]+[\\d\\w\\._]*");

    private void writeTrees(Collection<? extends Tree> trees, boolean checkTaxa) throws IOException {
        int nt = 0;
        for( Tree t : trees ) {
            if( checkTaxa && establishTreeTaxa(t) ) {
                throw new IllegalArgumentException();
            }
            final boolean isRooted = t instanceof RootedTree;
            final RootedTree rtree = isRooted ? (RootedTree)t : Utils.rootTheTree(t);

            final Object name = t.getAttribute(treeNameAttributeKey);

            ++nt;
            final String treeName = (name != null) ? name.toString() : "tree_" + nt;

            if (!IDENTIFIER_PATTERN.matcher(treeName).matches()) {
                // TT: Or should we put a translationList so we can use any name? But that would require more work
                // and maybe we should not do it until someone complains that their tree names get lost.
                throw new IllegalArgumentException("Tree name '" + name + "' is not a valid Nexus identifer, i.e. it doesn't match " + IDENTIFIER_PATTERN.pattern());
            }

            StringBuilder builder = new StringBuilder("\ttree ");

            builder.append(treeName);
            builder.append(" = ");

            // TREE & UTREE are depreciated in the NEXUS format in favour of a metacomment
            // [&U] or [&R] after the TREE command. Andrew.
            // TT: The [&U], [&R] should actually come *after* the " = " and be uppercase, see
            // e.g. tree_rest in http://www.cs.nmsu.edu/~epontell/nexus/nexus_grammar .
            // Before 2008-05-05 we incorrectly inserted it before the treeName.
            builder.append(isRooted && !rtree.conceptuallyUnrooted() ? "[&R] " : "[&U] ");

            appendAttributes(rtree, exportExcludeKeys, builder);

            appendTree(rtree, rtree.getRootNode(), builder);
            builder.append(";");

            writer.println(builder);
        }
    }

    public void exportTrees(Collection<? extends Tree> trees) throws IOException {
        // all trees in a set should have the same taxa
        establishTreeTaxa(trees.iterator().next());
        writer.println("begin trees;");
        writeTrees(trees, true);
        writer.println("end;");
    }

    public void exportTreesWithTranslation(Collection<? extends Tree> trees, Map<String, String> t) throws IOException {
        writer.println("begin trees;");
        writer.println("\ttranslate");
        boolean first = true;
        for( Map.Entry<String, String> e : t.entrySet() ) {
            writer.print((first ? "" : ",\n") + "\t\t" + safeName(e.getKey()) + " " + safeName(e.getValue()));
            first = false;
        }
        writer.println("\n\t;");

        writeTrees(trees, false);
        writer.println("end;");
    }

    public void exportMatrix(final DistanceMatrix distanceMatrix) {
        final List<Taxon> taxa = distanceMatrix.getTaxa();
        establishTaxa(taxa);
        writer.println("begin distances;");
        // assume distance matrix is symetric, so save upper part. no method to guarantee this yet
        final double[][] distances = distanceMatrix.getDistances();
        writer.println(" format triangle = upper nodiagonal;");
        writer.println(" matrix ");
        for(int i = 0; i < taxa.size(); ++i) {
            StringBuilder builder = new StringBuilder("\t");
            appendTaxonName(taxa.get(i), builder);
            for(int j = i+1; j < taxa.size(); ++j) {
                builder.append(" ");
                builder.append(distances[i][j]);
            }
            writer.println(builder);
        }
        writer.println(";");
        writer.println("end;");
    }

    /**
     * Write a new taxa block and record them for later reference.
     * @param taxons
     */
    private void setTaxa(Taxon[] taxons) {
        taxa = new HashSet<Taxon>();

        writer.println("begin taxa;");
        writer.println("\tdimensions ntax=" + taxons.length + ";");
        writer.println("\ttaxlabels");

        for (Taxon taxon : taxons) {
            taxa.add(taxon);

            StringBuilder builder = new StringBuilder("\t");
            appendTaxonName(taxon, builder);
            appendAttributes(taxon, null, builder);
            writer.println(builder);
        }
        writer.println(";\nend;\n");
    }

    final private String nameRegex = "^(\\w|-)+$";

    /**
     * Name suitable as token - quotes if necessary
     * @param name to check
     * @return the name
     */
    private String safeName(String name) {
        // allow dash in names

        if (!name.matches(nameRegex)) {
            name = name.replace("\'", "\'\'");
            return "\'" + name + "\'";
        }
        return name;
    }

    /**
     * name suitable for printing - quotes if necessary
     * @param taxon
     * @param builder
     * @return
     */
    private StringBuilder appendTaxonName(Taxon taxon, StringBuilder builder) {
        String name = taxon.getName();
        if (!name.matches(nameRegex)) {
            // JEBL way of quoting the quote character
            name = name.replace("\'", "\'\'");
            builder.append("\'").append(name).append("\'");
            return builder;
        }
        return builder.append(name);
    }

    /**
     * Prepare for writing an alignment. If a taxa block exists and is suitable for alignment,
     * do nothing. If not, write a new taxa block.
     * @param sequences
     */
    private void establishSequenceTaxa(Collection<? extends Sequence> sequences) {
        if( taxa != null && taxa.size() == sequences.size() ) {
            boolean hasAll = true;
            for( Sequence s : sequences ) {
                if( taxa.contains(s.getTaxon()) ) {
                    hasAll = false;
                    break;
                }
            }
            if( hasAll ) {
                return;
            }
        }

        List<Taxon> t = new ArrayList<Taxon>(sequences.size());
        for (Sequence sequence : sequences) {
            t.add(sequence.getTaxon());
        }
        setTaxa(t.toArray(new Taxon[]{}));
    }

    private boolean establishTreeTaxa(Tree tree) {
        return establishTaxa(tree.getTaxa());
    }

    private boolean establishTaxa(Collection<? extends Taxon> ntaxa) {
        if( taxa != null && taxa.size() == ntaxa.size()  && taxa.containsAll(ntaxa)) {
            return false;
        }

        setTaxa(ntaxa.toArray(new Taxon[]{}));
        return true;
    }

    /**
     * Prepare for writing a tree. If a taxa block exists and is suitable for tree,
     * do nothing. If not, write a new taxa block.
     * @param tree
     * @param node
     * @param builder
     */
    private void appendTree(RootedTree tree, Node node, StringBuilder builder) {
        if (tree.isExternal(node)) {
            appendTaxonName(tree.getTaxon(node), builder);

            appendAttributes(node, null, builder);

            if( tree.hasLengths() ) {
                builder.append(':');
                builder.append(tree.getLength(node));
            }
        } else {
            builder.append('(');
            List<Node> children = tree.getChildren(node);
            final int last = children.size() - 1;
            for (int i = 0; i < children.size(); i++) {
                appendTree(tree, children.get(i), builder);
                builder.append(i == last ? ')' : ',');
            }

            appendAttributes(node, null, builder);

            Node parent = tree.getParent(node);
            // Don't write root length. This is ignored elsewhere and the nexus importer fails
            // whet it is present.
            if (parent != null) {
                if (tree.hasLengths()) {
                    builder.append(":").append(tree.getLength(node));
                }
            }
        }
    }

    private StringBuilder appendAttributes(Attributable item, String[] excludeKeys, StringBuilder builder) {
	    if (!writeMetaComments) {
		    return builder;
	    }

        boolean first = true;
        for( String key : item.getAttributeNames() ) {
            // we should replace the explicit check for name by something more general.
            // Like a reserved character at the start (here &). however we have to worry about backward
            // compatibility so no change yet with name.
            boolean exclude = false;
            if(excludeKeys != null) {
                for(String eKey : excludeKeys) {
                    if(eKey.equals(key)) {
                        exclude = true;
                    }
                }
            }
            if( !exclude && !key.startsWith("&") ) {
                if (first) {
                    builder.append("[&");
                    first = false;
                } else {
                    builder.append(",");
                }

                if( key.indexOf(' ') < 0 ) {
                    builder.append(key);
                } else {
                    builder.append("\"").append(key).append("\"");
                }

                builder.append('=');

                Object value = item.getAttribute(key);
                appendAttributeValue(value, builder);
            }
        }
        if (!first) {
            builder.append("]");
        }

        return builder;
    }

    private StringBuilder appendAttributeValue(Object value, StringBuilder builder) {
        if (value instanceof Object[]) {
            builder.append("{");
            Object[] elements = ((Object[])value);

            if (elements.length > 0) {
                appendAttributeValue(elements[0], builder);
                for (int i = 1; i < elements.length; i++) {
                    builder.append(",");
                    appendAttributeValue(elements[i], builder);
                }
            }
            return builder.append("}");
        }

        if (value instanceof Color) {
            return builder.append("#").append(((Color)value).getRGB());
        }

        if (value instanceof String) {
            return builder.append("\"").append(value).append("\"");
        }

        return builder.append(value);
    }

    public static final String treeNameAttributeKey = "name";
    public static final String[] exportExcludeKeys = new String[] {treeNameAttributeKey, "R", "U"};

    static public boolean isGeneratedTreeName(String name) {
        return name != null && name.matches("tree_[0-9]+");
    }

    private Set<Taxon> taxa = null;
    protected final PrintWriter writer;
	private boolean writeMetaComments;
    private boolean interleave;
    public static final int MAX_ROW_LENGTH = 60;
}

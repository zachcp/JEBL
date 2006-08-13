package jebl.evolution.io;

import jebl.evolution.sequences.Sequence;
import jebl.evolution.sequences.SequenceType;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.Tree;
import jebl.evolution.trees.Utils;
import jebl.evolution.distances.DistanceMatrix;
import jebl.evolution.graphs.Node;
import jebl.util.Attributable;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.*;
import java.util.List;
import java.awt.*;

/**
 * Export sequences and trees to Nexus format.
 *
 * @author Joseph Heled
 *
 * @version $Id$
 */

public class NexusExporter implements SequenceExporter {
    /**
     *
     * @param writer where export text goes
     */
    public NexusExporter(Writer writer) {
        this.writer = new PrintWriter(writer);
        this.writer.println("#NEXUS");
    }

    /**
     * export alignment.
     */
    public void exportSequences(List<Sequence> sequences) throws IOException, IllegalArgumentException {

        establishTaxa(sequences);

        final int seqLen = sequences.get(0).getLength();
        final SequenceType seqType = sequences.get(0).getSequenceType();

        writer.println("begin characters;");
        writer.println("\tdimensions nchar=" + seqLen + ";");
        writer.println("\tformat datatype=" + seqType.getNexusDataType() +
                " missing=" + seqType.getUnknownState().getName() +
                " gap=" + seqType.getGapState().getName() + ";");
        writer.println("\tmatrix");
        for (Sequence sequence : sequences) {
            if( sequence.getSequenceType() != seqType || sequence.getLength() != seqLen ) {
                throw new IllegalArgumentException();
            }
            StringBuilder builder = new StringBuilder("\t");
            appendTaxonName(sequence.getTaxon(), builder);
            builder.append("\t").append(sequence.getString());
            writer.println(builder);
        }
        writer.println(";\nend;");
    }

    /**
     * export trees
     */
    public void exportTrees(List<? extends Tree> trees) throws IOException, IllegalArgumentException {
        // all trees in a set should have the same taxa
        establishTaxa(trees.get(0));

        writer.println("begin trees;");
        int nt = 0;
        for( Tree t : trees ) {
            if( establishTaxa(t) ) {
                throw new IllegalArgumentException();
            }
            boolean isRooted = t instanceof RootedTree;
            RootedTree rtree = isRooted ? (RootedTree)t : Utils.rootTheTree(t);

            Object name = t.getAttribute("name");

            ++nt;
            final String treeName = (name != null) ? name.toString() : "tree_" + nt;

            StringBuilder builder = new StringBuilder("\ttree [&");

            // TREE & UTREE are depreciated in the NEXUS format in favour of a metacomment
            // [&U] or [&R] after the TREE command. Andrew.
            builder.append(isRooted && !rtree.conceptuallyUnrooted() ? "r] " : "u] ");
            builder.append(treeName);
            builder.append(" = ");

            appendAttributes(rtree, "name", builder);

            appendTree(rtree, rtree.getRootNode(), builder);
            builder.append(";");

            writer.println(builder);
        }
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

    /**
     * name suitable for printing - quotes if necessary
     */
    private StringBuilder appendTaxonName(Taxon taxon, StringBuilder builder) {
        String name = taxon.getName();
        if (!name.matches("^\\w+$")) {
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
    private void establishTaxa(List<Sequence> sequences) {
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

    private boolean establishTaxa(Collection<Taxon> ntaxa) {
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
     */
    private boolean establishTaxa(Tree tree) {
        return establishTaxa(tree.getTaxa());
    }

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
                builder.append(":").append(tree.getLength(node));
            }
        }
    }

    private StringBuilder appendAttributes(Attributable item, String excludeKey, StringBuilder builder) {
        boolean first = true;
        for( String key : item.getAttributeNames() ) {
            // we should replace the explicit check for name by something more general.
            // Like a reserved character at the start (here &). however we have to worry about backward
            // compatibility so no change yet with name.
            if( excludeKey == null || !key.equals(excludeKey) ) {
                if (first) {
                    builder.append("[&");
                    first = false;
                } else {
                    builder.append(",");
                }

                builder.append(key).append('=');

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

    private Set<Taxon> taxa = null;
    protected final PrintWriter writer;
}

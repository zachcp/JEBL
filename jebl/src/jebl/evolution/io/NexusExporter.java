package jebl.evolution.io;

import jebl.evolution.sequences.Sequence;
import jebl.evolution.sequences.SequenceType;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.Tree;
import jebl.evolution.trees.Utils;
import jebl.evolution.distances.DistanceMatrix;

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

    // name suitable for printing - quotes if necessary.
    private static String safeTaxonName(Taxon tax) {
        return ImportHelper.safeName(tax.getName());
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

        for (Taxon tax : taxons) {
            taxa.add(tax);

            writer.print("\t" + safeTaxonName(tax));
        }
        writer.println(";\nend;\n");
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
            writer.print(safeTaxonName(sequence.getTaxon()) + "\t");
            writer.println(sequence.getString());
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
            StringBuilder builder = new StringBuilder();
            for( String key : t.getAttributeMap().keySet() ) {
                // we should replace the explicit check for name by something more general.
                // Like a reserved character at the start (here &). however we have to worry about backward
                // compatibility so no change yet with name.
                if( !key.equals("name") && key.charAt(0) != '&' ) {
                    Object value = t.getAttributeMap().get(key);

                    if( builder.length() > 0 ) {
                        builder.append(",");
                    }
                    builder.append(key).append('=');
	                appendAttributeValue(value, builder);
                }
            }
            final String metacomment = builder.toString();

            ++nt;
            final String treeName = (name != null) ? name.toString() : "tree_" + nt;

	        // TREE & UTREE are depreciated in the NEXUS format in favour of a metacomment
	        // [&U] or [&R] after the TREE command. Andrew.
            writer.println("\ttree [&" + (isRooted && !rtree.conceptuallyUnrooted() ? "r]" : "u]") +
		                    treeName + "=" + ((metacomment.length() > 0) ? ('[' + metacomment + ']') : "") +
                           Utils.toNewick(rtree) + ";");
        }
        writer.println("end;");
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

    public void exportMatrix(final DistanceMatrix distanceMatrix) {
        final List<Taxon> taxa = distanceMatrix.getTaxa();
        establishTaxa(taxa);
        writer.println("begin distances;");
        // assume distance matrix is symetric, so save upper part. no method to guarantee this yet
        final double[][] distances = distanceMatrix.getDistances();
        writer.println(" format triangle = upper nodiagonal;");
        writer.println(" matrix ");
        for(int i = 0; i < taxa.size(); ++i) {
            writer.print(safeTaxonName(taxa.get(i)));
            for(int j = i+1; j < taxa.size(); ++j) {
               writer.print(" ");
               writer.print(distances[i][j]);
            }
            writer.println();
        }
        writer.println(";");
        writer.println("end;");
    }

    private Set<Taxon> taxa = null;
    protected final PrintWriter writer;
}

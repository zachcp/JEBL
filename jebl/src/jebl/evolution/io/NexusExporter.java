package jebl.evolution.io;

import jebl.evolution.sequences.Sequence;
import jebl.evolution.sequences.SequenceType;
import jebl.evolution.taxa.Taxon;
import jebl.evolution.trees.RootedTree;
import jebl.evolution.trees.Tree;
import jebl.evolution.trees.Utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.*;

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

    /**
     * Prepare for writing a tree. If a taxa block exists and is suitable for tree,
     * do nothing. If not, write a new taxa block.
     * @param tree
     */
    private boolean establishTaxa(Tree tree) {
        Set<Taxon> treeTaxa = tree.getTaxa();
        if( taxa != null && taxa.size() == treeTaxa.size()  && taxa.containsAll(treeTaxa)) {
            return false;
        }

        setTaxa(treeTaxa.toArray(new Taxon[]{}));
        return true;
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
    public void writeTrees(Collection<? extends Tree> trees, boolean checkTaxa) throws IOException, IllegalArgumentException {
        int nt = 0;
        for( Tree t : trees ) {
            if( checkTaxa && establishTaxa(t) ) {
                throw new IllegalArgumentException();
            }
            boolean isRooted = t instanceof RootedTree;
            RootedTree rtree = isRooted ? (RootedTree)t : Utils.rootTheTree(t);

            Object name = t.getAttribute("name");
            String metacomment = null;
            for( Map.Entry<String, Object> e : t.getAttributeMap().entrySet() ) {
                if( !e.getKey().equals("name") ) {
                    if( metacomment == null ) {
                        metacomment = "";
                    } else {
                        metacomment += ",";
                    }
                    metacomment = metacomment + e.getKey() + '=' +
                            ImportHelper.safeName(e.getValue().toString());
                }
            }

            ++nt;
            final String treeName = (name != null) ? name.toString() : "tree_" + nt;
            writer.println("\t" + (isRooted && !rtree.conceptuallyUnrooted() ? "" : "u") + "tree " + treeName
                           + "=" + ((metacomment != null) ? ('[' + metacomment + ']') : "") +
                           Utils.toNewick(rtree) + ";");
        }
    }

    /**
     * export trees
     */
    public void exportTrees(Collection<? extends Tree> trees) throws IOException {
        // all trees in a set should have the same taxa
        establishTaxa(trees.iterator().next());
        writer.println("begin trees;");
        writeTrees(trees, true);
        writer.println("end;");
    }

    public void exportTreesWithTranslation(Collection<? extends Tree> trees, Map<String, String> t) throws IOException {
        writer.println("begin trees;");
        writer.println("\ttranslate");
        boolean first = true;
        for( Map.Entry<String, String> e : t.entrySet() ) {
            writer.print((first ? "" : ",\n") + "\t\t" + ImportHelper.safeName(e.getKey()) + " " + ImportHelper.safeName(e.getValue()));
            first = false;
        }
        writer.println("\n\t;");

        writeTrees(trees, false);
        writer.println("end;");
    }


    private Set<Taxon> taxa = null;
    private final PrintWriter writer;
}

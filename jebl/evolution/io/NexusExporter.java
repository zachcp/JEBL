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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: joseph
 * Date: 10/01/2006
 * Time: 11:16:13
 *
 * @author joseph
 * @version $Id$
 *
 * Export sequences and trees to Nexus format.
 *
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
            writer.print("\t" + tax.getName());
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
            writer.print(sequence.getTaxon().getName() + "\t");
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
        for( Tree t : trees ) {
            if( establishTaxa(t) ) {
                throw new IllegalArgumentException();
            }
            boolean isRooted = t instanceof RootedTree;
            RootedTree rtree = isRooted ? (RootedTree)t : Utils.rootTheTree(t);
            // (FIXME) no tree name yet
            writer.println("\t" + (isRooted ? "" : "u") + "tree unon=" + Utils.toNewick(rtree) + ";");
        }
        writer.println("end;");
    }

    private Set<Taxon> taxa = null;
    private final PrintWriter writer;
}

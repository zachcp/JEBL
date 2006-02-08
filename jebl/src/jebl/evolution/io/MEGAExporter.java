package jebl.evolution.io;

import jebl.evolution.alignments.Alignment;
import jebl.evolution.sequences.Sequence;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;

/**
 * * Export to MEGA.
 *
 * @author Joseph Heled
 * @version $Id$
 */
public class MEGAExporter implements AlignmentExporter {
    private PrintWriter writer;

    /**
     *
     * @param writer where export text goes
     */
    public MEGAExporter(Writer writer, String comment) {
        this.writer = new PrintWriter(writer);
        this.writer.println("#mega");
        if( comment != null ) {
            this.writer.println("!" + comment);
        }
    }

    public void exportAlignment(Alignment alignment) throws IOException {
        List<Sequence> seqs = alignment.getSequenceList();

        for( Sequence seq : seqs )  {
            writer.println();
            writer.println("#" + seq.getTaxon().getName());
            writer.println(seq.getString());
        }
    }
}
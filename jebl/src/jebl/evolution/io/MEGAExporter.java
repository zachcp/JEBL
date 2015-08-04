package jebl.evolution.io;

import jebl.evolution.alignments.Alignment;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.sequences.SequenceType;
import jebl.util.SafePrintWriter;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * * Export to MEGA.
 *
 * @author Joseph Heled
 * @version $Id$
 */
public class MEGAExporter implements AlignmentExporter {
    private SafePrintWriter writer;
    private boolean wroteHeader = false;
    private String comment;

    /**
     *
     * @param writer where export text goes
     */
    public MEGAExporter(Writer writer, String comment) {
        this.writer = new SafePrintWriter(writer);
        this.comment = comment;
    }

    /**
     * Writes the header if we haven't already done so. ideally we'd just do this in the constructor, but we don't want to break the API by adding IOException to constructor the signature
     */
    private void writeHeaderIfNecessary() throws IOException {
        if (wroteHeader)
            return;
        wroteHeader = true;
        this.writer.println("#mega");
        if( comment != null ) {
            this.writer.println("!" + comment);
        }
    }

    /**
     * 
     * @param alignment the alignment to export
     * @param name the name of the alignment
     * @throws IOException
     */
    public void exportAlignment(Alignment alignment, String name) throws IOException {
        writeHeaderIfNecessary();
        writer.print("!Title ");
        writer.print(name);
        writer.println(";");

        writer.print("!Format DataType=");
        String dataType =
                alignment.getSequenceType() == SequenceType.NUCLEOTIDE?
                "nucleotide": "protein";
        writer.println(dataType + ";");
        exportAlignment(alignment);
    }

    /**
     * @deprecated Files created by this export method won't be importable by MEGA (because they don't have titles).  Use {@link #exportAlignment(jebl.evolution.alignments.Alignment, String)}  instead.
     * @param alignment
     * @throws IOException
     */
    public void exportAlignment(Alignment alignment) throws IOException {
        writeHeaderIfNecessary();
        List<Sequence> seqs = alignment.getSequenceList();

        for( Sequence seq : seqs )  {
            writer.println();
            writer.println("#" + seq.getTaxon().getName().replaceAll(" ","_"));
            writer.println(seq.getString());
        }
    }
}
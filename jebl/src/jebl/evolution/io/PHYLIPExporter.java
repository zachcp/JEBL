package jebl.evolution.io;

import jebl.evolution.sequences.Sequence;
import jebl.evolution.alignments.Alignment;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.io.IOException;
import java.io.Writer;
import java.io.PrintWriter;

/**
 * Export alignment to Phylip format.
 *
 * Must be one of the most braindead format around. Try to output something that hopefuly any
 * "Phylip suppor" variant can read - up to 9 name chars, followed by a blank, followed by 
 * sequence on one line.
 *
 * @author Joseph Heled
 * @version $Id$
 */
public class PHYLIPExporter implements AlignmentExporter {
    private PrintWriter writer;

    /**
     *
     * @param writer where export text goes
     */
    public PHYLIPExporter(Writer writer) {
        this.writer = new PrintWriter(writer);
    }

    private boolean namesUnique(List<String> names) {
        Set<String> all = new HashSet<String>();
        for( String name : names ) {
            if( all.contains(name) ) {
                return false;
            }
            all.add(name);
        }
        return true;
    }

    private List<String> tryNames(List<String> names, int fromBegining, int fromEnd) {
        List<String> pnames = new ArrayList<String>(names.size());
        final int total = fromBegining + fromEnd;
        for( String name : names ) {
            final int len = name.length();
            String n;

            if( len <= total ) {
                n = name + ((len<total) ? String.format("%" + (total-len) + "s", " ") : "");
            } else {
              n = name.substring(0, fromBegining) + name.substring(len - fromEnd, len);
            }
            pnames.add(n);
        }
        if( namesUnique(pnames) ) {
            return pnames;
        }
        return null;
    }

    private List<String> phylipNames(List<Sequence> seqs) {
       List<String> names = new ArrayList<String>();
        for( Sequence s : seqs ) {
           names.add(s.getTaxon().getName());
        }

        List<String> pnames = tryNames(names, 9, 0);
        if( pnames == null ) {
            pnames = tryNames(names, 0, 9);
        }
        if( pnames == null ) {
             pnames = tryNames(names, 5, 4);
        }

        if( pnames == null ) {
            final int nDig = (int)Math.ceil(Math.log10(names.size()));
            pnames = new ArrayList<String>(names.size());
            for(int i = 0; i < names.size(); ++i) {
                String f = "%" + (9-nDig) + "." + (9-nDig) + "s%0" + nDig + "d";
                pnames.add(String.format(f, names.get(i), i));
            }
        }
        return pnames;
    }

    public void exportAlignment(Alignment alignment) throws IOException {
        List<Sequence> seqs = alignment.getSequenceList();
        writer.println(" " + seqs.size() + " " + seqs.get(0).getLength());
        List<String> names = phylipNames(seqs);

        for(int i = 0; i < seqs.size(); ++i) {
            writer.print(names.get(i) + " ");
            writer.println(seqs.get(i).getString());
        }
    }
}

package jebl.evolution.alignments;

import jebl.evolution.sequences.BasicSequence;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.sequences.SequenceType;
import jebl.evolution.sequences.State;
import jebl.evolution.taxa.Taxon;
import jebl.math.Random;

import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: joseph
 * Date: 15/01/2006
 * Time: 10:13:50
 *
 * @author Joseph Heled
 * @version $Id$
 *
 */
public class BootrappedAlignment implements Alignment {
    private BasicAlignment alignment;

    public BootrappedAlignment(Alignment a) {
        final int nSites = a.getSiteCount();
        final int nSeqs = a.getSequences().size();

        // Work directly with states (fastest)
        State[][] newSeqs = new State[nSeqs][];

        for(int k = 0; k < nSeqs; ++k) {
            newSeqs[k] = new State[nSites];
        }

        final List<Sequence> seqs = a.getSequenceList();
        for(int n = 0; n < nSites; ++n) {
            final int fromSite = Random.nextInt(nSites);
            for(int k = 0; k < nSeqs; ++k) {
                newSeqs[k][n] = seqs.get(k).getState(fromSite);
            }
        }

        Sequence[] s = new Sequence[nSeqs];
        for(int k = 0; k < nSeqs; ++k) {
            Sequence src = seqs.get(k);
            s[k] = new BasicSequence(src.getSequenceType(), src.getTaxon(), newSeqs[k]);
        }
        alignment = new BasicAlignment(s);
    }

    public List<Sequence> getSequenceList() {
        return alignment.getSequenceList();
    }

    public int getPatternCount() {
        return alignment.getPatternCount();
    }

    public int getPatternLength() {
        return alignment.getPatternLength();
    }

    public List<Pattern> getPatterns() {
        return alignment.getPatterns();
    }

    public List<Taxon> getTaxa() {
        return alignment.getTaxa();
    }

    public SequenceType getSequenceType() {
        return alignment.getSequenceType();
    }

    public int getSiteCount() {
        return alignment.getSiteCount();
    }

    public Set<Sequence> getSequences() {
        return alignment.getSequences();
    }

    public Sequence getSequence(Taxon taxon) {
        return alignment.getSequence(taxon);
    }
}

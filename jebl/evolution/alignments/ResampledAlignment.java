package jebl.evolution.alignments;

import jebl.evolution.sequences.BasicSequence;
import jebl.evolution.sequences.Sequence;
import jebl.evolution.sequences.SequenceType;
import jebl.evolution.sequences.State;
import jebl.evolution.taxa.Taxon;

import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: joseph
 * Date: 17/01/2006
 * Time: 08:08:44
 *
 * @author Joseph Heled
 * @version $Id$
 *
 */
public class ResampledAlignment implements Alignment {
    protected BasicAlignment alignment;

    public void init(Alignment srcAlignment, int[] siteIndices) {
        final int nNewSites = siteIndices.length;
        final int nSeqs = srcAlignment.getSequences().size();

        // Work directly with states (fastest)
        State[][] newSeqs = new State[nSeqs][];

        for(int k = 0; k < nSeqs; ++k) {
            newSeqs[k] = new State[nNewSites];
        }

        final List<Sequence> seqs = srcAlignment.getSequenceList();
        for(int n = 0; n < nNewSites; ++n) {
            final int fromSite = siteIndices[n];
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

package jebl.evolution.aligners;

import jebl.evolution.sequences.Sequence;
import jebl.evolution.alignments.Alignment;

import java.util.Collection;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id$
 */
public interface Aligner {

    Alignment alignSequences(Collection<Sequence> sequences);

}

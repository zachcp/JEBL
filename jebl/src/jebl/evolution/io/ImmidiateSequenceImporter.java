package jebl.evolution.io;

import jebl.evolution.sequences.Sequence;

import java.io.IOException;

/**
 *
 * A sequence importer sending the sequences back one by one, which makes it
 * possible to import larger documents if handled wisely on the other side.
 * 
 * @author Joseph Heled
 * @version $Id$
 *
 */
public interface ImmidiateSequenceImporter {
    public interface Callback {
        void add(Sequence seq);
    }

    void importSequences(Callback callback) throws IOException, ImportException;
}

package jebl.evolution.io;

import jebl.evolution.sequences.Sequence;
import jebl.util.ProgressListener;

import java.io.IOException;

/**
 *
 * A sequence importer sending the sequences back one by one, which makes it
 * possible to import larger documents if handled wisely on the other side.
 * 
 * @author Joseph Heled
 *
 */
public interface ImmediateSequenceImporter {
    public interface Callback {
        void add(Sequence seq);
    }

    void importSequences(Callback callback, ProgressListener progressListener) throws IOException, ImportException;
}

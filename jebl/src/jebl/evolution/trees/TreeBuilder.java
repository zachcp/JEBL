package jebl.evolution.trees;

import jebl.util.ProgressListener;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 */
public interface TreeBuilder<T extends Tree> {

    T build();

    void addProgressListener(ProgressListener listener);

    void removeProgressListener(ProgressListener listener);
}

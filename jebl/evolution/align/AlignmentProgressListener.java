package jebl.evolution.align;

/**
 * @author Matt
 * @version $Id$
 */
public interface AlignmentProgressListener {
    /**
     *
     * @param fractionCompleted a number between 0 and 1 inclusive
     * representing the fraction of the operation completed.
     * If you are unsure of the fraction completed, set this parameter to 0.
     * @return returns true if the user has requested that this operation be cancelled.
     */
    public boolean setProgress(double fractionCompleted);
}
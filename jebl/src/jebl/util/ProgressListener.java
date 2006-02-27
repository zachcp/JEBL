package jebl.util;

/**
 * @author Matt
 *
 * @version $Id$
 */
public interface ProgressListener {
    /**
     *
     * @param fractionCompleted a number between 0 and 1 inclusive
     * representing the fraction of the operation completed.
     * If you are unsure of the fraction completed, set this parameter to 0.
     * @return returns true if the user has requested that this operation be cancelled.
     */
    boolean setProgress(double fractionCompleted);

    /**
     * Set visible user message.
     * @param message
     * @return true if the user has requested that this operation be cancelled.
     */
    boolean setMessage(String message);
}
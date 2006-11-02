package jebl.util;

/**
 * @author Matt Kearse
 *
 * @version $Id$
 */
public abstract class ProgressListener {
    /**
     * @param fractionCompleted a number between 0 and 1 inclusive
     * representing the fraction of the operation completed.
     * If you are unsure of the fraction completed, set this parameter to 0.
     * @return returns true if the user has requested that this operation be cancelled.
     */
    public abstract boolean setProgress(double fractionCompleted);

    /**
     * Set visible user message.
     * @param message
     * @return true if the user has requested that this operation be cancelled.
     */
    public abstract boolean setMessage(String message);

    /**
     * @return true if the user has requested that this operation be cancelled.
     */
    public abstract boolean isCancelled ();

    /**
     * Sets as an indefinite progress listener.
     * @return true if the user has requested that this operation be cancelled.
     */
    public abstract boolean setIndefiniteProgress();

    /**
     * a useful class to use when you don't care about the progress results or cancelling the operation.
     */

    public static final ProgressListener EMPTY_PROGRESS_LISTENER=new EmptyProgressListener();

    private static class EmptyProgressListener extends ProgressListener {
        public boolean setProgress(double fractionCompleted) {
            return false;
        }

        public boolean setMessage(String message) {
            return false;
        }

        public boolean isCancelled() {
            return false;
        }

        public boolean setIndefiniteProgress() {
            return false;
        }
    }
}
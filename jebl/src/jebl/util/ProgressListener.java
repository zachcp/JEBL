package jebl.util;

/**
 * @author Matt
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

    // we may consider using a public final ProgressListener like this and
    // deprecate the class EmptyProgressListener below
    private static final ProgressListener EMPTY = new ProgressListener() {
        public boolean setMessage(String message) { return false; }
        public boolean setProgress(double fractionCompleted) { return false; }
    };

    /**
     * a useful class to use when you don't care about the progress results or cancelling the operation.
     */
    public static class EmptyProgressListener extends ProgressListener {
        public boolean setProgress(double fractionCompleted) {
            return false;
        }

        public boolean setMessage(String message) {
            return false;
        }
    }
}
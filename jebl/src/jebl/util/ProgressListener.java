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

    private static final ProgressListener EMPTY = new ProgressListener() {
        public boolean setMessage(String message) { return false; }
        public boolean setProgress(double fractionCompleted) { return false; }
    };

    /**
     * @return A ProgressListener that never gets aborted. Although ProgressListener
     *         is immutable, it is not guaranteed whether this static factory method
     *         will return the same object on each invocation.   
     */
    public static ProgressListener empty() {
        return EMPTY;
    }

    /**
     * This class is deprecated. Use static factory method empty() instead.
     *
     * Old API documenation follows:
     *
     * a useful class to use when you don't care about the progress results or cancelling the operation.
     */
    @Deprecated
    public static class EmptyProgressListener extends ProgressListener {
        public boolean setProgress(double fractionCompleted) {
            return false;
        }

        public boolean setMessage(String message) {
            return false;
        }
    }
}
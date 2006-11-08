package jebl.util;

/**
 * @author Matt Kearse
 *
 * @version $Id$
 *
 * ProgressListener guarantees the following contract:
 *
 *   A call to any of the methods setProgress(), setMessage(), isCanceled() and
 *   setIndeterminateProgress() at a given time yields the same result as a call
 *   to another of these methods would have resulted at the same time.
 *
 *   Once the task whose progress we are observing has been canceled, calls
 *   to either of these methods reflect this. This does not prevent subclasses
 *   from introducing a way to "reset" a ProgressListener that was previously
 *   canceled from not being canceled any more.
 *
 * Any object may exhibit undefined behaviour when dealing with a ProgressListener 
 * that is not fulfilling this contract.
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
     * @return true if the user has requested that this operation be canceled.
     */
    public abstract boolean setMessage(String message);

    /**
     * @return true if the user has requested that this operation be canceled.
     */
    public abstract boolean isCanceled();

    /**
     * Sets indefinite progress (i.e. "some progress has happened, but I don't
     * know how close we are to finishing").
     * @return true if the user has requested that this operation be canceled.
     */
    public abstract boolean setIndeterminateProgress();

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

        public boolean isCanceled() {
            return false;
        }

        public boolean setIndeterminateProgress() {
            return false;
        }
    }
}
package jebl.util;

import java.awt.*;

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
public abstract class ProgressListener implements Cancelable { // TT: Should we let ProgressListener implement Cancelable or shouldn't we?
    /**
     * @param fractionCompleted a number between 0 and 1 inclusive
     * representing the fraction of the operation completed.
     * If you are unsure of the fraction completed, call {@link #setIndeterminateProgress} instead.
     *
     * @return true if the user has requested that this operation be canceled.
     */
    public final boolean setProgress(double fractionCompleted) {
//        System.out.println("setProgress "+fractionCompleted);
        if (fractionCompleted<0)
            assert false:"Progress must be >=0 but got "+fractionCompleted;
        if (fractionCompleted > 1.0000001) { // Allow 1.0000001 to handle rounding errors
            assert false:"Progress must be <=1 but got "+fractionCompleted;
        }
        if (fractionCompleted>1)
            fractionCompleted = 1;
        _setProgress(fractionCompleted);
        return isCanceled();
    }

    /**
     * Same as calling {@link #setProgress(double) setProgress(((double)currentStep)/numberOfSteps)}
     * @param currentStep between 0 and numberOfSteps inclusive
     * @param numberOfSteps the total number of steps. Must be greater than 0.
     * @return true if the user has requested that this operation be canceled.
     */
    public final boolean setProgress(int currentStep, int numberOfSteps) {
        return setProgress((long)currentStep, (long)numberOfSteps);
    }

    /**
     * Same as calling {@link #setProgress(double) setProgress(((double)currentStep)/numberOfSteps)}
     * @param currentStep between 0 and numberOfSteps inclusive
     * @param numberOfSteps the total number of steps. Must be greater than 0.
     * @return true if the user has requested that this operation be canceled.
     */
    public final boolean setProgress(long currentStep, long numberOfSteps) {
        if (numberOfSteps<=0) throw new IllegalArgumentException("numberOfSteps="+numberOfSteps);
        if (currentStep<0 || currentStep>numberOfSteps)  throw new IllegalArgumentException("currentStep must be between 0 and numberOfSteps inclusive.");
        double progress  = ((double)currentStep)/numberOfSteps;
        return setProgress(progress);
    }

    /**
     * Marks the operation as completed; same as {@link #setProgress(double) setProgress(1.0)}.
     * @return true if the user has requested that this operation be canceled.
     */
/*    public final boolean setComplete() {
        return setProgress(1.0);
    }*/

    /**
     * This method is a hook called from {@link #setProgress} to allow subclasses a
     * custom reaction to setProgress events. Currently, subclasses are required to
     * implement this method, but in the future it may get an empty default
     * implementation to make it optional for subclasses to subscribe to setProgress
     * events.
     */
    protected abstract void _setProgress(double fractionCompleted);

    /**
     * Sets indefinite progress (i.e. "some progress has happened, but I don't
     * know how close we are to finishing").
     * @return true if the user has requested that this operation be canceled.
     */
    public final boolean setIndeterminateProgress() {
        _setIndeterminateProgress();
        return isCanceled();
    }

    /**
     * This method is a hook called from {@link #setIndeterminateProgress} to
     * allow subclasses a custom reaction to setIndeterminateProgress events.
     * Currently, subclasses are required to implement this method, but in the
     * future it may get an empty default implementation to make it optional
     * for subclasses to subscribe to setIndeterminateProgress events.
     */
    protected abstract void _setIndeterminateProgress();

    /**
     * Set visible user message.
     * @param message a user visible message. If this is null, it will be automatically replaced with an empty string.
     * @return true if the user has requested that this operation be canceled.
     */
    public final boolean setMessage(String message) {
        _setMessage(message == null ? "" : message);
        return isCanceled();
    }

    /**
     * Set an image associated with the current progress. A progress listener
     * may choose to optionally display this image wherever is appropriate.
     * @param image an image
     * @return true if the user has requested that this operation be canceled.
     */
    public final boolean setImage(Image image) {
        _setImage(image);
        return isCanceled();
    }

    /**
     *
     * This method is a hook called from {@link #setImage} to allow subclasses a
     * custom reaction to setImage events
     *
     * @param image the image
     */
    protected void _setImage(Image image) {

    }

    /**
     * Equivalent to {@link #addFeedbackAction(String, String, org.virion.jam.util.SimpleListener) addFeedbackAction(label,"",listener)}
     */
    public void addFeedbackAction(String label, SimpleListener listener) {

    }

    /**
     * Adds an action that can choose to provide feedback. For example,
     * an operation may choose to provide a "Skip to next step" button
     * alongside the cancel button. There is no requirement that a
     * ProgressListener actually present this to the user - it may choose
     * to ignore this method, in which case <code> listener </code> will
     * never be fired.
     * @param label a label describing this feedback action. For example, "Skip to next step"
     * @param listener a listener to be notified when the user chooses to invoke
     *                 this action
     */
    public void addFeedbackAction(String label, String description, SimpleListener listener) {
        addFeedbackAction(label, listener);
    }

    /**
     * Removes a feedback action previously added using
     * {@link #addFeedbackAction(String, org.virion.jam.util.SimpleListener)}.
     * @param label The  label used as a parameter to {@link #addFeedbackAction(String, org.virion.jam.util.SimpleListener)}
     */
    public void removeFeedbackAction(String label) {

    }

    /**
     * Sets a title associated with whatever is being done. This will not necessarily even be presented to the user,
     * but typically will be presented as the title of a progress window.
     * @param title the title of a progress window (if any). Must not be null.
     */
    public void setTitle(String title) {

    }

    /**
     * This method is a hook called from {@link #setMessage} to allow subclasses a
     * custom reaction to setMessage events. Currently, subclasses are required to
     * implement this method, but in the future it may get an empty default
     * implementation to make it optional for subclasses to subscribe to setMessage
     * events.
     * @param message a user visible message. Will not be null but may be an empty string.
     */
    protected abstract void _setMessage(String message);

    /**
     * This method must be implemented by all subclasses. It is called from
     * {@link #setProgress}, {@link #setIndeterminateProgress} and {@link #setMessage}
     * to determine the return value of these methods.
     *
     * @return true if the user has requested that this operation be canceled.
     */
    public abstract boolean isCanceled();

    /* TT: commented this method out for now because Matt voted to have
     * ProgressListener implement Cancelable (see bug 3515). We may still decide
     * to put it back in later on.
     *
     * Returns a Cancelable wrapper around this ProgressListener that can be used
     * only to test whether this task has been canceled, but not to report any
     * progress (its concrete class is not ProgressListener, so it cannot be cast
     * back to ProgressListener). This is useful to keep exclusive responsibility for
     * progress report even if several different objects or threads may be
     * interested in finding out when the task is canceled.
     * @return A Cancelable wrapper around this ProgressListener from which this ProgressListener cannot be extracted.
     */
    /*public final Cancelable asCancelable() {
        return new Cancelable() {
            public boolean isCanceled() {
                return ProgressListener.this.isCanceled();
            }
        };
    } */

    /**
     * A ProgressListener that ignores all events and always returns false from
     * {@link #isCanceled}. Useful when you don't care about the progress
     * results or canceling the operation.
     */
    public static final ProgressListener EMPTY = new EmptyProgressListener();

    private static class EmptyProgressListener extends ProgressListener {
        protected void _setProgress(double fractionCompleted) {
        }

        protected void _setMessage(String message) {
        }

        public boolean isCanceled() {
            return false;
        }

        protected void _setIndeterminateProgress() {
        }
    }

    /**
     * A decorator progress listener which delegates all method calls to an internal progress listener.
     */
    public static class Wrapper extends ProgressListener {

        private final ProgressListener internalProgressListener;

        /**
         *
         * @param internalProgressListener progress listener that all method calls are forwarded to.
         */
        public Wrapper(ProgressListener internalProgressListener) {
            this.internalProgressListener = internalProgressListener;
        }

        protected void _setProgress(double fractionCompleted) {
            internalProgressListener._setProgress(fractionCompleted);
        }

        protected void _setIndeterminateProgress() {
            internalProgressListener._setIndeterminateProgress();
        }

        protected void _setMessage(String message) {
            internalProgressListener._setMessage(message);
        }

        public boolean isCanceled() {
            return internalProgressListener.isCanceled();
        }

        @Override
        protected void _setImage(Image image) {
            internalProgressListener._setImage(image);
        }

        @Override
        public void addFeedbackAction(String label, SimpleListener listener) {
            internalProgressListener.addFeedbackAction(label, listener);
        }

        @Override
        public void addFeedbackAction(String label, String description, SimpleListener listener) {
            internalProgressListener.addFeedbackAction(label, description, listener);
        }

        @Override
        public void removeFeedbackAction(String label) {
            internalProgressListener.removeFeedbackAction(label);
        }

        @Override
        public void setTitle(String title) {
            internalProgressListener.setTitle(title);
        }
    }
}
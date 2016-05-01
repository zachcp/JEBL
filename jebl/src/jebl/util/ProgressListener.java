package jebl.util;

import org.virion.jam.util.SimpleListener;

import java.awt.*;
import java.util.*;

/**
 * Provides feedback on the progress of an operation and allows the operation to be cancelled part way through.
 * <p/>
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
 * @author Matt Kearse
 *
 */
public abstract class ProgressListener implements Cancelable {
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
        if (numberOfSteps<=0)
            throw new IllegalArgumentException("numberOfSteps="+numberOfSteps);
        if (currentStep<0 || currentStep>numberOfSteps)
            throw new IllegalArgumentException("currentStep must be between 0 and numberOfSteps inclusive. Got "+currentStep+" of "+numberOfSteps);
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

    /**
     * Splits a progress listener into a number of separate progress listeners. Each returned progress listener may
     * independently set their progress and the total progress set by all returned progress listeners is summed
     * and forwarded to progressListener.
     * <p/>
     * Also consider using {@link CompositeProgressListener} which is easier to use in most situations
     * <p/>
     * The returned progress listeners are not thread safe.
     * @param progressListener the parent progress listener to forward the sum of the returned progress listeners to.
     * @param numberOfEvenlyWeightedPartialTasks the number of evenly weighted progress listeners to split it into.
     * @return numberOfEvenlyWeightedPartialTasks evenly weighted progress listeners.
     */
    public static java.util.List<ProgressListener> createSplitProgress(ProgressListener progressListener, int numberOfEvenlyWeightedPartialTasks) {
        java.util.List<ProgressListener> progressListeners = new ArrayList<ProgressListener>(numberOfEvenlyWeightedPartialTasks);
        double [] totalProgress = new double[1];
        double singleWeight = 1.0/numberOfEvenlyWeightedPartialTasks;
        for (int i = 0; i < numberOfEvenlyWeightedPartialTasks; i++) {
            progressListeners.add(new PartialProgress(totalProgress, singleWeight, progressListener));
        }
        return progressListeners;
    }

    /**
     * Splits a progress listener into a number of separate progress listeners weighted according to partialWeights.
     * Each returned progress listener may
     * independently set their progress and the total progress set by all returned progress listeners is summed
     * and forwarded to progressListener.
     * <p/>
     * Also consider using {@link CompositeProgressListener} which is easier to use in most situations
     * <p/>
     * The returned progress listeners are not thread safe.
     * @param progressListener the parent progress listener to forward the sum of the returned progress listeners to.
     * @param partialWeights the relative partial weights of the progress listeners to return. This weights do not need to sum to 1.0.
     * @return partialWeights.length weighted progress listeners.
     */
    public static java.util.List<ProgressListener> createSplitProgress(ProgressListener progressListener, double... partialWeights) {
        double totalWeight = 0;
        for (double partialWeight : partialWeights) {
            totalWeight+=partialWeight;
        }
        double[] normalizedWeights = new double[partialWeights.length];
        for (int i = 0; i < partialWeights.length; i++) {
            normalizedWeights[i]=partialWeights[i]/totalWeight;
        }
        return createSplitProgressForNormalizedWeights(progressListener, normalizedWeights);
    }

    /**
     * @param progressListener a progress listener returned from a previous call to {@link #createSplitProgress(ProgressListener, double...)}
     * @return the current progress for the given progress listener which must have been returned from a previous call to {@link #createSplitProgress(ProgressListener, double...)}
     */
    public static double getSplitProgress(ProgressListener progressListener) {
        if (progressListener instanceof PartialProgress) {
            PartialProgress partialProgress = (PartialProgress) progressListener;
            return partialProgress.thisProgressSoFar;
        }
        else {
            throw new IllegalArgumentException("progressListener was not returned from SplitProgress");
        }
    }

    private static java.util.List<ProgressListener> createSplitProgressForNormalizedWeights(ProgressListener progressListener, double[] partialWeightsWhichSumTo1) {
        java.util.List<ProgressListener> progressListeners = new ArrayList<ProgressListener>(partialWeightsWhichSumTo1.length);
        double [] totalProgress = new double[1];
        for (double splitWeight : partialWeightsWhichSumTo1) {
            progressListeners.add(new PartialProgress(totalProgress, splitWeight, progressListener));
        }
        return progressListeners;
    }

    private static class PartialProgress extends ProgressListener {
        final double [] totalProgress; // We use this 1 element array rather than an AtomcReference<Double> to improve performance by avoiding unnecessary sychronzation
        final double thisWeight;
        double thisProgressSoFar;
        double thisWeightedProgressSoFar;
        final ProgressListener fullProgressListener;

        private PartialProgress(double[] totalProgress, double thisWeight, ProgressListener fullProgressListener) {
            this.totalProgress = totalProgress;
            this.thisWeight = thisWeight;
            this.fullProgressListener = fullProgressListener;
        }

        protected void _setProgress(double fractionCompleted) {
            if (fractionCompleted<0 || fractionCompleted>1.001) { // We allow going a little over 1.0 due to double accuracy issues when summing lots of partial weights (e.g. I got 1.0000000000000002) once.
                assert false:"FractionComplete="+fractionCompleted;
            }
            if (fractionCompleted>1.0)
                fractionCompleted = 1.0;
            if (fractionCompleted< thisProgressSoFar) {
                assert false:"Progress went backwards from "+ thisProgressSoFar +" to "+fractionCompleted;
            }
            thisProgressSoFar = fractionCompleted;
            double weightedProgress = thisWeight * fractionCompleted;
            double newWeightedProgress = weightedProgress - thisWeightedProgressSoFar;
            thisWeightedProgressSoFar = weightedProgress;
            totalProgress[0]+=newWeightedProgress;
            double fullProgress = totalProgress[0];
            if (fullProgress<0 || fullProgress>1.001) {
                throw new IllegalStateException("fullProgress="+fullProgress);
            }
            if (fullProgress>1.0)
                fullProgress = 1.0;
            fullProgressListener.setProgress(fullProgress);
        }

        protected void _setIndeterminateProgress() {
            // Ignored since just because 1 part wants indeterminate progress, doesn't mean they all should.
        }

        protected void _setMessage(String message) {
            fullProgressListener.setMessage(message);
        }

        public boolean isCanceled() {
            return fullProgressListener.isCanceled();
        }
    }

    /**
     * Creates a ProgressListener that delegates its {@link #isCanceled()} method to the provided Cancelable and
     * does nothing for all other methods
     * @param cancelable the cancelable to delegate {@link jebl.util.ProgressListener#isCanceled()} to
     * @return a ProgressListener that delegates its {@link #isCanceled()} method to the provided Cancelable
     */
    public static ProgressListener forCancelable(final Cancelable cancelable) {
        return new ProgressListener() {
            @Override
            protected void _setProgress(double fractionCompleted) {
            }

            @Override
            protected void _setIndeterminateProgress() {
            }

            @Override
            protected void _setMessage(String message) {
            }

            @Override
            public boolean isCanceled() {
                return cancelable.isCanceled();
            }
        };
    }
}

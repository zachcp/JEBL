package jebl.util;

/**
 * @author Tobias Thierer
 * @version $Id$
 *          <p/>
 *          created on 17/03/2006 11:15:00
 *          <p/>
 *          This is a progress listener that is suitable for a task that consists of several subtasks.
 *          You specfiy the relative duration of each subtask, and then the subtasks' setProgress()
 *          calls with values between 0 and 1 are translated to reflect the overall progress on the whole
 *          (combined) task.
 *          <p/>
 *          As the combined progress listener cannot know which subtask it is currently being called from,
 *          you have to explicitely let it know when a new subtask (not the first) starts, by calling
 *          startNextOperation(). Thus when the constructor is passed an array of N doubles as its second
 *          argument, startNextOperation() should be called precisely N-1 times.
 */
public final class CompositeProgressListener implements ProgressListener {
    protected int numOperations;
    protected ProgressListener listener;
    protected int currentOperationNum = 0;
    protected double[] time;
    protected double baseTime = 0.0; // overall progress (0..1) at the start of the current sub-operation
    protected boolean aborted = false;
    protected double currentOperationProgress = 0.0;

    public CompositeProgressListener(ProgressListener listener, double[] operationDuration) {
        numOperations = operationDuration.length;
        if (numOperations == 0) {
            throw new IllegalArgumentException();
        }
        if (listener == null) {
            this.listener = new ProgressListener() {
                public boolean setProgress(double fractionCompleted) {
                    return false; // do nothing
                }

                public boolean setMessage(String message) {
                    return false;  // do nothing
                }
            };
        } else {
            this.listener = listener;
        }
        this.time = operationDuration.clone();

        // scale times to a sum of 1
        double totalTime = 0.0;
        for (double d : operationDuration)
            totalTime += d;
        for (int i = 0; i < numOperations; i++)
            this.time[i] = (operationDuration[i] / totalTime);
    }

    public boolean isAborted() {
        return aborted;
    }

    public boolean setMessage(String message) {
        boolean result = listener.setMessage(message);
        if (result) aborted=true;
        return result;
    }

    public boolean addProgress(double fractionCompletedDiff) {
        return setProgress(currentOperationProgress + fractionCompletedDiff);
    }

    public boolean setProgress(double fractionCompleted) {
        currentOperationProgress = fractionCompleted;
        boolean result = listener.setProgress(baseTime + fractionCompleted * time[currentOperationNum]);
        if (result) aborted=true;
        return result;
    }

    public boolean setComplete() {
        return setProgress(1.0);
    }

    public boolean hasNextOperation() {
        return (currentOperationNum < (numOperations - 1));
    }

    /**
     * Convenience method to start the next operation AND set a new message.
     * @param message message to set (will be passed to setMessage()
     */
    public void startNextOperation(String message) {
        startNextOperation();
        setMessage(message);
    }

    public void startNextOperation() {
        setComplete();
        if (!hasNextOperation()) {
            throw new IllegalStateException(currentOperationNum + " " + numOperations);
        }
        baseTime += time[currentOperationNum];
        currentOperationNum++;
        currentOperationProgress = 0.0;
    }
}

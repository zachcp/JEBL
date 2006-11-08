package jebl.evolution.align;

import jebl.util.ProgressListener;

/**
 * @author Matt Kearse
 * @version $Id$
 */
public class CompoundAlignmentProgressListener  {
    private boolean cancelled = false;
    private int sectionsCompleted = 0;
    private int totalSections;
    private ProgressListener progress;
    private int sectionSize= 1;

    public CompoundAlignmentProgressListener(ProgressListener progress, int totalSections) {
        this.totalSections = totalSections;
        this.progress = progress;
    }

    public void setSectionSize(int size) {
        this.sectionSize = size;
    }

    public void incrementSectionsCompleted(int count) {
        sectionsCompleted += count;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public ProgressListener getMinorProgress() {
        return minorProgress;
    }

    ProgressListener minorProgress = new ProgressListener() {

        public boolean setProgress(double fractionCompleted) {
//            System.out.println("progress =" + fractionCompleted+ " sections =" + sectionsCompleted+ "/" + totalSections);
            double totalProgress = (sectionsCompleted + fractionCompleted*sectionSize) / totalSections;
            if( totalProgress > 1.0 ) {
                System.out.println(totalProgress);
            }
            if (progress.setProgress(totalProgress)) cancelled = true;
            return cancelled;
        }

        public boolean setMessage(String message) {
            if (progress.setMessage(message)) cancelled = true;
            return cancelled;
        }

        public boolean isCanceled() {
            return cancelled;
        }

        public boolean setIndeterminateProgress() {
            return cancelled;
        }
    };
}

package jebl.evolution.align;

import jebl.util.ProgressListener;

/**
 * @author Matt Kearse
 * @version $Id$
 */
public class CompoundAlignmentProgressListener  {
    private boolean cancelled = false;
    private int sectionsCompleted = 0;
    private int totalSections ;
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
        sectionsCompleted+= count;
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
            if (progress.setProgress(totalProgress)) cancelled = true;
            return cancelled;
        }
    };
}

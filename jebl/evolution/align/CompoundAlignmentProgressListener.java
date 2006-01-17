package jebl.evolution.align;

/**
 * @author Matt Kearse
 * @version $Id$
 */
public class CompoundAlignmentProgressListener  {
    private boolean cancelled = false;
    private int sectionsCompleted = 0;
    private int totalSections ;
    private AlignmentProgressListener progress;
    private int sectionSize= 1;

    public CompoundAlignmentProgressListener(AlignmentProgressListener progress, int totalSections) {
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

    public AlignmentProgressListener getMinorProgress() {
        return minorProgress;
    }

    AlignmentProgressListener minorProgress = new AlignmentProgressListener() {

        public boolean setProgress(double fractionCompleted) {
//            System.out.println("progress =" + fractionCompleted+ " sections =" + sectionsCompleted+ "/" + totalSections);
            double totalProgress = (sectionsCompleted + fractionCompleted*sectionSize) / totalSections;
            if (progress.setProgress(totalProgress)) cancelled = true;
            return cancelled;
        }
    };
}

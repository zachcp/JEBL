package jebl.evolution.align;

/**
 *
 * @author Alexei Drummond
 *
 *
 */
public interface TracebackPlotter {

    void newTraceBack(String sequence1, String sequence2);

    void traceBack(Traceback t);

    void finishedTraceBack();   
}

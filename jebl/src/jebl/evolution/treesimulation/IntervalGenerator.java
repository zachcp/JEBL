package jebl.evolution.treesimulation;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id$
 */
public interface IntervalGenerator {
    double getInterval(double criticalValue, int lineageCount, double currentHeight);
}

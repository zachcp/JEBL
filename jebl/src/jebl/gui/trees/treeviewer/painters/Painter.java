package jebl.gui.trees.treeviewer.painters;

import org.virion.jam.controlpanels.ControlsProvider;

import java.awt.*;
import java.awt.geom.Rectangle2D;


/**
 * A painter draws a particular decoration onto the tree within a
 * rectangle.
 * @author Andrew Rambaut
 * @version $Id$
 */
public interface Painter<T> extends ControlsProvider {

    public enum Orientation {
        TOP,
        LEFT,
        BOTTOM,
        RIGHT
    }

    public enum Justification {
        FLUSH,
        LEFT,
        RIGHT,
        CENTER
    }

    boolean isVisible();

    //void calibrate(Graphics2D g2, T item);

    void calibrate(Graphics2D g2);

    // May change paint and stroke
    void paint(Graphics2D g2, T item, Justification justification, Rectangle2D bounds);

    double getWidth(Graphics2D g2, T item);
    double getPreferredHeight();
    double getHeightBound();

    void addPainterListener(PainterListener listener);
    void removePainterListener(PainterListener listener);
}

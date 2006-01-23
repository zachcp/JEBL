package jebl.gui.trees.treeviewer.painters;

import java.awt.*;
import java.awt.geom.Rectangle2D;

import org.virion.jam.controlpanels.ControlsProvider;


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

    void calibrate(Graphics2D g2, T item);

    void paint(Graphics2D g2, T item, Justification justification, Rectangle2D bounds);

    double getPreferredWidth();
    double getPreferredHeight();

    void addPainterListener(PainterListener listener);
    void removePainterListener(PainterListener listener);
}

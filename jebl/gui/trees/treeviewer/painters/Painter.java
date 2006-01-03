package jebl.gui.trees.treeviewer.painters;

import jebl.gui.trees.treeviewer.controlpanels.ControlsProvider;

import java.awt.*;

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

    public enum LabelAlignment {
        FLUSH,
        LEFT,
        RIGHT,
        CENTER
    }

	void calibrate(Graphics2D g2);

	void paint(Graphics2D g2, T item, LabelAlignment labelAlignment, Insets insets);

    double getPreferredWidth();
    double getPreferredHeight();

    void addPainterListener(PainterListener listener);
    void removePainterListener(PainterListener listener);
}

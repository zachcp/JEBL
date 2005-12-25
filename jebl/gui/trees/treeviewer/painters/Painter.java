package jebl.gui.trees.treeviewer.painters;

import javax.swing.*;
import java.awt.*;

/**
 * A painter draws a particular decoration onto the tree within a
 * rectangle.
 * @author Andrew Rambaut
 * @version $Id$
 */
public interface Painter<T> {
	
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

	JPanel getControlPanel();

    double getPreferredWidth();
    double getPreferredHeight();

    void addPainterListener(PainterListener listener);
    void removePainterListener(PainterListener listener);
}

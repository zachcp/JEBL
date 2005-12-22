package jebl.gui.trees.treeviewer.painters;

/**
 * A painter draws a particular decoration onto the tree within a
 * rectangle.
 * @author Andrew Rambaut
 * @version $Id$
 */
public interface Painter {
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

    double getPreferredWidth();
    double getPreferredHeight();

    void addPainterListener(PainterListener listener);
    void removePainterListener(PainterListener listener);
}

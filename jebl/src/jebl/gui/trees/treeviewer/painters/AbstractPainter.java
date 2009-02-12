package jebl.gui.trees.treeviewer.painters;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public abstract class AbstractPainter<T> implements Painter<T> {
    protected boolean paintAsMirrorImage = true;

    public void addPainterListener(PainterListener listener) {
        listeners.add(listener);
    }

    public void removePainterListener(PainterListener listener) {
        listeners.remove(listener);
    }

    public void firePainterChanged() {
        for (PainterListener listener : listeners) {
            listener.painterChanged();
        }
    }
    private final List<PainterListener> listeners = new ArrayList<PainterListener>();

    public void setPaintAsMirrorImage(boolean paintAsMirrorImage) {
        this.paintAsMirrorImage = paintAsMirrorImage;
    }
}

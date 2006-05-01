package jebl.gui.trees.treeviewer_dev.painters;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public abstract class AbstractPainter<T> implements Painter<T> {
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

    public void firePainterSettingsChanged() {
        for (PainterListener listener : listeners) {
            listener.painterSettingsChanged();
        }
    }
    
    private final List<PainterListener> listeners = new ArrayList<PainterListener>();
}

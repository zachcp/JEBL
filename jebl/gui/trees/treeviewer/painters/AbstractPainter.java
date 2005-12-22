package jebl.gui.trees.treeviewer.painters;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id$
 */
public abstract class AbstractPainter implements Painter {
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
}

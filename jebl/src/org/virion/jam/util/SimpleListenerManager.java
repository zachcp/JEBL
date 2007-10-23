package org.virion.jam.util;

import javax.swing.event.ChangeListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Used for a managing a list of SimpleListeners.
 * Calling {@link #objectChanged()} calls that method on all internal SimpleListeners.  
 * @author Richard Moir
 * @version $Id$
 */
public class SimpleListenerManager implements SimpleListener {

    private List<SimpleListener> listeners = new ArrayList<SimpleListener>();

    public SimpleListenerManager(SimpleListenerManager manager) {
        this.listeners = new ArrayList<SimpleListener>(manager.listeners);
    }

    public SimpleListenerManager() {
    }

    public synchronized void add(SimpleListener listener) {
        listeners.add(listener);
    }

    public synchronized void remove(SimpleListener listener) {
        listeners.remove(listener);
    }


    /**
     * calls {@link org.virion.jam.util.SimpleListener#objectChanged()}  on all listeners added using
     * {@link #add(SimpleListener)} .
     */
    public synchronized void fire() {
        List<SimpleListener> listenersCopy = new ArrayList<SimpleListener>(listeners);  // Copy to avoid ConcurrentModificationExceptions
        for (SimpleListener simpleListener : listenersCopy) {
            simpleListener.objectChanged();
        }
    }

    /**
     * Get the number of listeners (those added, but not yet removed)
     * @return
     */
    public synchronized int size () {
        return listeners.size ();
    }


    public void objectChanged() {
        fire();
    }
}

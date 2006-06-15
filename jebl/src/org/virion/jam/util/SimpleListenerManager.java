package org.virion.jam.util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Richard
 * @version $Id$
 */
public class SimpleListenerManager {

    List<SimpleListener> listeners = new ArrayList<SimpleListener>();

    public SimpleListenerManager(SimpleListenerManager manager) {
        this.listeners = new ArrayList<SimpleListener>(manager.listeners);
    }

    public synchronized void add(SimpleListener listener) {
        listeners.add(listener);
    }

    public synchronized void remove(SimpleListener listener) {
        listeners.remove(listener);
    }

    public synchronized void fire() {
        for (SimpleListener simpleListener : listeners) {
            simpleListener.objectChanged();
        }
    }
}

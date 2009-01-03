/*
 * SimpleListenerManager.java
 *
 * Copyright (c) 2009 JAM Development Team
 *
 * This package is distributed under the Lesser Gnu Public Licence (LGPL)
 *
 */

package org.virion.jam.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A composite SimpleListener that manages a list of component SimpleListeners to which
 * it forwards all {@link #objectChanged()} calls (i.e. it calls that method on all
 * internal SimpleListeners). This class is thread safe.
 *
 * @author Richard Moir
 * @version $Id$
 */
public class SimpleListenerManager implements SimpleListener {
    private final List<SimpleListener> listeners;

    public SimpleListenerManager(SimpleListenerManager manager) {
        this(manager.listeners);
    }

    public SimpleListenerManager() {
        this(Collections.<SimpleListener>emptyList());
    }

    private SimpleListenerManager(List<SimpleListener> initialListeners) {
        this.listeners = new ArrayList<SimpleListener>(initialListeners);
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
    public void fire() {
        List<SimpleListener> listenersCopy;
        synchronized(this) {
            listenersCopy = new ArrayList<SimpleListener>(listeners);  // Copy to avoid ConcurrentModificationExceptions
        }
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

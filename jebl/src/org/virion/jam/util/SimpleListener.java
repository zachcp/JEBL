/*
 * SimpleListener.java
 *
 * Copyright (c) 2009 JAM Development Team
 *
 * This package is distributed under the Lesser Gnu Public Licence (LGPL)
 *
 */

package org.virion.jam.util;

/**
 * @author Richard Moir
 * @version $Id$
 */
public interface SimpleListener {

    void objectChanged();

    /**
     * A SimpleListener that does nothing when objectChanged is called. Useful when a method has a SimpleListener parameter
     * but you don't need to know when it is fired.
     *
     * Added for Geneious API 4.701 (Geneious version 7.0.1)
     */
    public static final SimpleListener EMPTY = new SimpleListener() {
        @Override
        public void objectChanged() {
            //do nothing, i'm empty!
        }
    };
}

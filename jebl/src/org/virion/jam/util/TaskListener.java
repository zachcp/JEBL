/*
 * TaskListener.java
 *
 * Copyright (c) 2009 JAM Development Team
 *
 * This package is distributed under the Lesser Gnu Public Licence (LGPL)
 *
 */

package org.virion.jam.util;


public interface TaskListener {

    void taskFinished();

    void taskCanceled();
}

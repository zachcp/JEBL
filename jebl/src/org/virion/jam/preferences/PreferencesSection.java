package org.virion.jam.preferences;

import javax.swing.*;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id$
 */
public interface PreferencesSection {
    String getTitle();

    Icon getIcon();

    JPanel getPanel();

    void retrievePreferences();

    void storePreferences();
}

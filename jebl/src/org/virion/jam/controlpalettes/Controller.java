package org.virion.jam.controlpalettes;

import javax.swing.*;
import java.util.Map;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public interface Controller {

    /**
     * Get a component that will be put in the title bar of the palette section.
     * If a simple text title is required, this should return a JLabel.
     *
     * @return A component
     */
    JComponent getTitleComponent();

    /**
     * Get a JPanel which is the main section for the palette.
     * @return A panel
     */
    JPanel getPanel();

    /**
     * @return whether the panel should be open or closed initially
     */
    boolean isInitiallyVisible();

    /**
     * Collect the settings for this controller. These should be stored
     * in the given settings map using string keys.
     *
     * @param settings
     */
    void getSettings(Map<String, Object> settings);

    /**
     * Set the settings for this controller. These will have been stored
     * as a map by the getSettings function.
     *
     * @param settings
     */
    void setSettings(Map<String,Object> settings);

    /**
     * Add a ControllerListener to this controllers list of listeners
     * The main listener will be the ControlPalette itself which will use
     * this to resize the panels if the components changed
     * @param listener
     */
    void addControllerListener(ControllerListener listener);

    /**
     * Remove a listener
     * @param listener
     */
    void removeControllerListener(ControllerListener listener);
}

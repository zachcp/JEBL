package org.virion.jam.controlpanels;

import java.util.List;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public interface ControlsProvider {
    /**
     * Give the controls provider with a handle for the controlPalette object
     *
     * @param controlPalette
     */
    void setControlPalette(ControlPalette controlPalette);

    /**
     * Get a list of Controls handled by this provider
     *
     * @param detachPrimaryCheckbox
     * @return A list of Controls
     */
    List<Controls> getControls(boolean detachPrimaryCheckbox);

    /**
     * Give the provider some settings
     *
     * @param settings
     */
    void setSettings(ControlsSettings settings);

    /**
     * Get the settings for a given Controls object
     * @param settings
     */
    void getSettings(ControlsSettings settings);
}

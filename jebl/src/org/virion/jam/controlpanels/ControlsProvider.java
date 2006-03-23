package org.virion.jam.controlpanels;

import java.util.List;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public interface ControlsProvider {
    /**
     * Give the controls provider with a handle for the controlPalette object.
     *
     * @param controlPalette
     */
    void setControlPalette(ControlPalette controlPalette);

    /**
     * Get a list of Controls handled by this provider.
     *
     * @param detachPrimaryCheckbox When false, do nothing. When true, if controls have a "main" on/off switch
     *                              (implemented as a JCheckBox), that checkbox should not be included in the controls panel but returned in
     * @return A list of Controls
     * @see Controls#getPrimaryCheckbox
     */
    List<Controls> getControls(boolean detachPrimaryCheckbox);

    /**
     * Give the provider some settings.
     *
     * @param settings
     */
    void setSettings(ControlsSettings settings);

    /**
     * Get the settings for a given Controls object.
     *
     * @param settings
     */
    void getSettings(ControlsSettings settings);
}

package org.virion.jam.controlpanels;

import java.util.List;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public interface ControlsProvider {
	/**
	 * Give the controls provider with a handle for the controlPalette object
	 * @param controlPalette
	 */
    void setControlPalette(ControlPalette controlPalette);

	/**
	 * Get a list of Controls handled by this provider
	 * @return A list of Controls
	 */
    List<Controls> getControls();

	/**
	 * Give the provider some settings for the given Controls object
	 * @param controls
	 * @param settings
	 */
	void setSettings(Controls controls, ControlsSettings settings);

	/**
	 * Get the settings for a given Controls object
	 * @param controls
	 * @return The settings
	 */
	ControlsSettings getSettings(Controls controls);
}

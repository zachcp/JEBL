package org.virion.jam.controlpalettes;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: joseph
 * Date: 20/03/2006
 * Time: 10:23:21
 *
 * @author Joseph Heled
 * @version $Id$
 */
public interface ControlPalette {

	/**
	 * get the panel that encloses the control palette
	 * @return the panel
	 */
    JPanel getPanel();

	/**
	 * install a Controller into the palette
	 * @param controller
	 */
    void addController(Controller controller);

	/**
	 * tell listeners that the palette has changed
	 */
    void fireControlsChanged();

	/**
	 * Add a listener to this palette
	 * @param listener
	 */
    void addControlPaletteListener(ControlPaletteListener listener);

	/**
	 * Remove a listener fromm this palette
	 * @param listener
	 */
    void removeControlPaletteListener(ControlPaletteListener listener);

	/**
	 * Gather up all the settings from all the controls in the palette.
	 * This would usually called before saving them with the document
	 * that the palette controls.
	 * @param settings
	 */
	void getSettings(ControllerSettings settings);

	/**
	 * Distribute all the settings to all the controls in the palette.
	 * This would usually called after loading the document
	 * that the palette controls.
	 * @param settings
	 */
	void setSettings(ControllerSettings settings);
}

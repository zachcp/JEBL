package org.virion.jam.controlpalettes;

import javax.swing.*;

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
	 * in the given ControllerSettings using keys, object maps.
	 *
	 * @param settings
	 */
	void getSettings(ControllerSettings settings);

	/**
	 * Set the settings for this controller. These will have been stored
	 * as a map by the getSettings function.
	 *
	 * @param settings
	 */
	void setSettings(ControllerSettings settings);
}

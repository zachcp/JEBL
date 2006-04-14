package org.virion.jam.controlpalettes;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public interface ControllerSettings {

	void putSetting(String key, Object value);

	Object getSetting(String key);
}

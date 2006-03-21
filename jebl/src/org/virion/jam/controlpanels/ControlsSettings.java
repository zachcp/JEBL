package org.virion.jam.controlpanels;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public interface ControlsSettings {

	void putSetting(String key, Object value);

	Object getSetting(String key);
}

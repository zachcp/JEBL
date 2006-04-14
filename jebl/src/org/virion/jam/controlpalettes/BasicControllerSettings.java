package org.virion.jam.controlpalettes;

import java.util.Map;
import java.util.HashMap;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class BasicControllerSettings implements ControllerSettings {
	public void putSetting(String key, Object value) {
		settingsMap.put(key, value);
	}

	public Object getSetting(String key) {
		return settingsMap.get(key);
	}

	private Map<String, Object> settingsMap = new HashMap<String, Object>();
}

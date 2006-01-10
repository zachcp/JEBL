/*
 * Attributable.java
 *
 * (c) 2005 JEBL Development Team
 *
 * This package is distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */

package jebl.util;

import java.util.Map;
import java.util.Set;

/**
 * Interface for associating attributes with an object.
 *
 * @version $Id$
 *
 * @author Andrew Rambaut
 */
public interface Attributable {

	/**
	 * Sets an named attribute for this object.
	 * @param name the name of the attribute.
	 * @param value the new value of the attribute.
	 */
	void setAttribute(String name, Object value);

	/**
	 * @return an object representing the named attributed for this object.
	 * @param name the name of the attribute of interest.
	 */
	Object getAttribute(String name);

	/**
	 * @return an array of the attributes that this object has.
	 */
    Set<String> getAttributeNames();

	/**
	 * Gets the entire attribute map.
	 * @return an unmodifiable map
	 */
	Map<String, Object> getAttributeMap();
}



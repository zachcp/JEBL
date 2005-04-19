/*
 * Attributable.java
 *
 * (c) 2002-2005 BEAST Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */

package jebl.util;

import java.util.*;

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
	public void setAttribute(String name, Object value);

	/**
	 * @return an object representing the named attributed for this object.
	 * @param name the name of the attribute of interest.
	 */
	public Object getAttribute(String name);

	/**
	 * @return an array of the attributes that this object has.
	 */
    Set getAttributeNames();

	public static final class Helper implements Attributable {
		public void setAttribute(String name, Object value) {
			attributes.put(name, value);
		}

		public Object getAttribute(String name) {
			return attributes.get(name);
		}

		public Set getAttributeNames() {
			return attributes.keySet();
		}
	
		// **************************************************************
		// INSTANCE VARIABLE
		// **************************************************************
		
		private Map attributes = new TreeMap();
	};
	
	
}



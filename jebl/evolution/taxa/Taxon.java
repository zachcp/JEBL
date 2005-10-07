/*
 * Taxon.java
 *
 * (c) 2005 JEBL Development Team
 *
 * This package is distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.taxa;

import jebl.util.Attributable;

import java.util.*;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 *
 * @version $Id$
 */
public final class Taxon implements Attributable, Comparable {

    /**
     * A private constructor. Taxon objects can only be created by the static Taxon.getTaxon()
     * factory method.
     * @param name the name of the taxon
     */
    private Taxon(String name) {
        this(name, null);
    }

    /**
     * A private constructor. Taxon objects can only be created by the static Taxon.getTaxon()
     * factory method.
     * @param name the name of the taxon
     */
    private Taxon(String name, TaxonomicLevel taxonomicLevel) {
        this.name = name;
        this.taxonomicLevel = taxonomicLevel;
    }

    /**
     * get the name of the taxon
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * get the taxonomic level of the taxon
     * @return the taxonomic level
     */
    public TaxonomicLevel getTaxonomicLevel() {
        return taxonomicLevel;
    }

	// Attributable implementation

	public void setAttribute(String name, Object value) {
		if (attributeMap == null) {
			attributeMap = new HashMap<String, Object>();
		}
		attributeMap.put(name, value);
	}

	public Object getAttribute(String name) {
		if (attributeMap == null) {
			return null;
		}
		return attributeMap.get(name);
	}

	public Set<String> getAttributeNames() {
		if (attributeMap == null) {
			return Collections.emptySet();
		}
		return attributeMap.keySet();
	}

    // Static factory methods

    /**
     * @return a Set containing all the currently created Taxon objects.
     */
    public static Set<Taxon> getAllTaxa() {
        return Collections.unmodifiableSet(new HashSet<Taxon>(taxa.values()));
    }

    /**
     * A static method that returns a Taxon object with the given name. If this has
     * already been created then the same instance will be returned.
     * @param name
     * @return the taxon
     */
    public static Taxon getTaxon(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Illegal null string for taxon name");
        }
        if (name.length() == 0) {
            throw new IllegalArgumentException("Illegal empty string for taxon name");
        }
        Taxon taxon = taxa.get(name);

        if (taxon == null) {
            taxon = new Taxon(name);
            taxa.put(name, taxon);
        }

        return taxon;
    }

	// private members

    /**
     * A lazily allocated Attribute Map .
     */
    private Map<String, Object> attributeMap = null;

    /**
     * The name of this taxon.
     */
    private final String name;

    /**
     * A hash map containing taxon name, object pairs.
     */
    private static Map<String, Taxon> taxa = new HashMap<String, Taxon>();

    /**
     * the taxonomic level of this taxon.
     */
    private final TaxonomicLevel taxonomicLevel;

    public String toString() {
        return name;
    }

	public int compareTo(Object o) {
		return name.compareTo(((Taxon)o).getName());
	}


    public boolean equals(Taxon t) {
        return this == t;
    }

    public int hashCode() {
        return name.hashCode();
    }
}

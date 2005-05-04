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
 * @author rambaut
 * @author Alexei Drummond
 *
 * @version $Id$
 */
public final class Taxon {

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
     * Set an attribute value for the given name. Will return null if no such attribute exists.
     * @param name the name of the attribute
     * @return the attribute's value or null
     */
    public Object getAttribute(String name) {
        if (helper == null) {
            return null;
        }
        return helper.getAttribute(name);
    }

    /**
     * Returns a Set of strings which are the names of the available attributes
     * @return
     */
    public Set getAttributeNames() {
        if (helper == null) {
            return Collections.EMPTY_SET;
        }
        return helper.getAttributeNames();
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

    /**
     * Set an attribute name, value pair
     * @param name the name of the attribute
     * @param value the value of the attribute
     */
    public void setAttribute(String name, Object value) {
        if (helper == null) {
            helper = new Attributable.Helper();
        }
        helper.setAttribute(name, value);
    }

    // Static factory methods

    /**
     * @return a Set containing all the currently created Taxon objects.
     */
    public static Set getAllTaxa() {
        return Collections.unmodifiableSet(new HashSet(taxa.values()));
    }

    /**
     * A static method that returns a Taxon object with the given name. If this has
     * already been created then the same instance will be returned.
     * @param name
     * @return
     */
    public static Taxon getTaxon(String name) {
        Taxon taxon = (Taxon)taxa.get(name);

        if (taxon == null) {
            taxon = new Taxon(name);
            taxa.put(name, taxon);
        }

        return taxon;
    }

    // private members

    /**
     * A lazily allocated Attributable.Helper object.
     */
    private Attributable helper = null;

    /**
     * The name of this taxon.
     */
    private final String name;

    /**
     * A hash map containing taxon name, object pairs.
     */
    private static Map taxa = new HashMap();

    /**
     * the taxonomic level of this taxon.
     */
    private final TaxonomicLevel taxonomicLevel;
}

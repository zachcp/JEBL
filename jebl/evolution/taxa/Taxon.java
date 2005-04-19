/*
 * Taxon.java
 *
 * (c) 2002-2005 BEAST Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.taxa;

import jebl.util.Attributable;

import java.util.*;

/**
 * @author rambaut
 *         Date: Apr 5, 2005
 *         Time: 5:37:06 PM
 */
public final class Taxon {

    /**
     * A private constructor. Taxon objects can only be created by the static Taxon.getTaxon()
     * factory method.
     * @param name the name of the taxon
     */
    private Taxon(String name) {
        this.name = name;
        this.taxonomicLevel = null;
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
     * The name of this taxon
     */
    private final String name;

    private final TaxonomicLevel taxonomicLevel;

    /**
     * A lazily allocated Attributable.Helper object
     */
    private Attributable helper = null;

    // Static factory methods

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

    /**
     * Returns a Set containing all the currently created Taxon objects.
     * @return
     */
    public static Set getAllTaxa() {
        return Collections.unmodifiableSet(taxa.entrySet());
    }

    /**
     * A hash map containing taxon name, object pairs.
     */
    private static Map taxa = new HashMap();
}

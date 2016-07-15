/**
 * Taxon.java
 *
 * (c) 2005 JEBL Development Team
 *
 * This package is distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.taxa;

import jebl.util.Attributable;
import jebl.util.AttributableHelper;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 *
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
        this.name = new String(name); // To avoid the Java string.substring "memory leak" where a substring holds a reference to the parent superstring. This line previously used String.intern() instead but that is bad because Strings are interned into the Java perm-gen memory space which is quite limited in size, and lead to OutOfMemory errors when I had plenty of free memory
        // which references the original string's characters 
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

	// Attributable IMPLEMENTATION

	public void setAttribute(String name, Object value) {
		if (helper == null) {
			helper = new AttributableHelper();
		}
		helper.setAttribute(name, value);
	}

	public Object getAttribute(String name) {
		if (helper == null) {
			return null;
		}
		return helper.getAttribute(name);
	}

    public void removeAttribute(String name) {
        if( helper != null ) {
            helper.removeAttribute(name);
        }
    }

    public Set<String> getAttributeNames() {
        if (helper == null) {
            return Collections.emptySet();
        }
        return helper.getAttributeNames();
    }

	public Map<String, Object> getAttributeMap() {
		if (helper == null) {
			return Collections.emptyMap();
		}
		return helper.getAttributeMap();
	}

	private AttributableHelper helper = null;

    private static void purgeGarbageCollectedTaxa() {
        synchronized(taxa) {
            List<String> taxonNamesToPurge = new ArrayList<String>();
            for (Map.Entry<String,WeakReference<Taxon>> entry : taxa.entrySet()) {
                Taxon taxon = entry.getValue().get();
                if (taxon == null) {
                    taxonNamesToPurge.add(entry.getKey());
                }
            }
            for (String taxonName : taxonNamesToPurge) {
                taxa.remove(taxonName);
            }
            taxaCreatedSinceLastPurge.set(0);
            lastPurgeTime.set(System.currentTimeMillis());
        }
    }

    // Static factory methods

    /**
     * Return a Set containing all Taxon objects created after
     * {@link #setTreatingTaxonsNamesAsUnique(boolean)} was called with true.
     * More precisely, {@link #setTreatingTaxonsNamesAsUnique(boolean)} can be used to
     * repeatably toggle a state that is initially false, but the intention is that it is called
     * only once to enable the true state when initialising an application if that is
     * the desired behaviour. This has not been used by default since tag geneious_6_0_5.
     * @return a Set containing all the currently created Taxon objects.
     */
    public static Set<Taxon> getAllTaxa() {
        Set<Taxon> result = new HashSet<Taxon>();
        synchronized (taxa) {
            purgeGarbageCollectedTaxa();
            for (Map.Entry<String,WeakReference<Taxon>> entry : taxa.entrySet()) {
                Taxon taxon = entry.getValue().get();
                if (taxon != null) { // might have been garbage collected just now
                    result.add(taxon);
                }
            }
        }
        return Collections.unmodifiableSet(result);
    }

    private static AtomicInteger taxaCreatedSinceLastPurge = new AtomicInteger(0);
    private static AtomicLong lastPurgeTime = new AtomicLong(0);

    private static boolean taxonNamesUnique = false;

    /**
     * Set this to true if you want successive calls to {@link #getTaxon(String)} to return the same object.
     * This means that if you add attributes to a taxon, when you request the taxon with the same name again later,
     * it will have those same attributes.
     * This was the default behaviour in the geneious_6_0_5 tag and earlier.
     * @param taxonNamesUnique
     */
    public static void setTreatingTaxonsNamesAsUnique(boolean taxonNamesUnique) {
        Taxon.taxonNamesUnique = taxonNamesUnique;
    }

    /**
     * A static method that returns a Taxon instance with the given name.
     * If {@link #setTreatingTaxonsNamesAsUnique(boolean)} was last called with false
     * (the default state after geneious_6_0_5 tag) it will create a new instance of Taxon.
     * If {@link #setTreatingTaxonsNamesAsUnique(boolean)} was last called with true,
     * if a Taxon with that name has already been created then the same instance will be returned.
     *
     * @param name
     * @return the taxon
     */
    public static Taxon getTaxon(String name) {
        if (!taxonNamesUnique) {
            return new Taxon(name);
        }
        if (name == null) {
            throw new IllegalArgumentException("Illegal null string for taxon name");
        }
        if (name.length() == 0) {
            assert false:"Illegal empty string for taxon name"; // This is an assertion rather than an exception because we have users in the real world who have sequences with empty names. Ideally we prevent users from creating such sequences, but if we forget to check for empty sequence names somewhere, it isn't nice to crash on them.
        }
        Taxon taxon;
        synchronized(taxa) {
            // TT: Can we somehow get around having to manually purge WeakReferences whose values have been
            // garbage collected? E.g. using a WeakHashMap<Taxon,Taxon>() which maps each taxon to is canonicalized form
            // and could be accessed as map.get(new Taxon(...))? Or would a WeakHashMap entry where key==value not get
            // garbage collected?
            if (taxaCreatedSinceLastPurge.get() > 10000 || (lastPurgeTime.get() + 10000 < System.currentTimeMillis())) {
                purgeGarbageCollectedTaxa();
            }
            WeakReference<Taxon> taxonReference = taxa.get(name);
            taxon = (taxonReference == null ? null : taxonReference.get());
            if (taxon == null) {
                taxaCreatedSinceLastPurge.incrementAndGet();
                taxon = new Taxon(name);
                taxa.put(taxon.getName(), new WeakReference<Taxon>(taxon));
            }
        }
        return taxon;
    }

    /**
     * Creates a copy of this taxon including a _temp_copy stamp appended to the name.
     * @return a new Taxon instance reflecting the passed in taxon.
     */
    public Taxon getTaxonCopy(){
        Taxon taxonCopy = new Taxon(getName()+"_geneious_temp_copy", getTaxonomicLevel());
        taxonCopy.setHelper(helper);
        return taxonCopy;
    }

	// private members

    private void setHelper(AttributableHelper helper){
        this.helper = helper;
    }

    /**
     * The name of this taxon.
     */
    private final String name;

    /**
     * Maps taxon name to the taxon of that name. We use WeakReferences so that if no other references
     * to a taxon exist, it no longer wastes memoryt.
     */
    private static final Map<String, WeakReference<Taxon>> taxa = new HashMap<String, WeakReference<Taxon>>();

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

    public boolean equals(Object t) {
        if (t instanceof Taxon) {
            return name.equals(((Taxon)t).getName());
        }
        return false;
    }

    public boolean equals(Taxon t) {
	    return name.equals(t.getName());
    }

    public int hashCode() {
        return name.hashCode();
    }
}

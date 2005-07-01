/*
 * State.java
 *
 * (c) 2002-2005 JEBL Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package jebl.evolution.sequences;

import java.util.*;

/**
 * @author Andrew Rambaut
 * @author Alexei Drummond
 *
 * @version $Id$
 */
public abstract class State<T extends State> implements Comparable {

    State(String name, String stateCode, int index) {

        this.name = name;
        this.stateCode = stateCode;

        List<T> ambiguities = new ArrayList<T>();
        ambiguities.add((T)this);
        this.ambiguities = Collections.unmodifiableSortedSet(new TreeSet<T>(ambiguities));
        this.index = index;
    }

    State(String name, String stateCode, int index, T[] ambiguities) {

        this.name = name;
        this.stateCode = stateCode;
        this.ambiguities = Collections.unmodifiableSortedSet(new TreeSet<T>(Arrays.asList(ambiguities)));
        this.index = index;
    }

    public String getCode() {
        return stateCode;
    }

    public int getIndex() {
        return index;
    }

    public String getName() { return name; }

    public boolean isAmbiguous() {
        return getCanonicalStates().size() > 1;
    }

    public Set<T> getCanonicalStates() {
        return ambiguities;
    }

    public int compareTo(Object o) {
        return index - ((State)o).index;
    }

    public String toString() { return stateCode; }

    private String stateCode;
    private String name;
    private Set<T> ambiguities;
    private int index;
}

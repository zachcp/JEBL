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
public abstract class State implements Comparable {

    State(String name, String stateCode, int index) {

        this.name = name;
        this.stateCode = stateCode;

        List<State> ambiguities = new ArrayList<State>();
        ambiguities.add(this);
        this.ambiguities = Collections.unmodifiableSortedSet(new TreeSet<State>(ambiguities));
        this.index = index;
    }

    State(String name, String stateCode, int index, State[] ambiguities) {

        this.name = name;
        this.stateCode = stateCode;
        this.ambiguities = Collections.unmodifiableSortedSet(new TreeSet<State>(Arrays.asList(ambiguities)));
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

    public Set<State> getCanonicalStates() {
        return ambiguities;
    }

    public int compareTo(Object o) {
        return index - ((State)o).index;
    }

    public String toString() { return stateCode; }

	public abstract boolean isGap();

	private String stateCode;
    private String name;
    private Set<State> ambiguities;
    private int index;
}

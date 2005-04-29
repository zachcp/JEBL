/*
* State.java
*
* (c) 2005 JEBL Development Team
*
* This package may be distributed under the
* Lesser Gnu Public Licence (LGPL)
*/
package jebl.evolution.datatypes;

import java.util.*;
import java.lang.reflect.Array;

/**
 * @author rambaut
 *         Date: Apr 29, 2005
 *         Time: 10:16:36 AM
 */
public class State implements Comparable {

    State(String name, String stateCode, int index) {

        this.name = name;
        this.stateCode = stateCode;
        Set a = new TreeSet();

        State[] ambiguities = new State[] { this };
        this.ambiguities = Collections.unmodifiableSortedSet(new TreeSet(Arrays.asList(ambiguities)));
        this.index = index;
    }

    State(String name, String stateCode, int index, State[] ambiguities) {

        this.name = name;
        this.stateCode = stateCode;
        this.ambiguities = Collections.unmodifiableSortedSet(new TreeSet(Arrays.asList(ambiguities)));
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

    public <State>Set getCanonicalStates() {
        return ambiguities;
    }

    public int compareTo(Object o) {
        return index - ((State)o).index;
    }

    public String toString() { return stateCode; }


    private String stateCode;
    private String name;
    private Set ambiguities;
    private int index;
}

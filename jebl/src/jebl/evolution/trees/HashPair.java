package jebl.evolution.trees;

/**
 * A pair suitable for use in a HashMap.
 *
 * @author Joseph Heled
 *
 * @version $Id$
 */

class HashPair<T> {
    HashPair(T a, T b) {
        first = a;
        second = b;
    }

    public int hashCode() {
        return first.hashCode() + second.hashCode();
    }

    public boolean equals(Object x) {
        if( x instanceof HashPair ) {
            return ((HashPair) x).first.equals(first) &&  ((HashPair )x).second.equals(second);
        }
        return false;
    }

    public final T first;
    public final T second;
}
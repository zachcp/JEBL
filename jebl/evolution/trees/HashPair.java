package jebl.evolution.trees;

/**
 * Created by IntelliJ IDEA.
 * User: joseph
 * Date: 9/01/2006
 * Time: 16:46:23
 *
 * @author joseph
 * @version $Id$
 *
 *  A pair suitable for use in a HashMap.
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
            return ((HashPair<T>)x).first == first &&  ((HashPair<T>)x).second == second;
        }
        return false;
    }

    public final T first;
    public final T second;
}

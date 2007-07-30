package jebl.util;

/**
 * A tri-state boolean value that can also be "Maybe" besides True and False.
 * @author Tobias Thierer
 * @version $Id$
 *          <p/>
 *          Created on 30/07/2007 14:15:09
 */
public enum MaybeBoolean {
    True, False, Maybe;

    public static MaybeBoolean valueOf(boolean b) {
        return b ? True : False;
    }

    public static Boolean booleanValue(MaybeBoolean b) {
        if (b == Maybe) {
            return null;
        } else {
            return (b == True);
        }
    }

    public static MaybeBoolean consensus(boolean a, boolean b) {
        if (a == b) {
            return a ? True : False;
        } else {
            return Maybe;
        }
    }
}

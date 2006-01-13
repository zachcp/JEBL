package jebl.evolution.align;

/**
 * @author Matt Kearse
 * @version $Id$
 *
 * Used for representing the results of a sequence alignment. Basically just stores
 * an array representing whether or not each position in the alignment is a gap or not.
 */
class AlignmentResult {
    int size;
    boolean values[];
    // true represents a character from the original sequence, and false represents a gap
    // for example:
    // original sequence: abcd
    // values: true, false, false, true, true, false, true
    // then the resulting alignment is: a--bc-d

    public AlignmentResult(int size) {
        this.size = 0;
        values =new boolean[size];
    }

    public void append(String result) {
        for (char character : result.toCharArray()) {
            if(character =='-')
                values [ size ++ ]= false;
            else
                values [ size ++ ] = true;
        }
    }
}

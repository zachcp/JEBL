package jebl.util;

/**
 * Created by IntelliJ IDEA.
 * User: joseph
 * Date: 15/01/2006
 * Time: 09:36:13
 *
 * @author Joseph Heled
 * @version $Id$
 *
 */
public class FixedBitSet {
    int[] bits;
    int size;
    private int intSize = Integer.SIZE;

    private final static int ADDRESS_BITS_PER_UNIT = 5;
    private final static int BITS_PER_UNIT = 1 << ADDRESS_BITS_PER_UNIT;
    private final static int BIT_INDEX_MASK = BITS_PER_UNIT - 1;


    private static int unitIndex(int bitIndex) {
        return bitIndex >> ADDRESS_BITS_PER_UNIT;
    }

    /**
     * Given a bit index, return a unit that masks that bit in its unit.
     */
    private static long bit(int bitIndex) {
        return 1L << (bitIndex & BIT_INDEX_MASK);
    }

    public FixedBitSet(int size) {
       this.size = size;
       bits = new int[(unitIndex(size-1) + 1)];
    }

    public FixedBitSet(FixedBitSet bs) {
        bits = bs.bits.clone();
        size = bs.size;
    }


    public void set(int position) {
       int unitIndex = unitIndex(position);
       bits[unitIndex] |= bit(position);
    }

    public void clear(int position) {
       int unitIndex = unitIndex(position);
       bits[unitIndex] &= ~bit(position);
    }

    public boolean containedIn(final FixedBitSet b) {
        for(int k = 0; k < bits.length; ++k) {
            if( bits[k] != (bits[k] & b.bits[k]) ) {
                return false;
            }
        }
        return true;
    }

    public void or(FixedBitSet b) {
        for(int k = 0; k < Math.min(bits.length, b.bits.length); ++k) {
            bits[k] |= b.bits[k];
        }
    }

    private final static byte firstBitLocation[] = {
         -1, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
       4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
       5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
       4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
       6, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
       4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
       5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
       4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
       7, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
       4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
       5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
       4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
       6, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
       4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
       5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
       4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0};

    private int firstBit(int i) {
        for(int k = 0; k < 4; ++k) {
            char b = (char)(i & 0xff);
            if( b != 0 ) {
                return 8*k + firstBitLocation[b];
            }
            i = i >> 8;
        }
        return -1;
    }

    public int nextSetBit(int fromIndex) {
        int u = unitIndex(fromIndex);
        int testIndex = (fromIndex & BIT_INDEX_MASK);
        int unit = bits[u] >> testIndex;

        if (unit == 0) {
            testIndex = 0;

          while((unit==0) && (u < bits.length-1))
              unit = bits[++u];
        }

        if (unit == 0)
            return -1;

        testIndex += firstBit(unit);
        return ((u * BITS_PER_UNIT) + testIndex);
    }

    public int cardinality() {
        int sum = 0;
        for( int b : bits ) {
           while( b != 0) {
               // remove most significant bit
               b = b & (b-1);
               ++sum;
           }
        }
        return sum;
    }

    public boolean contains(int i) {
        final int unitIndex = unitIndex(i);
        return (bits[unitIndex] & bit(i)) != 0;
    }
}
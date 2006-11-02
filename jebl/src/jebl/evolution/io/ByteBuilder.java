package jebl.evolution.io;

/**
 * @author Joseph Heled
 * @version $Id$
 */
public class ByteBuilder implements CharSequence {
    int maxCapacity;
    int current;
    byte[] data;

    void ensureCapacity(final int cap) {
       if( cap > data.length ) {
           int newLen = 2*(cap+1);
           if( newLen <= 0 ) {
               newLen += 256;
           }
           if( newLen > maxCapacity) newLen = maxCapacity;
           if( newLen < cap ) newLen = cap;
           byte[] d  = new byte[newLen];
           System.arraycopy(data, 0, d, 0, data.length);
           data = d;
       }
    }

    public ByteBuilder(int maxCapacity) {
        this.maxCapacity = maxCapacity;
        current = 0;
        data = new byte[16];
    }


    public ByteBuilder append(char c) {
        if( current + 1 > data.length ) {
            ensureCapacity(current + 1);
        }
        data[current] = (byte)c;
        ++current;
        return this;
    }

    public int length() {
        return current;
    }

    public char charAt(int index) {
        return (char)data[index];
    }

    public CharSequence subSequence(int start, int end) {
        return new String(data, start, end - start);
    }

    public String toString() {
        return new String(data, 0, current);
    }
}

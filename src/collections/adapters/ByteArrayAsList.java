package collections.adapters;

import java.util.AbstractList;
import java.util.Objects;

public class ByteArrayAsList extends AbstractList<Byte> {
    private final byte[] original;

    public ByteArrayAsList(byte[] original) {
        Objects.requireNonNull(original);
        this.original = original;
    }

    public Byte get(int index) {
        return original[index];
    }

    public int size() {
        return original.length;
    }
}

package collections.adapters;

import java.util.AbstractList;
import java.util.Objects;

public class ShortArrayAsList extends AbstractList<Short> {
    private final short[] original;

    public ShortArrayAsList(short[] original) {
        Objects.requireNonNull(original);
        this.original = original;
    }

    public Short get(int index) {
        return original[index];
    }

    public int size() {
        return original.length;
    }
}

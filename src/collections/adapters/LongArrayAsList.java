package collections.adapters;

import java.util.AbstractList;
import java.util.Objects;

public class LongArrayAsList extends AbstractList<Long> {
    private final long[] original;

    public LongArrayAsList(long[] original) {
        Objects.requireNonNull(original);
        this.original = original;
    }

    public Long get(int index) {
        return original[index];
    }

    public int size() {
        return original.length;
    }
}

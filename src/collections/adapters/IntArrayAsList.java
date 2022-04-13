package collections.adapters;

import java.util.AbstractList;
import java.util.Objects;

public class IntArrayAsList extends AbstractList<Integer> {
    private final int[] original;

    public IntArrayAsList(int[] original) {
        Objects.requireNonNull(original);
        this.original = original;
    }

    public Integer get(int index) {
        return original[index];
    }

    public int size() {
        return original.length;
    }
}

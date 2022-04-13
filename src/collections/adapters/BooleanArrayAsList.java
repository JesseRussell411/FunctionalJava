package collections.adapters;

import java.util.AbstractList;
import java.util.Objects;

public class BooleanArrayAsList extends AbstractList<Boolean> {
    private final boolean[] original;

    public BooleanArrayAsList(boolean[] original) {
        Objects.requireNonNull(original);
        this.original = original;
    }

    public Boolean get(int index) {
        return original[index];
    }

    public int size() {
        return original.length;
    }
}

package collections.adapters;

import java.util.AbstractList;
import java.util.Objects;

public class FloatArrayAsList extends AbstractList<Float> {
    private final float[] original;

    public FloatArrayAsList(float[] original) {
        Objects.requireNonNull(original);
        this.original = original;
    }

    public Float get(int index) {
        return original[index];
    }

    public int size() {
        return original.length;
    }
}

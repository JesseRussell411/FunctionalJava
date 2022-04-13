package collections.adapters;

import java.util.AbstractList;
import java.util.Objects;

public class DoubleArrayAsList extends AbstractList<Double> {
    private final double[] original;

    public DoubleArrayAsList(double[] original) {
        Objects.requireNonNull(original);
        this.original = original;
    }

    public Double get(int index) {
        return original[index];
    }

    public int size() {
        return original.length;
    }
}

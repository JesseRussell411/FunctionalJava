package memoization;

import java.util.Arrays;

class Dependencies {
    public static final Dependencies EMPTY = new Dependencies(new Object[0]);

    public final Object[] items;
    private final Lazy<Integer> getHashCode;

    public Dependencies(Object[] items) {
        this.items = items;
        getHashCode = new Lazy<>(() -> Arrays.hashCode(items));
    }

    @Override
    public int hashCode() {
        return getHashCode.get();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Dependencies other && Arrays.equals(items, other.items);
    }
}

package collections;

import memoization.pure.Lazy;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Supplier;

public class ObjectTuple implements Iterable<Object> {
    public static final ObjectTuple EMPTY = new ObjectTuple(new Object[0]);
    private static final int HASH_CACHE_THRESHOLD = 3;

    private final Object[] items;
    private final Supplier<Integer> getHashCode;

    public ObjectTuple(Object[] items) {
        Objects.requireNonNull(items);
        this.items = Arrays.copyOf(items, items.length);
        final Supplier<Integer> getHashCode = () -> Arrays.hashCode(items);

        if (this.items.length >= HASH_CACHE_THRESHOLD) {
            this.getHashCode = new Lazy<>(getHashCode);
        } else {
            this.getHashCode = getHashCode;
        }
    }

    public Object get(int index) {
        return items[index];
    }

    public int size() {
        return items.length;
    }

    @Override
    public int hashCode() {
        return getHashCode.get();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ObjectTuple other && Arrays.equals(items, other.items);
    }

    public static class SelfIterator implements Iterator<Object> {
        private final Iterator<Object> itemsIterator;

        private SelfIterator(ObjectTuple self) {
            itemsIterator = Arrays.stream(self.items).iterator();
        }

        @Override
        public boolean hasNext() {
            return itemsIterator.hasNext();
        }

        @Override
        public Object next() {
            return itemsIterator.next();
        }
    }

    public Iterator<Object> iterator() {
        return new SelfIterator(this);
    }
}

package collections.records;

import collections.PersistentSet;
import memoization.pure.Lazy;
import memoization.pure.WeakLazy;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class SetRecord<T> implements Iterable<T>, Serializable {
    private final PersistentSet<T> values;

    public SetRecord(PersistentSet<T> values) {
        Objects.requireNonNull(values);
        this.values = values;
    }

    public SetRecord(T[] items) {
        this(new PersistentSet<T>().withMany(items));
    }

    public SetRecord(Iterable<T> items) {
        this(new PersistentSet<T>().withMany(items));
    }

    public SetRecord(Stream<T> itemStream) {
        this(new PersistentSet<T>().withMany(itemStream));
    }

    public SetRecord(Iterator<T> itemIterator) {
        this(new PersistentSet<T>().withMany(itemIterator));
    }

    public PersistentSet<T> values() {
        return values;
    }

    private final Supplier<Integer> lazyHash = new Lazy<>(() -> {
        // TODO implement quickHash in PersistentSet
        int hash = 1;
        for (final var value : values()) {
            hash ^= Objects.hashCode(value);
        }
        return hash;
    });

    @Override
    public int hashCode() {
        return lazyHash.get();
    }

    @Override
    public boolean equals(Object o) {
        // TODO equality caching
        if (!(o instanceof SetRecord<?> other)) return false;
        if (values.size() == 0 && other.values.size() == 0) return true;
        if (values.size() != other.values.size()) return false;
        if (hashCode() != other.hashCode()) return false;

        for (final var value : values) {
            if (!other.values.contains(value)) return false;
        }

        return true;
    }

    @Override
    public Iterator<T> iterator() {
        return values.iterator();
    }

    public Stream<T> stream() {
        return values.stream();
    }

    private final Supplier<String> lazyToString = new WeakLazy<>(() -> {
        final var builder = new StringBuilder();
        final var iter = iterator();

        builder.append("{ ");
        if (iter.hasNext()) builder.append(iter.next());
        while (iter.hasNext()) {
            builder.append(", ").append(iter.next());
        }
        builder.append(" }");

        return builder.toString();
    });

    @Override
    public String toString() {
        return lazyToString.get();
    }
}

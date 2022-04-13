package collections.records;

import collections.persistent.PersistentSet;
import memoization.pure.lazy.Lazy;
import memoization.pure.lazy.SoftLazy;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

// TODO add no-caching flag

public class SetRecord<T> implements Iterable<T>, Serializable {
    private final PersistentSet<T> set;

    public SetRecord(PersistentSet<T> set) {
        Objects.requireNonNull(set);
        this.set = set;
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
        return set;
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
        if (set.size() == 0 && other.set.size() == 0) return true;
        if (set.size() != other.set.size()) return false;
        if (hashCode() != other.hashCode()) return false;

        for (final var value : set) {
            if (!other.set.contains(value)) return false;
        }

        return true;
    }

    @Override
    public Iterator<T> iterator() {
        return set.iterator();
    }

    public Stream<T> stream() {
        return set.stream();
    }

    private final Supplier<String> lazyToString = new SoftLazy<>(() -> {
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

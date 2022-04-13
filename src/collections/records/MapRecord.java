package collections.records;

import collections.PersistentMap;
import memoization.pure.Lazy;
import memoization.pure.WeakLazy;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class MapRecord<K, V> implements Iterable<PersistentMap.Entry<K, V>>, Serializable {
    private final PersistentMap<K, V> entries;

    public PersistentMap<K, V> entries() {
        return entries;
    }

    public MapRecord(PersistentMap<K, V> entries) {
        this.entries = entries;
    }

    private final Supplier<Integer> lazyHash = new Lazy<>(() -> {
        int hash = 0;
        for (final var entry : entries().getEntries()) {
            hash ^= Objects.hash(entry.getKey(), entry.getValue());
        }
        return hash;
    });

    @Override
    public int hashCode() {
        return lazyHash.get();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MapRecord<?, ?> other)) return false;
        if (entries.size() == 0 && other.entries.size() == 0) return true;
        if (entries.size() != other.entries.size()) return false;
        if (hashCode() != other.hashCode()) return false;

        for (final var entry : entries.entrySet()) {
            final var otherValue = other.entries.get(entry.getKey());
            if (!Objects.equals(entry.getValue(), otherValue)) return false;
        }

        return true;
    }

    @Override
    public Iterator<PersistentMap.Entry<K, V>> iterator() {
        return entries.getEntries().iterator();
    }

    public Stream<PersistentMap.Entry<K, V>> stream() {
        return entries.stream();
    }

    private final Supplier<String> lazyToString = new WeakLazy<>(() -> {
        final var builder = new StringBuilder();
        final var iter = iterator();
        builder.append("{ ");
        if (iter.hasNext()) {
            final var entry = iter.next();
            builder.append(entry.getKey()).append(": ").append(entry.getValue());
        }
        while (iter.hasNext()) {
            final var entry = iter.next();
            builder.append(", ").append(entry.getKey()).append(": ").append(entry.getValue());
        }
        builder.append(" }");
        return builder.toString();
    });

    @Override
    public String toString() {
        return lazyToString.get();
    }
}

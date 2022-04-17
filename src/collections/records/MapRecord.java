package collections.records;

import collections.persistent.PersistentMap;
import collections.reference.WeakIdentityConcurrentHashMap;
import memoization.pure.lazy.Lazy;
import memoization.pure.lazy.SoftLazy;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

// TODO add no-caching flag

public class MapRecord<K, V> implements Iterable<Map.Entry<K, V>>, Serializable {
    private final PersistentMap<K, V> map;
    private final Map<MapRecord<?, ?>, Boolean> equalityCache = new WeakIdentityConcurrentHashMap<>();

    public PersistentMap<K, V> entries() {
        return map;
    }

    public MapRecord(PersistentMap<K, V> map) {
        this.map = map;
    }

    private final Supplier<Integer> getHash = new Lazy<>(() -> {
        int hash = 0;
        for (final var entry : entries().entrySet()) {
            hash ^= Objects.hash(entry.getKey(), entry.getValue());
        }
        return hash;
    });

    @Override
    public int hashCode() {
        return getHash.get();
    }

    @Override
    public boolean equals(Object o) {
        // TODO equality caching
        // check...
        // instances
        if (this == o) return true;

        // type
        if (!(o instanceof MapRecord<?, ?> other)) return false;

        // innards
        if (map == other.map) return true;

        // size
        if (map.size() == 0 && other.map.size() == 0) return true;
        if (map.size() != other.map.size()) return false;

        // cache
        final var fromCache = equalityCache.get(other);
        if (fromCache != null) return fromCache;
        else {
            final var fromOtherCache = other.equalityCache.get(this);
            if (fromOtherCache != null) return fromOtherCache;
        }

        // hash
        if (hashCode() != other.hashCode()) return false;

        // contents
        for (final var entry : map.entrySet()) {
            final var value = entry.getValue();
            final var key = entry.getKey();
            if (value == null) {
                if (other.entries().get(key) != null || !other.entries().containsKey(key)) {
                    equalityCache.put(other, false);
                    return false;
                }
            } else if (!Objects.equals(value, other.entries().get(key))) {
                equalityCache.put(other, false);
                return false;
            }
        }
        // equals = true

        equalityCache.put(other, true);
        return true;
    }

    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        return map.entrySet().iterator();
    }

    public Stream<Map.Entry<K, V>> stream() {
        return map.stream();
    }

    private final Supplier<String> toString = new SoftLazy<>(() -> {
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
        return toString.get();
    }
}

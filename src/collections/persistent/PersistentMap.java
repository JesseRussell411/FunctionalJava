package collections.persistent;

import annotations.UnsupportedOperation;
import collections.records.MapRecord;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class PersistentMap<K, V> extends AbstractMap<K, V> implements Serializable {
    @NotNull
    private final PersistentSet<Entry<K, V>> entries;

    public PersistentMap() {
        entries = new PersistentSet<>();
    }

    public PersistentMap(@NotNull PersistentSet<Entry<K, V>> entries) {
        this.entries = entries;
    }

    public int size() {
        return entries.size();
    }

    public PersistentMap<K, V> with(Entry<K, V> entry) {
        Objects.requireNonNull(entry);
        return new PersistentMap<>(entries.with(entry));
    }

    public PersistentMap<K, V> with(Map.Entry<K, V> entry) {
        Objects.requireNonNull(entry);
        return with(new Entry<>(entry.getKey(), entry.getValue()));
    }

    public PersistentMap<K, V> with(K key, V value) {
        return with(new Entry<>(key, value));
    }

    public PersistentMap<K, V> without(K key) {
        final var result = entries.without(new Entry<>(key, null));

        if (result.size() != entries.size()) {
            return new PersistentMap<>(result);
        } else return this;
    }

    @Override
    public V get(Object key) {
        final var entry = getEntry(key);

        if (entry != null) {
            return entry.getValue();
        } else return null;
    }

    public Entry<K, V> getEntry(Object key) {
        try {
            return entries.get(new Entry<>((K) key, null));
        } catch (ClassCastException cce) {
            return null;
        }
    }

    @Override
    public PersistentSet<Map.Entry<K, V>> entrySet() {
        return (PersistentSet) entries;
    }

    public PersistentSet<Entry<K, V>> getEntries() {
        return entries;
    }

    public Stream<Entry<K, V>> stream() {
        return stream(false);
    }

    public Stream<Entry<K, V>> stream(boolean parallel) {
        return entries.stream(parallel);
    }

    public MapRecord<K, V> asRecord() {
        return new MapRecord<>(this);
    }

    public boolean containsKey(Object key) {
        try {
            final var entry = entries.get(new Entry<>((K) key, null));
            return entry != null;
        } catch (ClassCastException cce) {
            return false;
        }
    }

    public record Entry<K, V>(K key, V value) implements Map.Entry<K, V>, Serializable {
        @Override
        public int hashCode() {
            return Objects.hashCode(key);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof PersistentMap.Entry<?, ?> other) {
                return Objects.equals(key, other.key);
            } else return false;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        @UnsupportedOperation
        public V setValue(V value) {
            throw new UnsupportedOperationException();
        }
    }
}

package collections.persistent;

import collections.adapters.AdapterSet;
import collections.records.MapRecord;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

public class PersistentMap<K, V> extends AbstractMap<K, V> implements Serializable {
    @NotNull
    private final PersistentSet<SpecialEntry<K, V>> entries;

    public PersistentMap() {
        entries = new PersistentSet<>();
    }

    public PersistentMap(@NotNull PersistentSet<SpecialEntry<K, V>> entries) {
        this.entries = entries;
    }

    public int size() {
        return entries.size();
    }

    public PersistentMap<K, V> with(SpecialEntry<K, V> entry) {
        Objects.requireNonNull(entry);
        return new PersistentMap<>(entries.with(entry));
    }

    public PersistentMap<K, V> with(K key, V value) {
        return with(new SpecialEntry<>(key, value));
    }

    public PersistentMap<K, V> without(K key) {
        final var result = entries.without(new SpecialEntry<>(key, null));

        if (result.size() != entries.size()) {
            return new PersistentMap<>(result);
        } else return this;
    }

    @Override
    public V get(Object key) {
        final var entry = getSpecialEntry(key);

        if (entry != null) {
            return entry.value;
        } else return null;
    }

    public Map.Entry<K, V> getEntry(Object key) {
        final var se = getSpecialEntry(key);
        if (se != null) {
            return new SimpleImmutableEntry<>(se.key, se.value);
        } else return null;
    }

    private SpecialEntry<K, V> getSpecialEntry(Object key) {
        try {
            return entries.get(new SpecialEntry<>((K) key, null));
        } catch (ClassCastException cce) {
            return null;
        }
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return new AdapterSet<>(
                entries,
                se -> new SimpleImmutableEntry<>(se.key, se.value),
                e -> new SpecialEntry<>(e.getKey(), e.getValue()));
    }

    public Stream<Map.Entry<K, V>> stream() {
        return stream(false);
    }

    public Stream<Map.Entry<K, V>> stream(boolean parallel) {
        return entries.stream(parallel).map(e -> new SimpleImmutableEntry<>(e.key, e.value));
    }

    public MapRecord<K, V> asRecord() {
        return new MapRecord<>(this);
    }

    public boolean containsKey(Object key) {
        try {
            final var entry = entries.get(new SpecialEntry<>((K) key, null));
            return entry != null;
        } catch (ClassCastException cce) {
            return false;
        }
    }

    private record SpecialEntry<K, V>(K key, V value) implements Serializable {
        @Override
        public int hashCode() {
            return Objects.hashCode(key);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj instanceof SpecialEntry<?, ?> other) {
                return Objects.equals(key, other.key);
            } else return false;
        }
    }
}

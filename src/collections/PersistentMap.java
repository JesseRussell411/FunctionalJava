package collections;

import java.util.Objects;

public class PersistentMap<K, V> {
    private final PersistentSet<Entry<K, V>> entries;

    public PersistentMap() {
        entries = new PersistentSet<>();
    }

    private PersistentMap(PersistentSet<Entry<K, V>> entries) {
        this.entries = entries;
    }

    public int size() {
        return entries.size();
    }

    public PersistentMap<K, V> with(K key, V value) {
        return new PersistentMap<>(entries.with(new Entry<>(key, value)));
    }

    public PersistentMap<K, V> without(K key) {
        final var result = entries.without(new Entry<>(key, null));

        if (result.size() != entries.size()) {
            return new PersistentMap<>(result);
        } else return this;
    }

    public V get(K key) {
        final var entry = entries.get(new Entry<>(key, null));
        if (entry == null) return null;
        return entry.value;
    }

    public boolean containsKey(K key) {
        final var entry = entries.get(new Entry<>(key, null));
        return entry != null;
    }

    private record Entry<K, V>(K key, V value) {
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
    }
}

package collections.reference;

import collections.adapters.AdapterSet;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.*;

public class SoftHashMap<K, V> extends AbstractMap<K, V> {
    private final Map<Reference<? extends K>, V> data = new HashMap<>();
    private final ReferenceQueue<K> possiblyCollected = new ReferenceQueue<>();

    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        prune();
        return new AdapterSet<>(
                data.entrySet(),
                a -> new SimpleEntry<>(a.getKey().get(), a.getValue()),
                b -> Map.entry(new Key<>(b.getKey()), b.getValue()));
    }

    @Override
    public V put(@NotNull K key, V value) {
        Objects.requireNonNull(key);
        prune();
        return data.put(new Key<>(key, possiblyCollected), value);
    }

    @Override
    public V get(Object key) {
        prune();
        if (key == null) return null;
        return data.get(new Key<>(key));
    }

    @Override
    public boolean containsKey(Object key) {
        prune();
        return data.containsKey(new Key<>(key));
    }

    @Override
    public boolean containsValue(Object value) {
        prune();
        return data.containsValue(value);
    }

    @Override
    public V remove(@NotNull Object key) {
        prune();
        return data.remove(new Key<>(key));
    }

    @Override
    public boolean remove(Object key, Object value) {
        prune();
        return data.remove(new Key<>(key), value);
    }

    private void prune() {
        for (Reference<? extends K> ref; (ref = possiblyCollected.poll()) != null; ) {
            if (ref.get() == null) data.remove(ref);
        }
    }


    private static class Key<K> extends SoftReference<K> {
        public final K kHolder;
        public final int hashCode;

        public Key(K key, ReferenceQueue<K> queue) {
            super(key, queue);
            kHolder = null;
            hashCode = Objects.hashCode(key);
        }

        /**
         * Creates new Key which maintains a strong reference, for querying purposes.
         */
        public Key(K key) {
            super(key);
            kHolder = key;
            hashCode = Objects.hashCode(key);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;

            if (!(obj instanceof Key<?> other)) return false;

            final var inst = this.get();
            final var otherInst = other.get();
            if (inst == null || otherInst == null) return false;

            return Objects.equals(inst, otherInst);
        }
    }
}

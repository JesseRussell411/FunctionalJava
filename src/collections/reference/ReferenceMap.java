package collections.reference;

import collections.adapters.AdapterSet;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

abstract class ReferenceMap<K, V> extends AbstractMap<K, V> {
    private final Map<Reference<? extends K>, V> data;
    private final ReferenceQueue<K> possiblyCollected = new ReferenceQueue<>();

    @NotNull
    protected abstract Map<Reference<? extends K>, V> buildData();

    @NotNull
    protected abstract <T> Reference<T> buildKey(T k, ReferenceQueue<T> queue);

    @NotNull
    protected abstract <T> Reference<T> buildKey(T k);

    public ReferenceMap() {
        data = buildData();
    }

    @Override
    public int size() {
        prune();
        return data.size();
    }

    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        prune();

        return new AdapterSet<>(
                data.entrySet(),
                a -> new SimpleImmutableEntry<>(a.getKey().get(), a.getValue()),
                b -> Map.entry(buildKey(b.getKey()), b.getValue()));
    }

    @Override
    public V put(K key, V value) {
        try {
            return data.put(buildKey(key, possiblyCollected), value);
        } finally {
            prune();
        }
    }

    @Override
    public V get(Object key) {
        prune();
        return data.get(buildKey(key));
    }

    @Override
    public boolean containsKey(Object key) {
        prune();
        return data.containsKey(buildKey(key));
    }

    @Override
    public boolean containsValue(Object value) {
        prune();
        return data.containsValue(value);
    }

    @Override
    public V remove(Object key) {
        try {
            return data.remove(buildKey(key));
        } finally {
            prune();
        }
    }

    @Override
    public boolean remove(Object key, Object value) {
        try {
            return data.remove(buildKey(key), value);
        } finally {
            prune();
        }
    }

    private void prune() {
        Reference<? extends K> ref;
        while ((ref = possiblyCollected.poll()) != null) {
            if (ref.get() == null) data.remove(ref);
        }
    }
}

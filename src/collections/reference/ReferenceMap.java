package collections.reference;

import collections.adapters.AdapterSet;
import collections.adapters.ExtensionSet;
import collections.persistent.PersistentSet;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

abstract class ReferenceMap<K, V> extends AbstractMap<K, V> {
    private final Map<Reference<? extends K>, V> data;
    private final ReferenceQueue<K> possiblyCollected = new ReferenceQueue<>();
    private volatile boolean containsNullKey = false;
    private volatile V nullKeyValue = null;


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
        return data.size() + (containsNullKey ? 1 : 0);
    }

    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        prune();

        final Set<Entry<K, V>> adaptedSet = new AdapterSet<>(
                data.entrySet(),
                a -> new SimpleImmutableEntry<>(a.getKey().get(), a.getValue()),
                b -> Map.entry(buildKey(b.getKey()), b.getValue()));

        if (containsNullKey) {
            return new ExtensionSet<>(
                    adaptedSet,
                    PersistentSet.of(
                            new SimpleImmutableEntry<>(null, nullKeyValue)));
        } else {
            return adaptedSet;
        }
    }

    @Override
    public V put(K key, V value) {
        prune();
        if (key == null) {
            containsNullKey = true;
            final var prevValue = nullKeyValue;
            nullKeyValue = value;
            return prevValue;
        }

        return data.put(buildKey(key, possiblyCollected), value);
    }

    @Override
    public V get(Object key) {
        prune();
        if (key == null) return nullKeyValue;
        return data.get(buildKey(key));
    }

    @Override
    public boolean containsKey(Object key) {
        prune();
        if (key == null) return containsNullKey;
        return data.containsKey(buildKey(key));
    }

    @Override
    public boolean containsValue(Object value) {
        prune();
        return data.containsValue(value);
    }

    @Override
    public V remove(Object key) {
        prune();
        if (key == null) {
            containsNullKey = false;
            final var prevValue = nullKeyValue;
            nullKeyValue = null;
            return prevValue;
        }
        return data.remove(buildKey(key));
    }

    @Override
    public boolean remove(Object key, Object value) {
        prune();
        if (key == null) {
            if (containsNullKey && Objects.equals(nullKeyValue, value)) {
                containsNullKey = false;
                nullKeyValue = null;
                return true;
            } else return false;
        }
        return data.remove(buildKey(key), value);
    }

    private void prune() {
        for (Reference<? extends K> ref; (ref = possiblyCollected.poll()) != null; ) {
            if (ref.get() == null) data.remove(ref);
        }
    }
}

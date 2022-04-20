package collections.decorators;

import collections.adapters.ExtensionSet;
import collections.persistent.PersistentSet;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class VolatileNullSafeMap<K, V> implements Map<K, V> {
    private final Map<K, V> base;
    private volatile boolean containsNullKey = false;
    private volatile V nullKeyValue = null;

    public VolatileNullSafeMap(Map<K, V> base) {
        this.base = base;
    }

    @Override
    public int size() {
        return base.size() + (containsNullKey ? 1 : 0);
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {

        if (containsNullKey) {
            return new ExtensionSet<>(
                    base.entrySet(),
                    PersistentSet.of(
                            new AbstractMap.SimpleImmutableEntry<>(null, nullKeyValue)));
        } else {
            return new UnmodifiableSet<>(base.entrySet());
        }
    }

    @Override
    public boolean equals(Object o) {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public V put(K key, V value) {
        if (key == null) {
            try {
                return nullKeyValue;
            } finally {
                containsNullKey = true;
                nullKeyValue = value;
            }
        }

        return base.put(key, value);
    }

    @Override
    public V get(Object key) {
        if (key == null) return nullKeyValue;
        return base.get(key);
    }

    @Override
    public boolean containsKey(Object key) {
        if (key == null) return containsNullKey;
        return base.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return base.containsValue(value);
    }

    @Override
    public V remove(Object key) {
        if (key == null) {
            try {
                return nullKeyValue;
            } finally {
                nullKeyValue = null;
                containsNullKey = false;
            }
        }
        return base.remove(key);
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> m) {
        for (final var entry : m.entrySet()) {
            if (entry.getKey() == null) {
                containsNullKey = true;
                nullKeyValue = entry.getValue();
            } else {
                base.put(entry.getKey(), entry.getValue());
            }
        }
    }


    @Override
    public void clear() {
        base.clear();
        nullKeyValue = null;
        containsNullKey = false;
    }

    @NotNull
    @Override
    public Set<K> keySet() {
        if (containsNullKey) {
            return new ExtensionSet<>(base.keySet(), new PersistentSet<K>().with(null));
        } else {
            return base.keySet();
        }
    }

    @NotNull
    @Override
    public Collection<V> values() {
        if (containsNullKey) {
            final var listOfNull = new ArrayList<V>(1);
            listOfNull.add(null);
            return new ExtensionCollection<>(base.values(), listOfNull);
        } else {
            return new UnmodifiableCollection<>(base.values());
        }
    }

    @Override
    public boolean remove(Object key, Object value) {
        if (key == null) {
            if (containsNullKey && Objects.equals(nullKeyValue, value)) {
                containsNullKey = false;
                nullKeyValue = null;
                return true;
            } else return false;
        }
        return base.remove(key, value);
    }
}

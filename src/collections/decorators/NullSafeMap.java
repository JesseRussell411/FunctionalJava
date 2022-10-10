package collections.decorators;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class NullSafeMap<K, V> implements Map<K, V> {
    private final Map<K, V> base;
    private volatile boolean hasNull = false;
    private volatile V nullValue = null;

    public NullSafeMap(Map<K, V> base) {
        this.base = base;
    }

    @Override
    public int size() {
        return base.size() + (hasNull ? 1 : 0);
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        if (hasNull) {
            return new AppendedSet<>(
                    base.entrySet(),
                    new AbstractMap.SimpleImmutableEntry<>(null, nullValue));
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
                return nullValue;
            } finally {
                hasNull = true;
                nullValue = value;
            }
        }

        return base.put(key, value);
    }

    @Override
    public V get(Object key) {
        if (key == null) return nullValue;
        return base.get(key);
    }

    @Override
    public boolean containsKey(Object key) {
        if (key == null) return hasNull;
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
                return nullValue;
            } finally {
                nullValue = null;
                hasNull = false;
            }
        }
        return base.remove(key);
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> m) {
        for (final var entry : m.entrySet()) {
            if (entry.getKey() == null) {
                hasNull = true;
                nullValue = entry.getValue();
            } else {
                base.put(entry.getKey(), entry.getValue());
            }
        }
    }


    @Override
    public void clear() {
        base.clear();
        nullValue = null;
        hasNull = false;
    }

    @NotNull
    @Override
    public Set<K> keySet() {
        if (hasNull) {
            return new AppendedSet<>(base.keySet(), null);
        } else {
            return base.keySet();
        }
    }

    @NotNull
    @Override
    public Collection<V> values() {
        if (hasNull) {
            return new AppendedCollection<>(base.values(), null);
        } else {
            return new UnmodifiableCollection<>(base.values());
        }
    }

    @Override
    public boolean remove(Object key, Object value) {
        if (key == null) {
            if (hasNull && Objects.equals(nullValue, value)) {
                hasNull = false;
                nullValue = null;
                return true;
            } else return false;
        }
        return base.remove(key, value);
    }
}

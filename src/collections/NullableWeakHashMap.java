package collections;

import java.util.*;

public class NullableWeakHashMap<K, V> implements Map<K, V> {
    private final WeakHashMap<K, Ref<V>> entries;

    public NullableWeakHashMap(int initialSize, float loadFactor) {
        entries = new WeakHashMap<>(initialSize, loadFactor);
    }

    public NullableWeakHashMap(int initialSize) {
        entries = new WeakHashMap<>(initialSize);
    }

    public NullableWeakHashMap() {
        entries = new WeakHashMap<>();
    }

    @Override
    public int size() {
        return entries.size();
    }

    @Override
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return entries.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return entries.containsValue(new Ref<>(value));
    }

    @Override
    public V get(Object key) {
        final var result = entries.get(key);
        if (result == null) {
            return null;
        } else {
            return result.value;
        }
    }

    @Override
    public V put(K key, V value) {
        final var result = entries.put(key, new Ref<>(value));
        if (result == null) {
            return null;
        } else {
            return result.value;
        }
    }

    @Override
    public V remove(Object key) {
        final var result = entries.remove(key);
        if (result == null) {
            return null;
        } else {
            return result.value;
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        final var mappedMap = new HashMap<K, Ref<V>>();
        for (final var entry : m.entrySet()) {
            mappedMap.put(entry.getKey(), new Ref(entry.getValue()));
        }
        entries.putAll(mappedMap);
    }

    @Override
    public void clear() {
        entries.clear();
    }

    @Override
    public Set<K> keySet() {
        return entries.keySet();
    }

    @Override
    public Collection<V> values() {
        final var valueList = new ArrayList<V>(size());
        for (final var entry : entries.entrySet()) {
            final var valueRef = entry.getValue();
            if (valueRef != null) valueList.add(valueRef.value);
        }
        valueList.trimToSize();
        return valueList;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        final var result = new HashSet<Entry<K, V>>(size());
        for (final var entry : entries.entrySet()) {
            final var valueRef = entry.getValue();
            if (valueRef != null) result.add(Map.entry(entry.getKey(), valueRef.value));
        }
        return result;
    }


    private record Ref<T>(T value) {
    }
}

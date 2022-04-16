package collections;

import collections.adapters.SetAdapter;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SoftConcurrentHashMap<K, V> extends AbstractMap<K, V> {
    private final ConcurrentHashMap<Reference<? extends K>, V> data = new ConcurrentHashMap<>();
    private final ReferenceQueue<K> dataQueue = new ReferenceQueue<>();
    private final Lock queueLock = new ReentrantLock();

    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        prune();
        return new SetAdapter<>(
                data.entrySet(),
                a -> new SimpleEntry<>(a.getKey().get(), a.getValue()),
                b -> Map.entry(new Key<>(b.getKey()), b.getValue()));
    }

    @Override
    public V put(K key, V value) {
        prune();
        return data.put(new Key<>(key, dataQueue), value);
    }

    @Override
    public V get(Object key) {
        prune();
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
        if (queueLock.tryLock()) {
            try {
                for (Reference<? extends K> ref; (ref = dataQueue.poll()) != null; ) {
                    if (ref.get() == null) data.remove(ref);
                }
            } finally {
                queueLock.unlock();
            }
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

package collections.reference;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WeakIdentityConcurrentHashMap<K, V> extends ReferenceMap<K, V> {
    @NotNull
    @Override
    protected Map<Reference<? extends K>, V> buildData() {
        return new ConcurrentHashMap<>();
    }

    @NotNull
    @Override
    protected <T> Reference<T> buildKey(T k) {
        return new WeakIdentityKey<>(k);
    }

    @NotNull
    @Override
    protected <T> Reference<T> buildKey(T k, ReferenceQueue<T> queue) {
        return new WeakIdentityKey<>(k, queue);
    }
}
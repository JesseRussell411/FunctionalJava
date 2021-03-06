package collections.reference;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.HashMap;
import java.util.Map;

public class SoftHashMap<K, V> extends ReferenceMap<K, V> {
    @NotNull
    @Override
    protected Map<Reference<? extends K>, V> buildData() {
        return new HashMap<>();
    }

    @NotNull
    @Override
    protected <T> Reference<T> buildKey(T k) {
        return new SoftKey<>(k);
    }

    @NotNull
    @Override
    protected <T> Reference<T> buildKey(T k, ReferenceQueue<T> queue) {
        return new SoftKey<>(k, queue);
    }
}
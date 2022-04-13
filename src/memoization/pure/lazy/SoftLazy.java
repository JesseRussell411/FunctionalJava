package memoization.pure.lazy;

import reference.FinalPointer;

import java.lang.ref.SoftReference;
import java.util.Objects;
import java.util.function.Supplier;

public class SoftLazy<T> implements Supplier<T> {
    private Supplier<T> original;
    private volatile SoftReference<FinalPointer<T>> cache = new SoftReference<>(null);
    private volatile RuntimeException exceptionCache = null;

    public SoftLazy(Supplier<T> original) {
        this.original = Objects.requireNonNull(original);
    }

    public T get() {
        var fromCache = cache.get();
        if (fromCache != null) return fromCache.current;
        if (exceptionCache != null) throw exceptionCache;

        synchronized (this) {
            fromCache = cache.get();
            if (fromCache != null) return fromCache.current;
            if (exceptionCache != null) throw exceptionCache;

            try {
                final var result = original.get();
                cache = new SoftReference<>(new FinalPointer<>(result));
                return result;
            } catch (RuntimeException error) {
                exceptionCache = error;
                original = null;
                throw error;
            }
        }
    }
}

package memoization.pure;

import reference.FinalPointer;
import reference.NullableWeakReference;
import reference.VolatileUntilSet;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.function.Supplier;

public class WeakLazy<T> implements Supplier<T> {
    private Supplier<T> original;
    private volatile WeakReference<FinalPointer<T>> cache = new WeakReference<>(null);
    private volatile RuntimeException exceptionCache = null;

    public WeakLazy(Supplier<T> original) {
        this.original = Objects.requireNonNull(original);
    }

    public T get() {
        var fromCache = this.cache.get();
        if (fromCache != null) return fromCache.current;
        if (exceptionCache != null) throw exceptionCache;

        synchronized (this) {
            fromCache = this.cache.get();
            if (fromCache != null) return fromCache.current;
            if (exceptionCache != null) throw exceptionCache;

            try {
                final var result = original.get();
                cache = new WeakReference<>(new FinalPointer<>(result));
                return result;
            } catch (RuntimeException error) {
                exceptionCache = error;
                original = null;
                throw error;
            }
        }
    }
}

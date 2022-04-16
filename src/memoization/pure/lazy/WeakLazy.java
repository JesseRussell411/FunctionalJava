package memoization.pure.lazy;

import reference.FinalPointer;

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
        var fromCache = cache.get();
        if (fromCache != null) return fromCache.current;
        if (exceptionCache != null) throw exceptionCache;

        synchronized (this) {
            fromCache = cache.get();
            if (fromCache != null) return fromCache.current;
            if (exceptionCache != null) throw exceptionCache;

            T result;
            try {
                result = original.get();
            } catch (RuntimeException error) {
                exceptionCache = error;
                original = null;
                throw error;
            }

            cache = new WeakReference<>(new FinalPointer<>(result));
            return result;
        }
    }
}

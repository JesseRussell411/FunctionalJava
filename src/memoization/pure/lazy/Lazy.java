package memoization.pure.lazy;

import reference.VolatileUntilSet;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * A memoized {@link Supplier}. Possibly useful for lazy execution, hence the name.
 * @param <T>
 */
public class Lazy<T> implements Supplier<T> {
    private Supplier<T> original;
    private final VolatileUntilSet<Supplier<T>> cache = new VolatileUntilSet<>();

    public Lazy(Supplier<T> original) {
        this.original = Objects.requireNonNull(original);
    }

    public T get() {
        if (cache.isSet()) return cache.get().get();

        synchronized (this) {
            if (cache.isSet()) return cache.get().get();

            T result;
            try {
                result = original.get();
            } catch (RuntimeException error) {
                cache.set(() -> {
                    throw error;
                });
                original = null;
                throw error;
            }

            cache.set(() -> result);
            original = null;
            return result;
        }
    }

    public boolean isCached() {
        return cache.isSet();
    }
}

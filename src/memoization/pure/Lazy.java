package memoization.pure;

import java.util.Objects;
import java.util.function.Supplier;

public class Lazy<T> implements Supplier<T> {
    private Supplier<T> original;
    private volatile Supplier<T> cache = null;

    public Lazy(Supplier<T> original) {
        Objects.requireNonNull(original);

        this.original = original;
    }

    public T get() {
        var cache = this.cache;
        if (cache != null) return cache.get();

        synchronized (this) {
            cache = this.cache;
            if (cache != null) return cache.get();

            try {
                final var result = original.get();
                cache = () -> result;
            } catch (RuntimeException error) {
                cache = () -> {
                    throw error;
                };
            }

            original = null;
            this.cache = cache;
        }
        return cache.get();
    }
}

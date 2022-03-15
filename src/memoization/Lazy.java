package memoization;

import java.util.function.Supplier;

public class Lazy<T> implements Supplier<T> {
    private Supplier<T> original;
    private Supplier<T> cache = null;

    public Lazy(Supplier<T> original) {
        this.original = original;
    }

    public T get() {
        if (cache != null) return cache.get();

        synchronized (this) {
            if (cache != null) return cache.get();

            try {
                final var result = original.get();
                cache = () -> result;
            } catch (RuntimeException e) {
                cache = () -> {
                    throw e;
                };
            }
        }

        original = null;
        return cache.get();
    }
}

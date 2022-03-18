package memoization.pure;

import references.Init;

import java.util.Objects;
import java.util.function.Supplier;

public class Lazy<T> implements Supplier<T> {
    private Supplier<T> original;
    private final Init<Supplier<T>> cache = new Init<>();

    public Lazy(Supplier<T> original) {
        Objects.requireNonNull(original);

        this.original = original;
    }

    public T get() {
        if (cache.isSet()) return cache.get().get();

        synchronized (this) {
            if (cache.isSet()) return cache.get().get();

            try {
                final var result = original.get();
                cache.set(() -> result);
            } catch (RuntimeException error) {
                cache.set(() -> {
                    throw error;
                });
            }

            original = null;
        }

        return cache.get().get();
    }
}

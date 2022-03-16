package memoization;

import java.util.Objects;
import java.util.function.Supplier;

public class Lazy<T> implements Supplier<T> {
    private volatile Supplier<T> getter;

    public Lazy(Supplier<T> original) {
        Objects.requireNonNull(original);

        getter = () -> hardGet(original);
    }

    private synchronized T hardGet(Supplier<T> original) {
        try {
            final var result = original.get();
            this.getter = () -> result;
            return result;
        } catch (RuntimeException error) {
            this.getter = () -> {
                throw error;
            };
            throw error;
        }
    }

    public T get() {
        return getter.get();
    }
}

package memoization;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

public class MemoizedFunction<T, R> implements Function<T, R> {
    private final Function<T, R> original;
    private final Map<T, Supplier<R>> cache = newCache();

    protected Map<T, Supplier<R>> newCache() {
        return new ConcurrentHashMap<>();
    }

    public MemoizedFunction(Function<T, R> original) {
        Objects.requireNonNull(original);
        this.original = original;
    }

    public R apply(T arg) {
        // Check the cache.
        var fromCache = cache.get(arg);
        if (fromCache != null) return fromCache.get();

        synchronized (this) {
            // Check the cache again. It could have changed in the time it took to synchronize.
            fromCache = cache.get(arg);
            if (fromCache != null) return fromCache.get();

            // Cache miss, calculate value for real.
            try {
                final var result = original.apply(arg);
                cache.put(arg, () -> result);
                return result;
            } catch (RuntimeException e) {
                cache.put(arg, () -> {
                    throw e;
                });
                throw e;
            }
        }
    }
}

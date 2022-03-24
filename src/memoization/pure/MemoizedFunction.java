package memoization.pure;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

public class MemoizedFunction<T, R> implements Function<T, R> {
    private final Function<T, R> original;
    private final Map<T, Supplier<R>> cache = Objects.requireNonNull(initCache());

    protected Map<T, Supplier<R>> initCache() {
        return new ConcurrentHashMap<>();
    }

    public MemoizedFunction(Function<T, R> original) {
        Objects.requireNonNull(original);
        this.original = original;
    }

    public R hardApply(T t) {
        return original.apply(t);
    }

    public R apply(T t) {
        // Check the cache.
        var fromCache = cache.get(t);
        if (fromCache != null) return fromCache.get();

        // Cache miss, calculate value for real.
        try {
            final var result = original.apply(t);
            cache.put(t, () -> result);
            return result;
        } catch (RuntimeException e) {
            cache.put(t, () -> {
                throw e;
            });
            throw e;
        }
    }
}

package memoization.pure;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

public class MemoizedFunction<T, R> implements Function<T, R> {
    private final Function<T, R> original;
    private final Map<T, Supplier<R>> cache = Objects.requireNonNull(initCache());
    private final VolatileUntilSet<Supplier<R>> nullEntryCache = new VolatileUntilSet<>();

    private Supplier<R> fromCache(T t) {
        if (t == null) {
            return nullEntryCache.get();
        } else {
            return cache.get(t);
        }
    }

    private void cacheResult(T t, Supplier<R> result) {
        if (t == null) {
            nullEntryCache.set(result);
        } else {
            cache.putIfAbsent(t, result);
        }
    }


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
        var fromCache = fromCache(t);
        if (fromCache != null) return fromCache.get();

        // Cache miss, calculate value for real.
        try {
            final var result = original.apply(t);
            cacheResult(t, () -> result);
            return result;
        } catch (RuntimeException e) {
            cacheResult(t, () -> {
                throw e;
            });
            throw e;
        }
    }
}

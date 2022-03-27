package memoization.pure;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

public class MemoizedFunction<T, R> implements Function<T, R> {
    private final Function<T, R> original;
    private final Map<Argument<T>, Supplier<R>> cache = Objects.requireNonNull(initCache());

    private Supplier<R> fromCache(T t) {
        return cache.get(new Argument<>(t));
    }

    private void cacheResult(T t, Supplier<R> result) {
        cache.put(new Argument<>(t), result);
    }

    protected Map<Argument<T>, Supplier<R>> initCache() {
        return new ConcurrentHashMap<>();
    }

    public MemoizedFunction(Function<T, R> original) {
        this.original = Objects.requireNonNull(original);
    }

    public R apply(T t) {
        // Check the cache.
        final var fromCache = fromCache(t);
        if (fromCache != null) return fromCache.get();

        // Cache miss, calculate t for real.
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

    public R hardApply(T t) {
        return original.apply(t);
    }

    public R cacheApply(T t) {
        final var fromCache = fromCache(t);
        if (fromCache != null) {
            return fromCache.get();
        } else {
            return null;
        }
    }

    public boolean isCached(T t) {
        return fromCache(t) != null;
    }

    protected record Argument<T>(T t) {
    }
}

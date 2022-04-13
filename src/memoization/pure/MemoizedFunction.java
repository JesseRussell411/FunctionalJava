package memoization.pure;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Memoization decorator for the {@link Function} interface. It is assumed that the original function is pure;
 * ie: no side effects and no mutable dependencies; the result is determined only by the input parameter, t.
 * @param <T> Input type.
 * @param <R> Return type.
 */
public class MemoizedFunction<T, R> implements Function<T, R> {
    private final Function<T, R> original;
    private final Map<Argument<T>, Supplier<R>> cache = Objects.requireNonNull(initCache());

    private Supplier<R> cacheGet(T t) {
        return cache.get(new Argument<>(t));
    }

    private void cachePut(T t, Supplier<R> result) {
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
        final var fromCache = cacheGet(t);
        if (fromCache != null) return fromCache.get();

        // Cache miss, calculate result for real.
        R result;
        try {
            result = original.apply(t);
        } catch (RuntimeException e) {
            cachePut(t, () -> {
                throw e;
            });
            throw e;
        }

        cachePut(t, () -> result);
        return result;
    }

    /**
     * Circumvent the cache and call the original function.
     */
    public R hardApply(T t) {
        return original.apply(t);
    }

    /**
     * Get the result of t from the cache.
     * @return The cached result or null if the result is not cached.
     */
    public R cacheApply(T t) {
        final var fromCache = cacheGet(t);
        if (fromCache != null) {
            return fromCache.get();
        } else {
            return null;
        }
    }

    /**
     * @return Whether the result of t is cached.
     */
    public boolean isCached(T t) {
        return cacheGet(t) != null;
    }

    /** Wrapper to allow for null values. */
    protected record Argument<T>(T t) {
    }
}

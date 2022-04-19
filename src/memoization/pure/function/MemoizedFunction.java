package memoization.pure.function;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Memoization decorator for {@link Function}. It is assumed that the original function is pure,
 * ie: no side effects and no mutable dependencies, and the result is determined only by the input parameter: t.
 *
 * @param <T> Input type.
 * @param <R> Return type.
 */
public class MemoizedFunction<T, R> implements Function<T, R> {
    @NotNull
    private final Function<T, R> original;
    @NotNull
    private final Map<T, Supplier<R>> cache = buildCache();

    /**
     * @return The map to be used as a cache. If null values are to be given to the function, then the map must allow null keys.
     */
    @NotNull
    protected Map<T, Supplier<R>> buildCache() {
        return new ConcurrentHashMap<>();
    }

    public MemoizedFunction(@NotNull Function<T, R> original) {
        this.original = Objects.requireNonNull(original);
    }

    public R apply(T t) {
        // Check the cache.
        final var fromCache = cache.get(t);
        if (fromCache != null) return fromCache.get();

        // Cache miss, calculate result for real.
        // it's ok that this can run in multiple threads at the same time
        // because it is assumed that the original function is pure and therefor causes no side effects.
        R result;
        try {
            result = original.apply(t);
        } catch (RuntimeException e) {
            cache.put(t, () -> {
                throw e;
            });
            throw e;
        }

        cache.put(t, () -> result);
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
     *
     * @return The cached result or null if the result is not cached.
     */
    public R cacheApply(T t) {
        final var fromCache = cache.get(t);
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
        return cache.get(t) != null;
    }
}

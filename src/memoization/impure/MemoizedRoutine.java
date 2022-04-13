package memoization.impure;

import collections.records.ListRecord;
import org.jetbrains.annotations.NotNull;
import reference.VolatileUntilSet;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class MemoizedRoutine<T, R> implements BiFunction<T, ListRecord<?>, R> {
    @NotNull
    private final Map<Context<T>, VolatileUntilSet<Supplier<R>>> cache = new ConcurrentHashMap<>();
    @NotNull
    private final Function<T, R> subRoutine;

    public MemoizedRoutine(@NotNull Function<T, R> subRoutine) {
        Objects.requireNonNull(subRoutine);
        this.subRoutine = subRoutine;
    }

    private VolatileUntilSet<Supplier<R>> getCachedResult(Context<T> context) {
        final var newResult = new VolatileUntilSet<Supplier<R>>();
        final var existingResult = cache.putIfAbsent(context, new VolatileUntilSet<>());
        return existingResult == null ? newResult : existingResult;
    }

    @Override
    public R apply(T argument, ListRecord<?> dependencies) {
        Objects.requireNonNull(dependencies);
        final var result = getCachedResult(new Context<>(argument, dependencies));

        // check cache
        var cacheValue = result.get();
        if (cacheValue != null) return cacheValue.get();

        // lock and check again
        synchronized (result) {
            cacheValue = result.get();
            if (cacheValue != null) return cacheValue.get();

            // run the subroutine
            R newValue;
            try {
                newValue = subRoutine.apply(argument);
            } catch (RuntimeException re) {
                result.set(() -> {
                    throw re;
                });
                throw re;
            }
            result.set(() -> newValue);
            return newValue;
        }
    }

    public R apply(T argument, Object[] dependencies) {
        return apply(argument, new ListRecord<>(dependencies));
    }

    public R apply(T argument, Iterable<?> dependencies) {
        return apply(argument, new ListRecord<>(dependencies));
    }

    public R apply(T argument, Stream<T> dependencies) {
        return apply(argument, new ListRecord<>(dependencies));
    }

    public R apply(T argument, Iterator<?> dependencies) {
        return apply(argument, new ListRecord<>(dependencies));
    }

    public R cacheApply(T argument, ListRecord<?> dependencies) {
        final var cachedResult = cache.get(new Context<>(argument, dependencies));

        if (cachedResult != null) {
            final var fromCache = cachedResult.get();

            if (fromCache != null) {
                return fromCache.get();
            } else return null;
        } else return null;
    }

    public R cacheApply(T argument, Object[] dependencies) {
        return cacheApply(argument, new ListRecord<>(dependencies));
    }

    public R cacheApply(T argument, Iterable<?> dependencies) {
        return cacheApply(argument, new ListRecord<>(dependencies));
    }

    public R cacheApply(T argument, Stream<T> dependencies) {
        return cacheApply(argument, new ListRecord<>(dependencies));
    }

    public R cacheApply(T argument, Iterator<?> dependencies) {
        return cacheApply(argument, new ListRecord<>(dependencies));
    }

    public R hardApply(T argument) {
        return subRoutine.apply(argument);
    }

    public boolean isCached(T argument, ListRecord<?> dependencies) {
        return cache.get(new Context<>(argument, dependencies)) != null;
    }

    public boolean isCached(T argument, Object[] dependencies) {
        return isCached(argument, new ListRecord<>(dependencies));
    }

    public boolean isCached(T argument, Iterable<?> dependencies) {
        return isCached(argument, new ListRecord<>(dependencies));
    }

    public boolean isCached(T argument, Stream<?> dependencies) {
        return isCached(argument, new ListRecord<>(dependencies));
    }

    public boolean isCached(T argument, Iterator<?> dependencies) {
        return isCached(argument, new ListRecord<>(dependencies));
    }

    record Context<T>(T argument, ListRecord<?> dependencies) {
    }
}
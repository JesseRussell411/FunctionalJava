package memoization.impure;

import collections.reference.SoftConcurrentHashMap;
import org.jetbrains.annotations.NotNull;
import reference.VolatileUntilSet;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class SoftMemoizedRoutine<T, R> extends MemoizedRoutine<T, R> {
    @Override
    @NotNull
    protected Map<Context<T>, VolatileUntilSet<Supplier<R>>> buildCache() {
        return new SoftConcurrentHashMap<>();
    }

    public SoftMemoizedRoutine(@NotNull Function<T, R> subRoutine) {
        super(subRoutine);
    }
}

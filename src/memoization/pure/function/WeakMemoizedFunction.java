package memoization.pure.function;

import collections.reference.WeakConcurrentHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class WeakMemoizedFunction<T, R> extends MemoizedFunction<T, R> {
    public WeakMemoizedFunction(@NotNull Function<T, R> original) {
        super(original);
    }

    @NotNull
    @Override
    protected Map<T, Supplier<R>> buildCache() {
        return new WeakConcurrentHashMap<>();
    }
}
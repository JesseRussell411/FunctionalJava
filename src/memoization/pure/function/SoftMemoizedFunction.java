package memoization.pure.function;

import collections.reference.SoftConcurrentHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class SoftMemoizedFunction<T, R> extends MemoizedFunction<T, R> {
    public SoftMemoizedFunction(@NotNull Function<T, R> original) {
        super(original);
    }

    @NotNull
    @Override
    protected Map<T, Supplier<R>> buildCache() {
        return new SoftConcurrentHashMap<>();
    }
}

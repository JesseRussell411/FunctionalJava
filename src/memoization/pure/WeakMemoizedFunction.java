package memoization.pure;

import collections.ReadWriteLockedMap;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

public class WeakMemoizedFunction<T, R> extends MemoizedFunction<T, R> {
    public WeakMemoizedFunction(Function<T, R> original) {
        super(original);
    }

    @Override
    protected Map<Argument<T>, Supplier<R>> initCache() {
        return new ReadWriteLockedMap<>(new WeakHashMap<>());
    }
}

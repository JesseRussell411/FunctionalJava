package memoization;

import java.util.Objects;
import java.util.function.Function;

public class MemoizedRoutine<T, R> implements Function<T, R> {
    private final MemoizedFunction<Dependencies, R> func;

    public MemoizedRoutine(Function<T, R> original) {
        Objects.requireNonNull(original);

        func = new MemoizedFunction<>(
                (dependencies) -> original.apply((T) dependencies.items[0]));
    }

    public R apply(T t) {
        return func.apply(new Dependencies(new Object[]{t}));
    }

    public R apply(T t, Object[] dependencies) {
        Objects.requireNonNull(dependencies);

        final var allDeps = new Object[dependencies.length + 1];
        allDeps[0] = t;
        System.arraycopy(dependencies, 0, allDeps, 1, dependencies.length);
        return func.apply(new Dependencies(allDeps));
    }
}
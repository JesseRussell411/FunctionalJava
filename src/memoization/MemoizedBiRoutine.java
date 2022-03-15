package memoization;

import java.util.Objects;
import java.util.function.BiFunction;

public class MemoizedBiRoutine<T, U, R> implements BiFunction<T, U, R> {
    private final MemoizedFunction<Dependencies, R> func;

    public MemoizedBiRoutine(BiFunction<T, U, R> original) {
        Objects.requireNonNull(original);

        func = new MemoizedFunction<>(
                (dependencies) -> original.apply((T) dependencies.items[0], (U) dependencies.items[1]));
    }

    public R apply(T t, U u) {
        return func.apply(new Dependencies(new Object[]{t, u}));
    }

    public R apply(T t, U u, Object[] dependencies) {
        Objects.requireNonNull(dependencies);

        final var allDeps = new Object[dependencies.length + 2];
        allDeps[0] = t;
        allDeps[1] = u;
        System.arraycopy(dependencies, 0, allDeps, 2, dependencies.length);
        return func.apply(new Dependencies(allDeps));
    }
}

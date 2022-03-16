package memoization;

import java.util.Objects;
import java.util.function.BiFunction;

public class MemoizedBiRoutine<T, U, R> implements BiFunction<T, U, R> {
    private final MemoizedFunction<Arguments<T, U>, R> func;

    public MemoizedBiRoutine(BiFunction<T, U, R> original) {
        Objects.requireNonNull(original);

        func = new MemoizedFunction<>(
                arguments -> original.apply(arguments.t, arguments.u));
    }

    public R hardApply(T t, U u) {
        return func.hardApply(new Arguments<>(t, u, Dependencies.EMPTY));
    }

    public R apply(T t, U u) {
        return func.apply(new Arguments<>(t, u, Dependencies.EMPTY));
    }

    public R apply(T t, U u, Object[] dependencies) {
        Objects.requireNonNull(dependencies);

        return func.apply(new Arguments<>(t, u, new Dependencies(dependencies)));
    }

    private record Arguments<T, U>(T t, U u, Dependencies dependencies) {
    }
}

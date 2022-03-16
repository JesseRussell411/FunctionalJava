package memoization;

import java.util.Objects;
import java.util.function.Function;

public class MemoizedRoutine<T, R> implements Function<T, R> {
    private final MemoizedFunction<Arguments<T>, R> func;

    public MemoizedRoutine(Function<T, R> original) {
        Objects.requireNonNull(original);


        func = new MemoizedFunction<>(
                (arguments) -> original.apply(arguments.t));
    }

    public R hardApply(T t) {
        return func.hardApply(new Arguments<>(t, Dependencies.EMPTY));
    }

    public R apply(T t) {
        return func.apply(new Arguments<>(t, Dependencies.EMPTY));
    }

    public R apply(T t, Object[] dependencies) {
        Objects.requireNonNull(dependencies);

        return func.apply(new Arguments<>(t, new Dependencies(dependencies)));
    }

    private record Arguments<T>(T t, Dependencies dependencies) {
    }
}
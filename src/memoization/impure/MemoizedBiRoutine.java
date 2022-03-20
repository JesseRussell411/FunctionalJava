package memoization.impure;

import collections.ObjectTuple;
import functionPlus.TriFunction;
import memoization.pure.MemoizedFunction;

import java.util.Objects;
import java.util.function.BiFunction;

public class MemoizedBiRoutine<T, U, R> implements TriFunction<T, U, Object[], R> {
    private final MemoizedFunction<Arguments<T, U>, R> func;

    public MemoizedBiRoutine(BiFunction<T, U, R> original) {
        Objects.requireNonNull(original);

        func = new MemoizedFunction<>(
                arguments -> original.apply(arguments.t, arguments.u));
    }

    public R hardApply(T t, U u) {
        return func.hardApply(new Arguments<>(t, u, ObjectTuple.EMPTY));
    }

    public R apply(T t, U u, Object[] dependencies) {
        Objects.requireNonNull(dependencies);

        return func.apply(new Arguments<>(t, u, new ObjectTuple(dependencies)));
    }

    private record Arguments<T, U>(T t, U u, ObjectTuple objectTuple) {
    }
}

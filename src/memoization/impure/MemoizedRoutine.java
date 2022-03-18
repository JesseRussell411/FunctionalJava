package memoization.impure;

import collections.ObjectTuple;
import memoization.pure.MemoizedFunction;

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
        return func.hardApply(new Arguments<>(t, ObjectTuple.EMPTY));
    }

    public R apply(T t) {
        return func.apply(new Arguments<>(t, ObjectTuple.EMPTY));
    }

    public R apply(T t, Object[] dependencies) {
        Objects.requireNonNull(dependencies);

        return func.apply(new Arguments<>(t, new ObjectTuple(dependencies)));
    }

    private record Arguments<T>(T t, ObjectTuple objectTuple) {
    }
}
package memoization.impure;

import collections.ObjectTuple;
import memoization.pure.MemoizedFunction;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

public class MemoizedRoutine<T, R> implements BiFunction<T, ObjectTuple, R> {
    private final MemoizedFunction<Arguments<T>, R> func;

    public MemoizedRoutine(Function<T, R> original) {
        Objects.requireNonNull(original);


        func = new MemoizedFunction<>(
                (arguments) -> original.apply(arguments.t));
    }

    public R hardApply(T t) {
        return func.hardApply(new Arguments<>(t, null));
    }

    public R apply(T t, Object[] dependencies) {
        Objects.requireNonNull(dependencies);
        return func.apply(new Arguments<>(t, new ObjectTuple(dependencies)));
    }

    public R apply(T t, ObjectTuple dependencies) {
        Objects.requireNonNull(dependencies);
        return func.apply(new Arguments<>(t, dependencies));
    }

    private record Arguments<T>(T t, ObjectTuple objectTuple) {
    }
}
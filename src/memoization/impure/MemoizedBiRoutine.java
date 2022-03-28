package memoization.impure;

import collections.ListRecord;
import functionPlus.TriFunction;
import memoization.pure.MemoizedFunction;

import java.util.Objects;
import java.util.function.BiFunction;

public class MemoizedBiRoutine<T, U, R> implements TriFunction<T, U, ListRecord<?>, R> {
    private final MemoizedFunction<Arguments<T, U>, R> func;

    public MemoizedBiRoutine(BiFunction<T, U, R> original) {
        Objects.requireNonNull(original);

        func = new MemoizedFunction<>(
                arguments -> original.apply(arguments.t, arguments.u));
    }

    public R hardApply(T t, U u) {
        return func.hardApply(new Arguments<>(t, u, new ListRecord<>()));
    }

    public R apply(T t, U u, Object[] dependencies) {
        Objects.requireNonNull(dependencies);
        return func.apply(new Arguments<>(t, u, new ListRecord<>(dependencies)));
    }

    public R apply(T t, U u, ListRecord<?> dependencies) {
        Objects.requireNonNull(dependencies);
        return func.apply(new Arguments<>(t, u, dependencies));
    }

    private record Arguments<T, U>(T t, U u, ListRecord<?> objectTuple) {
    }
}

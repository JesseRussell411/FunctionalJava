package memoization.impure;

import collections.ObjectTuple;
import memoization.pure.MemoizedFunction;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public class MemoizedSupplier<T> implements Function<ObjectTuple, T> {
    private final MemoizedFunction<ObjectTuple, T> func;

    public MemoizedSupplier(Supplier<T> original) {
        func = new MemoizedFunction<>((dependencies) -> original.get());
    }

    public T apply(Iterable<?> dependencies) {
        Objects.requireNonNull(dependencies);
        return func.apply(new ObjectTuple(dependencies));
    }

    public T apply(Object[] dependencies) {
        Objects.requireNonNull(dependencies);
        return func.apply(new ObjectTuple(dependencies));
    }

    public T apply(ObjectTuple dependencies) {
        Objects.requireNonNull(dependencies);
        return func.apply(dependencies);
    }

    public T hardApply(Iterable<?> dependencies) {
        Objects.requireNonNull(dependencies);
        return func.hardApply(new ObjectTuple(dependencies));
    }

    public T hardApply(Object[] dependencies) {
        Objects.requireNonNull(dependencies);
        return func.hardApply(new ObjectTuple(dependencies));
    }

    public T hardApply(ObjectTuple dependencies) {
        Objects.requireNonNull(dependencies);
        return func.hardApply(dependencies);
    }

    public T hardGet() {
        return func.hardApply(ObjectTuple.EMPTY);
    }
}

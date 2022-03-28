package memoization.impure;

import collections.ListRecord;
import memoization.pure.MemoizedFunction;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public class MemoizedSupplier<T> implements Function<ListRecord<?>, T> {
    private final MemoizedFunction<ListRecord<?>, T> func;

    public MemoizedSupplier(Supplier<T> original) {
        func = new MemoizedFunction<>((dependencies) -> original.get());
    }

    public T apply(Iterable<?> dependencies) {
        Objects.requireNonNull(dependencies);
        return func.apply(new ListRecord<>(dependencies));
    }

    public T apply(Object[] dependencies) {
        Objects.requireNonNull(dependencies);
        return func.apply(new ListRecord<>(dependencies));
    }

    public T apply(ListRecord<?> dependencies) {
        Objects.requireNonNull(dependencies);
        return func.apply(dependencies);
    }

    public T hardApply(Iterable<?> dependencies) {
        Objects.requireNonNull(dependencies);
        return func.hardApply(new ListRecord<>(dependencies));
    }

    public T hardApply(Object[] dependencies) {
        Objects.requireNonNull(dependencies);
        return func.hardApply(new ListRecord<>(dependencies));
    }

    public T hardApply(ListRecord<?> dependencies) {
        Objects.requireNonNull(dependencies);
        return func.hardApply(dependencies);
    }

    public T hardGet() {
        return func.hardApply(new ListRecord<>());
    }
}

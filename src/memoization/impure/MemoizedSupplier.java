package memoization.impure;

import collections.ObjectTuple;
import memoization.pure.MemoizedFunction;

import java.util.function.Function;
import java.util.function.Supplier;

public class MemoizedSupplier<T> implements Function<Object[], T> {
    private final MemoizedFunction<ObjectTuple, T> func;

    public MemoizedSupplier(Supplier<T> original) {
        func = new MemoizedFunction<>((objectTuple) -> original.get());
    }

    public T apply(Object[] dependencies) {
        return func.apply(new ObjectTuple(dependencies));
    }

    public T hardGet() {
        return func.hardApply(ObjectTuple.EMPTY);
    }
}

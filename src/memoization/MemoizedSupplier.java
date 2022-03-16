package memoization;

import java.util.function.Function;
import java.util.function.Supplier;

public class MemoizedSupplier<T> implements Supplier<T>, Function<Object[], T> {
    private final MemoizedFunction<Dependencies, T> func;

    public MemoizedSupplier(Supplier<T> original) {
        func = new MemoizedFunction<>((dependencies) -> original.get());
    }

    public T apply(Object[] dependencies) {
        return func.apply(new Dependencies(dependencies));
    }

    public T get() {
        return func.apply(Dependencies.EMPTY);
    }

    public T hardGet() {
        return func.hardApply(Dependencies.EMPTY);
    }
}

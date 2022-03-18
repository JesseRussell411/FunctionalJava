package memoization.pure;

import java.util.function.BiFunction;

public class MemoizedBiFunction<T, U, R> implements BiFunction<T, U, R> {
    private final MemoizedFunction<Arguments<T, U>, R> function;

    public MemoizedBiFunction(BiFunction<T, U, R> original) {
        function = new MemoizedFunction<>((args) -> original.apply(args.t, args.u));
    }

    public R apply(T t, U u) {
        return function.apply(new Arguments<>(t, u));
    }

    public R hardApply(T t, U u) {
        return function.hardApply(new Arguments<>(t, u));
    }

    private record Arguments<T, U>(T t, U u) {
    }
}

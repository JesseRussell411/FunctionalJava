import memoization.MemoizedBiRoutine;
import memoization.MemoizedFunction;

import java.util.Scanner;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Main {
    public static int memoFib(int n) {
        return memoFib.apply(n);
    }

    public static final Function<Integer, Integer> memoFib = new MemoizedFunction<>((n) -> {
        if (n <= 0) return 0;
        if (n == 1) return 1;

        return Main.memoFib.apply(n - 1) + Main.memoFib.apply(n - 2);
    });

    public static int fib(int n) {
        if (n <= 0) return 0;
        if (n == 1) return 1;

        return fib(n - 1) + fib(n - 2);
    }

    record Fib_Fact(int fib, int fact) {
    }

    public static Fib_Fact memoFibFact(int n1, int n2) {
        return memoFibFact.apply(n1, n2);
    }

    public static final BiFunction<Integer, Integer, Fib_Fact> memoFibFact = new MemoizedBiRoutine<>((n1, n2) -> {
        if (n1 == 0 || n1 == 1) {
            if (n2 > 1) {
                return new Fib_Fact(
                        n1,
                        Main.memoFibFact.apply(0, n2 - 1).fact * n2);
            } else {
                return new Fib_Fact(
                        n1,
                        n2);
            }
        } else {
            if (n2 > 1) {
                final var half = Main.memoFibFact.apply(n1 - 1, n2 - 1);
                return new Fib_Fact(
                        half.fib + Main.memoFibFact.apply(n1 - 2, 0).fib,
                        half.fact * n2);
            } else {
                return new Fib_Fact(
                        Main.memoFibFact.apply(n1 - 1, 0).fib + Main.memoFibFact.apply(n1 - 2, 0).fib,
                        n2);
            }
        }
    });

    public static Fib_Fact fibFact(int n1, int n2) {
        if (n1 == 0 || n1 == 1) {
            if (n2 > 1) {
                return new Fib_Fact(
                        n1,
                        fibFact(0, n2 - 1).fact * n2);
            } else {
                return new Fib_Fact(
                        n1,
                        n2);
            }
        } else {
            if (n2 > 1) {
                final var half = fibFact(n1 - 1, n2 - 1);
                return new Fib_Fact(
                        half.fib + fibFact(n1 - 2, 0).fib,
                        half.fact * n2);
            } else {
                return new Fib_Fact(
                        fibFact(n1 - 1, 0).fib + fibFact(n1 - 2, 0).fib,
                        n2);
            }
        }
    }

    public static void main(String[] args) {
        try (final var input = new Scanner(System.in)) {
            while (true) {
                try {
                    System.out.print("Enter Number: ");
                    final var num = input.nextInt();

                    final var fibAndFact = memoFibFact(num, num);

                    System.out.println("Fibonacci number: " + fibAndFact.fib);
                    System.out.println("Factorial: " + fibAndFact.fact);
                    System.out.println();
                } catch (RuntimeException e) {
                    System.err.println(e);
                } catch (StackOverflowError e) {
                    System.err.println(e);
                }
            }
        }
    }
}

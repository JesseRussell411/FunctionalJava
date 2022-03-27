import collections.wrappers.ArrayAsList;
import collections.PersistentList;
import composition.Promise;
import memoization.pure.MemoizedBiFunction;
import memoization.pure.MemoizedFunction;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Main {
    static String iterString(Iterator<?> iter, String delim) {
        StringBuilder s = new StringBuilder();

        if (iter.hasNext()) s.append(iter.next());
        while (iter.hasNext()) {
            s.append(delim);
            s.append(iter.next());
        }

        return s.toString();
    }

    static String iterString(Iterable<?> iter, String delim) {
        return iterString(iter.iterator(), delim);
    }

    static String iterString(Iterator<?> iter) {
        return iterString(iter, ", ");
    }

    static String iterString(Iterable<?> iter) {
        return iterString(iter, ", ");
    }

    static void print() {
        System.out.println();
    }

    static void print(Object o) {
        System.out.println(o);
    }

    static void print(Iterable<?> iter) {
        if (iter == null) print("{  ~~  N U L L  ~~  }");
        print(iterString(iter));
    }

    static void print(Iterator<?> iter) {
        if (iter == null) print("{  ~~  N U L L  ~~  }");
        print(iterString(iter));
    }

    static void print(Object[] items) {
        if (items == null) print("{  ~~  N U L L  ~~  }");
        print(iterString(new ArrayAsList<>(items)));
    }

    static void print(Iterable<?> iter, Object delim) {
        if (iter == null) print("{  ~~  N U L L  ~~  }");
        print(iterString(iter, String.valueOf(delim)));
    }

    static void print(Iterator<?> iter, Object delim) {
        if (iter == null) print("{  ~~  N U L L  ~~  }");
        print(iterString(iter, String.valueOf(delim)));
    }

    static void print(Object[] items, Object delim) {
        if (items == null) print("{  ~~  N U L L  ~~  }");
        print(iterString(new ArrayAsList<>(items), String.valueOf(delim)));
    }

    public static int memoFib(int n) {
        return memoFib.apply(n);
    }

    public static final Function<Integer, Integer> memoFib = new MemoizedFunction<>((n) -> {
        if (n == null) return 42;// because douglas adams
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

    public static final BiFunction<Integer, Integer, Fib_Fact> memoFibFact = new MemoizedBiFunction<>((n1, n2) -> {
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

    static final Random rand = new Random();

    static Stream<Integer> randInts(int size, int length) {
        return Stream.generate(() -> rand.nextInt(size)).limit(length);
    }

    public static void main(String[] args) {
        var l = new PersistentList<String>(List.of(
                "apple",
                "banana",
                "stroke",
                "peanut",
                "tommy",
                "car",
                "bank",
                "chair",
                "relax",
                "broken",
                "knife",
                "clever",
                "stoke",
                "carmel"
        ));
        final var originalL = l;

        print(l);

        l = l.withInsertion(1, List.of("apple1", "apple2", "apple3", "apple4"));
        l = l.withInsertion(l.size(), List.of("this goes at the end", "this is the end"));
        l = l.withInsertion(0, List.of("this goes at the start"));
        print(l);

        l = l.withInsertion(0, Stream.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).map(String::valueOf));
        print(l, "");
        l = l.withReplacement(1, Stream.of("a", "b", "c"));
        print(l, "");
        l = l.withReplacement(0, Stream.of(">")).withReplacement(l.size() - 1, Stream.of("<"));
        print(l, "");
        print(l.withSwap(0, "[").withSwap(l.size() - 1, "]"), "");
        print(l.withAddition(0, "{").withAddition(l.size() + 1, "}"), "");
        print(originalL);
        print(l);
        print(l.without(3));
        print(l.without(3, 2));
        print(l.without(3, 3));
        print(l.without(3, 4));
        print();
        print(l);
        print(l.without(0));
        print(l.without(l.size() - 1));
        print(l.without(0, 2));
        print(l.without(l.size() - 2, 2));

        final var randIntList = new PersistentList<>(randInts(10000, 100));

        print(randIntList);
        print();
        print(randIntList.sorted(Comparator.comparingInt(a -> a)), "\n");

        final var randIntList1_000_000 = randInts(100_000_000, 100_000_000).toList();

        final var randIntPList1_000_000 = new PersistentList<>(randIntList1_000_000);
        print("done generating random nums");
        long start = 0;
        long stop = 0;

        final var comp = Comparator.<Integer>comparingInt(a -> a);

//        start = System.currentTimeMillis();
//        final var sortedints = randIntList1_000_000.stream().sorted(comp).toList();
//        stop = System.currentTimeMillis();
//        System.out.println("Stream sorting of built in list: " + (stop - start));
//
//        start = System.currentTimeMillis();
//        final var sortedSPints = new PersistentList<>(randIntPList1_000_000.stream().sorted(comp));
//        stop = System.currentTimeMillis();
//        System.out.println("Stream sorting of Pers list:" + (stop - start));
//
//        start = System.currentTimeMillis();
//        final var sortedPints = randIntPList1_000_000.sorted(comp);
//        stop = System.currentTimeMillis();
//        System.out.println("Stream sorting with crap:" + (stop - start));

        final int i = 500_000;


        start = System.currentTimeMillis();
        final var withAddition_normal_list = Stream.concat(Stream.concat(randIntList1_000_000.stream().limit(i), Stream.of(-2)), StreamSupport.stream(Spliterators.spliteratorUnknownSize(randIntList1_000_000.listIterator(i), 0), true)).toList();
        stop = System.currentTimeMillis();
        System.out.println("insertion into list (in place):" + (stop - start));

        start = System.currentTimeMillis();
        final var withAddition = randIntPList1_000_000.withAddition(i, -2);
        stop = System.currentTimeMillis();
        System.out.println("insertion into plist:" + (stop - start));

        print(withAddition_normal_list.get(i));
        print(withAddition.get(i));


        final var promiseChain = new Promise<>(
                settle1 -> settle1.resolve(new Promise<>(
                        settle2 -> settle2.resolve(new Promise<>(
                                settle3 -> settle3.resolve(new Promise<>(settle4 -> settle4.resolve("hello world!!!!")))))))
        );

        promiseChain.flatten().then(s -> {
            System.out.println(s);
            return null;
        });

        System.out.println("fib of null: " + memoFib.apply(null));
        System.out.println("fib of null: " + memoFib.apply(null));
        System.out.println("fib of null: " + memoFib.apply(null));


        try (final var input = new Scanner(System.in)) {
            while (true) {
                final var deferred = Promise.<Integer>deferred();

                deferred.promise().then(n -> {
                    System.out.println("You entered " + n);
                    return n;
                }).then((n) -> {
                    System.out.println("The fibonacci number at " + n + " is " + memoFibFact(n, n).fib);
                    return n;
                }).then((n) -> {
                    System.out.println("The factorial of " + n + " is " + memoFibFact(n, n).fact);
                    return n;
                });

                deferred.promise().then(n -> {
                    System.out.println("Again, the number you entered is " + n);
                    System.out.println("Now I'll throw an error with the code " + n);
                    throw new RuntimeException(String.valueOf(n));
                }).onError(error -> {
                    System.out.println("Caught the error " + error.getMessage());
                    return error.getMessage();
                }).then(message -> {
                    System.out.println("Again, that error's message was " + message);
                    System.out.println("Now, I'll NOT throw an error.");
                    return message;
                }).onError(e -> {
                    System.out.println("This text won't print");
                    return e;
                }).onCancel(reason -> {
                    System.out.println("The error handling code was canceled because " + reason.getMessage());
                    return reason;
                });

                System.out.print("Enter Number: ");
                deferred.settle().resolve(input.nextInt());
            }
        }
    }
}

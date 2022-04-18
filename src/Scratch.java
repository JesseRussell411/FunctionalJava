import collections.adapters.ArrayAsList;
import collections.persistent.PersistentList;
import collections.persistent.PersistentMap;
import collections.persistent.PersistentSet;
import collections.persistent.PersistentTreeSet;
import collections.records.MapRecord;
import collections.records.SetRecord;
import collections.reference.WeakConcurrentHashMap;
import concurrency.Promise;
import memoization.pure.function.MemoizedFunction;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


class MemoizedBiFunction<T, U, R> implements BiFunction<T, U, R> {
    private final MemoizedFunction<Arguments<T, U>, R> function;

    public MemoizedBiFunction(BiFunction<T, U, R> original) {
        function = new MemoizedFunction<>((args) -> original.apply(args.t, args.u));
    }

    public R apply(T t, U u) {
        return function.apply(new Arguments<>(t, u));
    }

    public R cacheApply(T t, U u) {
        return function.cacheApply(new Arguments<>(t, u));
    }

    public R hardApply(T t, U u) {
        return function.hardApply(new Arguments<>(t, u));
    }

    public boolean isCached(T t, U u) {
        return function.isCached(new Arguments<>(t, u));
    }

    private record Arguments<T, U>(T t, U u) {
    }
}

/**
 * int wrapper that returns a (B)ad (H)ash
 */
class Intbh {
    public final int value;

    public Intbh(int value) {
        this.value = value;
    }

    public Intbh() {
        this(0);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public int hashCode() {
        return value % 10;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Intbh other) {
            return value == other.value;
        } else return false;
    }
}

public class Scratch {
    static <T> ArrayList<T> repeat(ArrayList<T> list, int times) {
        if (times == 2) {
            final var resultSize = list.size() * 2;
            final var result = new ArrayList<T>(resultSize);
            for (int i = 0; i < resultSize; i++) {
                result.add(list.get(i % list.size()));
            }
            return result;
        }
        if (times == 1) return list;
        if (times == 0) return new ArrayList<>(0);

        final var quotient = times / 2;
        final var remainder = times % 2;
        final var resultSize = list.size() * times;
        final var result = new ArrayList<T>(resultSize);
        final var repeatedByQuotient = repeat(list, quotient);
        final var repeatedByRemainder = repeat(list, remainder);

        int i = 0;
        final var preRemainderSize = resultSize - repeatedByRemainder.size();
        if (repeatedByQuotient.size() != 0) {
            for (; i < preRemainderSize; i++) {
                result.add(repeatedByQuotient.get(i % repeatedByQuotient.size()));
            }
        }
        if (repeatedByRemainder.size() != 0) {
            for (; i < resultSize; i++) {
                result.add(repeatedByRemainder.get(i - preRemainderSize));
            }
        }

        return result;
    }

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
        else print(iterString(iter));
    }

    static void print(Iterator<?> iter) {
        if (iter == null) print("{  ~~  N U L L  ~~  }");
        else print(iterString(iter));
    }

    static void print(Object[] items) {
        if (items == null) print("{  ~~  N U L L  ~~  }");
        else print(iterString(new ArrayAsList<>(items)));
    }

    static void print(Iterable<?> iter, Object delim) {
        if (iter == null) print("{  ~~  N U L L  ~~  }");
        else print(iterString(iter, String.valueOf(delim)));
    }

    static void print(Iterator<?> iter, Object delim) {
        if (iter == null) print("{  ~~  N U L L  ~~  }");
        else print(iterString(iter, String.valueOf(delim)));
    }

    static void print(Object[] items, Object delim) {
        if (items == null) print("{  ~~  N U L L  ~~  }");
        else print(iterString(new ArrayAsList<>(items), String.valueOf(delim)));
    }


    static void print(Stream<?> items, Object delim) {
        if (items == null) print("{  ~~  N U L L  ~~  }");
        else print(iterString(items.iterator(), String.valueOf(delim)));
    }

    static void print(Stream<?> items) {
        print(items, ", ");
    }


    public static int memoFib(int n) {
        return memoFib.apply(n);
    }

    public static final Function<Integer, Integer> memoFib = new MemoizedFunction<>((n) -> {
        if (n == null) return 42;// because douglas adams
        if (n <= 0) return 0;
        if (n == 1) return 1;

        return Scratch.memoFib.apply(n - 1) + Scratch.memoFib.apply(n - 2);
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
                        Scratch.memoFibFact.apply(0, n2 - 1).fact * n2);
            } else {
                return new Fib_Fact(
                        n1,
                        n2);
            }
        } else {
            if (n2 > 1) {
                final var half = Scratch.memoFibFact.apply(n1 - 1, n2 - 1);
                return new Fib_Fact(
                        half.fib + Scratch.memoFibFact.apply(n1 - 2, 0).fib,
                        half.fact * n2);
            } else {
                return new Fib_Fact(
                        Scratch.memoFibFact.apply(n1 - 1, 0).fib + Scratch.memoFibFact.apply(n1 - 2, 0).fib,
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

        final var randIntList1_000_000 = randInts(1_000_000, 1_000_000).toList();

        final var randIntPList1_000_000 = new PersistentList<>(randIntList1_000_000);
        print("done generating random nums");
        long start = 0;
        long stop = 0;

        final var comp = Comparator.<Integer>comparingInt(a -> a);

        start = System.currentTimeMillis();
        final var sortedints = randIntList1_000_000.stream().sorted(comp).toList();
        stop = System.currentTimeMillis();
        System.out.println("Stream sorting of built in list: " + (stop - start));

        start = System.currentTimeMillis();
        final var sortedSPints = new PersistentList<>(randIntPList1_000_000.stream().sorted(comp));
        stop = System.currentTimeMillis();
        System.out.println("Stream sorting of Pers list:" + (stop - start));


        final int i = 5000;


        start = System.currentTimeMillis();
        final var withAddition_normal_list = Stream.concat(Stream.concat(randIntList1_000_000.stream().limit(i), Stream.of(-2)), StreamSupport.stream(Spliterators.spliteratorUnknownSize(randIntList1_000_000.listIterator(i), 0), true)).toList();
        stop = System.currentTimeMillis();
        System.out.println("insertion into list (out of place):" + (stop - start));

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

        final var ps = new PersistentSet<>().withMany(randInts(1000, 100).map(Intbh::new));

        print(ps, "\n");

        var pm = new PersistentMap<String, String>();
        pm = pm.with("nine", "9").with("ten", "10").with("one", "1");
        print("1:" + pm.size());
        pm = pm.with("five", "9");
        print("2:" + pm.size());
        final var wrongPM = pm;
        print(pm.get("five"));
        pm = pm.with("five", "5");
        print("3:" + pm.size());
        print(pm.get("five"));
        print(pm.get("nine"));


        print();
        print(wrongPM.get("five"));
        print(pm.without("five").get("five"));
        print(pm.without("five").containsKey("five"));
        print(pm.containsKey("five"));
        print(pm.size());
        print(pm.with("six", "6").size());
        print(pm.without("nine").size());
        print(pm.with("nine", "3^3").size());

        var pts = new PersistentTreeSet<Integer>();
        pts = pts.withMany(randInts(1000, 10));

        print(pts.stream().sorted().iterator(), "\n");

        final var somelist = PersistentList.of(1, 2, 3, 4, 5);
        print(somelist.repeated(4));
        print(somelist.repeated(-4));
        System.out.println(somelist.repeated(2).asString());
        start = System.currentTimeMillis();
        final var repeatedALot = somelist.repeated(429_496_729);
        stop = System.currentTimeMillis();
        print("repeating 429_496_729 times took:" + (stop - start) + "ms");
        print("size after repeating: " + repeatedALot.size());

        final var bigStructure = PersistentSet.of(
                        new PersistentMap<String, String>()
                                .with("id", "1")
                                .with("name", "george")
                                .with("address", "montuky")
                                .asRecord(),
                        new PersistentMap<String, String>()
                                .with("id", "2")
                                .with("name", "fred")
                                .with("address", "penvainia")
                                .asRecord())
                .asRecord();

        final var otherBigStructure = new PersistentSet<>()
                .with(new PersistentMap<String, String>()
                        .with("id", "2")
                        .with("address", "penvainia")
                        .with("name", "fred")
                        .asRecord())
                .with(new PersistentMap<String, String>()
                        .with("name", "george")
                        .with("id", "1")
                        .with("address", "montuky")
                        .asRecord())
                .asRecord();

        final var differentBigStructure = new SetRecord<>(otherBigStructure.values().with(
                new MapRecord<>(new PersistentMap<String, String>()
                        .with("id", "1.5")
                        .with("address", "extreme!")
                        .with("name", "crazy fred"))));

        final var differentDifferentBigStructure = differentBigStructure.values()
                .with(new PersistentMap<>()
                        .with("bob", PersistentSet.of("sally", "may").asRecord())
                        .with("john", PersistentSet.of("super", "man").asRecord())
                        .asRecord())
                .asRecord();


        System.out.println(bigStructure);
        System.out.println(otherBigStructure);
        System.out.println(differentBigStructure);
        System.out.println(differentDifferentBigStructure);

        print(Objects.equals(bigStructure, otherBigStructure));
        print(Objects.equals(bigStructure, differentBigStructure));


        final var plist123 = PersistentList.of(1, 2, 3).repeated(10_000_000 / 3);
        final var list123 = new ArrayList<Integer>(plist123.size());
        list123.addAll(plist123);

        long ptotal = 0;
        long total = 0;

        start = System.currentTimeMillis();
        for (final var n : list123) {
            total += n;
        }
        stop = System.currentTimeMillis();

        System.out.println("Iteration of normal list took: " + (stop - start) + " ms");

        start = System.currentTimeMillis();
        for (final var n : plist123) {
            ptotal += n;
        }
        stop = System.currentTimeMillis();
        System.out.println("Iteration of persis list took: " + (stop - start) + " ms");
        System.out.println(ptotal);
        System.out.println(total);

        final var whm = new WeakConcurrentHashMap<Integer, Integer>();
        whm.put(8, 3);
        System.out.println(whm.get(null));
        whm.put(null, 3);
        System.out.println(whm.get(null));
        System.out.println(whm.size());


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

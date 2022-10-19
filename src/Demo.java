import collections.persistent.PersistentList;

import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class Demo {
    public static void main(String[] args) {
        final var rand = new Random();
        final var l5 = PersistentList.of(1, 2, 3, 4, 5);
        final var l5t1000000000 = l5.repeat(Integer.MAX_VALUE / l5.size());
        final var l100 = l5t1000000000.get(0, 10);
        System.out.println(l100.asString(","));


        final var no2 = l5.withoutFirstOccurrence(3);

        final var noe = l5.concat(List.of(6, 7, 8, 9)).filter((n, i) -> n % 2 != 0);


        System.out.println(no2.asString(","));
        System.out.println(noe.asString(","));
        System.out.println(l5t1000000000.size());
        System.out.println(l5t1000000000.get(1000000, 10).asString(","));

        final var rl = PersistentList.generate((i) -> rand.nextInt(1000), 20_000);

        var startTime = System.currentTimeMillis();
        var sl = rl.sorted(Comparator.comparingInt(a -> a));
        var stopTime = System.currentTimeMillis();
        final var timeToNormalSort = stopTime - startTime;
        System.out.println(timeToNormalSort);

        startTime = System.currentTimeMillis();
        sl = rl.optosort(Comparator.comparingInt(a -> a));
        stopTime = System.currentTimeMillis();
        final var timeToOptoSort = stopTime - startTime;

        System.out.println(timeToOptoSort);
    }
}

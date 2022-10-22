import collections.persistent.PersistentList;
import collections.persistent.PersistentMap;

import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class Demo {
    public static void main(String[] args) {
        final var rand = new Random();

        final var randomList = PersistentList.generate(i -> rand.nextInt(i + 1), 100);
        System.out.println(randomList.asString());
        final var randomListBIG = randomList.repeat(Integer.MAX_VALUE / randomList.size());

        System.out.println(randomListBIG.get(randomListBIG.size() / 2 + 50, 100).sorted(Comparator.comparingInt(a -> a)).asString());

        var m = new PersistentMap<Integer, String>();
        m = m.with(1, "one");
        m = m.with(2, "two");

        System.out.println(m);

    }
}

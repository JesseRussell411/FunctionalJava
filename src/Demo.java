import collections.persistent.PersistentList;

import java.util.List;

public class Demo {
    public static void main(String[] args) {
        final var l5 = PersistentList.of(1, 2, 3, 4, 5);
        final var l5t1000000000 = l5.repeat(Integer.MAX_VALUE / l5.size() - 1000);
        final var l100 = l5t1000000000.get(1001, 10);
        System.out.println(l100.asString(","));


        final var no2 = l5.withoutFirstOccurrence(3);

        final var noe = l5.concat(List.of(6, 7, 8, 9)).filter((n, i) -> n % 2 != 0);


        System.out.println(no2.asString(","));
        System.out.println(noe.asString(","));
    }
}

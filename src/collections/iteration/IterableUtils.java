package collections.iteration;

import java.util.Comparator;
import java.util.Iterator;

public class IterableUtils {
    public static <T> boolean isSorted(Iterable<T> iterable, Comparator<T> comparator) {
        return isSorted(iterable.iterator(), comparator);
    }

    public static <T> boolean isSorted(Iterator<T> iterator, Comparator<T> comparator) {
        if (!iterator.hasNext()) return true;
        T prev = iterator.next();

        while (iterator.hasNext()) {
            if (comparator.compare(prev, iterator.next()) > 0) return false;
        }

        return true;
    }

    public static <T> Iterable<T> merge(Iterable<T> a, Iterable<T> b, Comparator<T> comparator) {
        return new MergeIterable<>(a, b, comparator);
    }

    public static <T> Iterator<T> merge(Iterator<T> a, Iterator<T> b, Comparator<T> comparator) {
        return new MergeIterator<>(a, b, comparator);
    }
}

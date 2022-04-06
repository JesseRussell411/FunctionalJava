package collections.iteration;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;

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

    public static <T1> T1[] toArray(Collection<?> collection, T1[] a) {
        Objects.requireNonNull(a);
        final T1[] result = (a.length >= collection.size())
                ? a
                : (T1[]) Array.newInstance(a.getClass().componentType(), collection.size());

        int index = 0;

        // copy list contents
        for (final var item : collection) {
            if (index < result.length) {
                ((Object[]) result)[index++] = item;
            } else break;
        }

        // pad with null
        while (index < result.length) {
            result[index++] = null;
        }

        return result;
    }
}

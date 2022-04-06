package collections;

import collections.iteration.MergeIterable;
import collections.iteration.MergeIterator;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;

public class CollectionUtils {
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

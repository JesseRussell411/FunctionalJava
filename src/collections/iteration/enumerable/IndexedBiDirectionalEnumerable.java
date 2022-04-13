package collections.iteration.enumerable;

import collections.iteration.adapters.ListEnumeratorIterator;
import collections.iteration.enumerator.IndexedBiDirectionalEnumerator;

import java.util.ListIterator;

public interface IndexedBiDirectionalEnumerable<T> extends Iterable<T> {
    IndexedBiDirectionalEnumerator<T> enumerator();

    @Override
    default ListIterator<T> iterator() {
        return new ListEnumeratorIterator<>(enumerator());
    }
}

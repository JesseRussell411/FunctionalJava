package collections.iteration.enumerable;

import collections.iteration.adapters.ListEnumeratorIterator;
import collections.iteration.enumerator.IndexedEnumerator;

import java.util.ListIterator;

public interface IndexedEnumerable<T> extends Enumerable<T> {
    @Override
    IndexedEnumerator<T> enumerator();
}

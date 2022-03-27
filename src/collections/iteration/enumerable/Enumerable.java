package collections.iteration.enumerable;

import collections.iteration.EnumeratorIterator;
import collections.iteration.enumerator.Enumerator;

import java.util.Iterator;

public interface Enumerable<T> extends Iterable<T> {
    Enumerator<T> enumerator();

    @Override
    default Iterator<T> iterator() {
        return new EnumeratorIterator<>(enumerator());
    }
}

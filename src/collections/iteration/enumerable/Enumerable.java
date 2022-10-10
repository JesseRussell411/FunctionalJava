package collections.iteration.enumerable;

import collections.iteration.adapters.EnumeratorIterator;
import collections.iteration.enumerator.Enumerator;

import java.util.Iterator;

/**
 * More like C#'s IEnumerator interface which can be easier to write than Iterator.
 * @param <T>
 */
public interface Enumerable<T> extends Iterable<T> {
    Enumerator<T> enumerator();

    @Override
    default Iterator<T> iterator() {
        return new EnumeratorIterator<>(enumerator());
    }
}

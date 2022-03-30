package collections.iteration;

import collections.iteration.enumerator.BiDirectionalEnumerator;
import collections.iteration.enumerator.Enumerator;

import java.util.Iterator;
import java.util.Objects;

public class ReversedEnumeratorIterator<T> implements Iterator<T> {
    private final BiDirectionalEnumerator<T> enumerator;
    private Boolean hasNext = null;

    public ReversedEnumeratorIterator(BiDirectionalEnumerator<T> enumerator) {
        this.enumerator = Objects.requireNonNull(enumerator);
    }

    public boolean hasNext() {
        if (hasNext == null) {
            hasNext = enumerator.movePrevious();
        }
        return hasNext;
    }

    public T next() {
        if (hasNext()) {
            hasNext = null;
            return enumerator.current();
        } else return null;
    }
}

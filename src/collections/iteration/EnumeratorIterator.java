package collections.iteration;

import collections.iteration.enumerator.Enumerator;

import java.util.Iterator;
import java.util.Objects;

public class EnumeratorIterator<T> implements Iterator<T> {
    private final Enumerator<T> enumerator;
    private Boolean hasNext = null;

    public EnumeratorIterator(Enumerator<T> enumerator) {
        this.enumerator = Objects.requireNonNull(enumerator);
    }

    public boolean hasNext() {
        if (hasNext == null) {
            hasNext = enumerator.moveNext();
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

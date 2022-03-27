package collections.iteration.enumerator;

import java.util.Iterator;
import java.util.Objects;

public class IteratorEnumerator<T> implements Enumerator<T> {
    private final Iterator<T> iterator;
    private T current = null;

    public IteratorEnumerator(Iterator<T> iterator) {
        this.iterator = Objects.requireNonNull(iterator);
    }

    @Override
    public boolean moveNext() {
        if (iterator.hasNext()) {
            current = iterator.next();
            return true;
        } else {
            current = null;
            return false;
        }
    }

    @Override
    public T current() {
        return current;
    }
}

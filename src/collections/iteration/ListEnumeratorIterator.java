package collections.iteration;

import collections.iteration.enumerator.IndexedBiDirectionalEnumerator;

import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public class ListEnumeratorIterator<T> implements ListIterator<T> {
    private final IndexedBiDirectionalEnumerator<T> enumerator;
    private Boolean hasPrevious = null;
    private Boolean hasNext = null;

    public ListEnumeratorIterator(IndexedBiDirectionalEnumerator<T> enumerator) {
        this.enumerator = Objects.requireNonNull(enumerator);
    }

    @Override
    public boolean hasNext() {
        if (hasPrevious != null) {
            enumerator.moveNext();
            hasPrevious = null;
        }
        if (hasNext == null) {
            hasNext = enumerator.moveNext();
        }
        return hasNext;
    }

    @Override
    public boolean hasPrevious() {
        if (hasNext != null) {
            enumerator.movePrevious();
            hasNext = null;
        }
        if (hasPrevious == null) {
            hasPrevious = enumerator.movePrevious();
        }
        return hasPrevious;
    }

    @Override
    public T next() {
        if (hasNext()) {
            hasNext = null;
            return enumerator.current();
        } else throw new NoSuchElementException();
    }

    @Override
    public T previous() {
        if (hasPrevious()) {
            hasPrevious = null;
            return enumerator.current();
        } else throw new NoSuchElementException();
    }

    @Override
    public int nextIndex() {
        if (hasNext()) {
            hasNext = null;
        }
        return enumerator.currentIndex();
    }

    @Override
    public int previousIndex() {
        if (hasPrevious()) {
            hasPrevious = null;
        }
        return enumerator.currentIndex();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void set(T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(T t) {
        throw new UnsupportedOperationException();
    }
}

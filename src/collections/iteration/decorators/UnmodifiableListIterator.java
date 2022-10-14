package collections.iteration.decorators;

import annotations.UnsupportedOperation;

import java.util.ListIterator;

public class UnmodifiableListIterator<T> implements ListIterator<T> {
    private final ListIterator<T> base;

    public UnmodifiableListIterator(ListIterator<T> base) {
        this.base = base;
    }

    @Override
    public boolean hasNext() {
        return base.hasNext();
    }

    @Override
    public T next() {
        return base.next();
    }

    @Override
    public boolean hasPrevious() {
        return base.hasPrevious();
    }

    @Override
    public T previous() {
        return base.previous();
    }

    @Override
    public int nextIndex() {
        return base.nextIndex();
    }

    @Override
    public int previousIndex() {
        return base.previousIndex();
    }

    @Override
    @UnsupportedOperation
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    @UnsupportedOperation
    public void set(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    @UnsupportedOperation
    public void add(Object o) {
        throw new UnsupportedOperationException();
    }
}

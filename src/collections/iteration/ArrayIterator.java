package collections.iteration;

import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public class ArrayIterator<T> implements ListIterator<T> {
    private final T[] array;
    private int index;

    public ArrayIterator(T[] array) {
        this.array = Objects.requireNonNull(array);
        index = -1;
    }


    @Override
    public boolean hasNext() {
        return index < array.length - 1;
    }

    @Override
    public T next() {
        if (hasNext()) {
            return array[++index];
        } else throw new NoSuchElementException();
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public T previous() {
        if (hasPrevious()) {
            return array[--index];
        } else throw new NoSuchElementException();
    }

    @Override
    public int nextIndex() {
        return 0;
    }

    @Override
    public int previousIndex() {
        return 0;
    }

    @Override
    public void remove() {

    }

    @Override
    public void set(T t) {

    }

    @Override
    public void add(T t) {

    }
}

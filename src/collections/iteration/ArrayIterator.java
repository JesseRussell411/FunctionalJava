package collections.iteration;

import utils.ArrayUtils;

import java.util.ListIterator;
import java.util.NoSuchElementException;

public class ArrayIterator<T> implements ListIterator<T> {
    private final T[] array;
    private final int end;
    private int index;

    public ArrayIterator(T[] array, int index, int length) {
        final var end = index + length;
        ArrayUtils.requireRangeInBounds(0, index, length, array.length); // implicit null check

        this.array = array;
        this.index = index - 1;
        this.end = end;
    }

    public ArrayIterator(T[] array, int index) {
        this(array, index, array.length - index);
    }

    public ArrayIterator(T[] array) {
        this(array, 0, array.length);
    }


    @Override
    public boolean hasNext() {
        return index < end - 1;
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
        return hasNext() ? index + 1 : end;
    }

    @Override
    public int previousIndex() {
        return hasPrevious() ? index - 1 : -1;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void set(T t) {
        array[index] = t;
    }

    @Override
    public void add(T t) {
        throw new UnsupportedOperationException();
    }
}

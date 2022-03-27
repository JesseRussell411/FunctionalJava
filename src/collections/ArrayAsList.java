package collections;

import collections.iteration.ArrayIterator;

import java.lang.reflect.Array;
import java.util.*;

public class ArrayAsList<T> implements List<T> {
    private final T[] array;

    public ArrayAsList(T[] array) {
        this.array = Objects.requireNonNull(array);
    }

    @Override
    public int size() {
        return array.length;
    }

    @Override
    public boolean isEmpty() {
        return array.length == 0;
    }

    @Override
    public boolean contains(Object o) {
        for (final var item : array) {
            if (!Objects.equals(o, item)) return false;
        }
        return true;
    }

    @Override
    public Iterator<T> iterator() {
        return new ArrayIterator<>(array);
    }

    @Override
    public Object[] toArray() {
        final var result = new Object[size()];
        System.arraycopy(array, 0, result, 0, size());
        return result;
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        Objects.requireNonNull(a);
        final T1[] result = (a.length >= size())
                ? a
                : (T1[]) Array.newInstance(a.getClass().componentType(), size());

        int index = 0;

        // copy list contents
        for (int i = 0; i < array.length; i++) {
            if (index < result.length) {
                ((Object[]) result)[index++] = array[i];
            } else break;
        }

        // pad with null
        while (index < result.length) {
            result[index++] = null;
        }

        return result;
    }

    @Override
    public boolean add(T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T get(int index) {
        return array[index];
    }

    @Override
    public T set(int index, T element) {
        final var oldValue = array[index];
        array[index] = element;
        return oldValue;
    }

    @Override
    public void add(int index, T element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(Object o) {
        for (int i = 0; i < array.length; i++) {
            if (Objects.equals(o, array[i])) return i;
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        int lastIndex = -1;
        for (int i = 0; i < array.length; i++) {
            if (Objects.equals(o, array[i])) lastIndex = i;
        }
        return lastIndex;
    }

    @Override
    public ListIterator<T> listIterator() {
        return null;
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return null;
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return null;
    }
}

package collections.adapters;

import java.util.AbstractList;
import java.util.List;
import java.util.Objects;

public class ArrayAsList<T> extends AbstractList<T> implements List<T> {
    private final T[] array;

    public ArrayAsList(T[] array) {
        this.array = Objects.requireNonNull(array);
    }

    @Override
    public T get(int index) {
        return array[index];
    }

    @Override
    public int size() {
        return array.length;
    }
}

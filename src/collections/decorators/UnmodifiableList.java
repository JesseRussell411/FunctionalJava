package collections.decorators;

import annotations.UnsupportedOperation;
import collections.iteration.decorators.UnmodifiableIterator;
import collections.iteration.decorators.UnmodifiableListIterator;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class UnmodifiableList<T> implements List<T> {
    private final List<T> base;

    public UnmodifiableList(List<T> base) {
        this.base = base;
    }

    @Override
    public int size() {
        return base.size();
    }

    @Override
    public boolean isEmpty() {
        return base.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return return base.contains(o);
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return new UnmodifiableIterator<>(base.iterator());
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return base.toArray();
    }

    @NotNull
    @Override
    public <T1> T1[] toArray(@NotNull T1[] a) {
        return base.toArray(a);
    }

    @Override
    @UnsupportedOperation
    public boolean add(T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    @UnsupportedOperation
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return base.containsAll(c);
    }

    @Override
    @UnsupportedOperation
    public boolean addAll(@NotNull Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    @UnsupportedOperation
    public boolean addAll(int index, @NotNull Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    @UnsupportedOperation
    public boolean removeAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    @UnsupportedOperation
    public boolean retainAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    @UnsupportedOperation
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T get(int index) {
        return base.get(index);
    }

    @Override
    @UnsupportedOperation
    public T set(int index, T element) {
        throw new UnsupportedOperationException();
    }

    @Override
    @UnsupportedOperation
    public void add(int index, T element) {
        throw new UnsupportedOperationException();
    }

    @Override
    @UnsupportedOperation
    public T remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(Object o) {
        return base.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return base.lastIndexOf(o);
    }

    @NotNull
    @Override
    public ListIterator<T> listIterator() {
        return new UnmodifiableListIterator<>(base.listIterator());
    }

    @NotNull
    @Override
    public ListIterator<T> listIterator(int index) {
        return new UnmodifiableListIterator<>(base.listIterator(index));
    }

    @NotNull
    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return new UnmodifiableList<>(base.subList(fromIndex, toIndex));
    }
}

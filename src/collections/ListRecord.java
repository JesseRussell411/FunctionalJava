package collections;

import memoization.pure.Lazy;

import java.util.*;
import java.util.stream.Stream;

public class ListRecord<T> implements List<T> {
    private final PersistentList<T> items;

    public ListRecord() {
        this(new PersistentList<>());
    }

    public ListRecord(Stream<T> itemStream) {
        this(new PersistentList<>(itemStream));
    }

    public ListRecord(Iterator<T> itemIterator) {
        this(new PersistentList<>(itemIterator));
    }

    public ListRecord(Iterable<T> items) {
        this(new PersistentList<>(items));
    }

    public ListRecord(T[] items) {
        this(new PersistentList<>(items));
    }

    public ListRecord(PersistentList<T> items) {
        this.items = Objects.requireNonNull(items);
    }

    public PersistentList<T> items() {
        return items;
    }


    // important stuff
    private final Lazy<Integer> lazyHash = new Lazy<>(() -> {
        int hash = 1;
        for (final var item : items()) {
            hash = Objects.hash(hash, item);
        }
        return hash;
    });

    @Override
    public int hashCode() {
        return lazyHash.get();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ListRecord<?> other)) return false;

        if (items == other.items) return true;
        if (size() == 0 && other.size() == 0) return true;
        if (size() != other.size()) return false;
        if (hashCode() != other.hashCode()) return false;

        final var iter = iterator();
        final var otherIter = other.iterator();
        while (iter.hasNext() && otherIter.hasNext()) {
            if (!Objects.equals(iter.next(), otherIter.next())) return false;
        }
        return true;
    }

    @Override
    public int size() {
        return items().size();
    }

    @Override
    public boolean isEmpty() {
        return items().isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return items().contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return items().iterator();
    }

    @Override
    public Object[] toArray() {
        return items().toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return items().toArray(a);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return items().containsAll(c);
    }


    @Override
    public int indexOf(Object o) {
        return items().indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return 0;
    }

    @Override
    public ListIterator<T> listIterator() {
        return items().listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return items().listIterator(index);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return items().subList(fromIndex, toIndex);
    }

    // =========== unsupported interface operations ==================
    @Override
    public boolean add(T t) {
        return items().add(t);
    }

    @Override
    public boolean remove(Object o) {
        return items().remove(o);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return items().addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        return items().addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return items().removeAll(c);
    }


    @Override
    public void add(int index, T element) {
        items().add(index, element);
    }

    @Override
    public T remove(int index) {
        return items().remove(index);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return items().retainAll(c);
    }

    @Override
    public void clear() {
        items().clear();
    }

    @Override
    public T get(int index) {
        return items.get(index);
    }

    @Override
    public T set(int index, T element) {
        return items().set(index, element);
    }
}

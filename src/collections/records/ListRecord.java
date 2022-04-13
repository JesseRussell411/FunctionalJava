package collections.records;

import collections.persistent.PersistentList;
import memoization.pure.lazy.Lazy;
import memoization.pure.lazy.SoftLazy;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

// TODO add no-caching flag

public class ListRecord<T> implements Iterable<T>, java.io.Serializable {
    private final PersistentList<T> list;

    public ListRecord() {
        this(new PersistentList<>());
    }

    public ListRecord(Stream<T> itemStream) {
        this(new PersistentList<>(itemStream));
    }

    public ListRecord(Iterator<T> itemIterator) {
        this(new PersistentList<>(itemIterator));
    }

    public ListRecord(Iterable<T> list) {
        this(new PersistentList<>(list));
    }

    public ListRecord(T[] list) {
        this(new PersistentList<>(list));
    }

    public ListRecord(PersistentList<T> list) {
        this.list = Objects.requireNonNull(list);
    }

    public PersistentList<T> items() {
        return list;
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
        // TODO equality caching
        if (this == obj) return true;
        if (!(obj instanceof ListRecord<?> other)) return false;

        if (list == other.list) return true;
        if (isEmpty() && other.isEmpty()) return true;
        if (size() != other.size()) return false;

        if (hashCode() != other.hashCode()) return false;

        final var iter = iterator();
        final var otherIter = other.iterator();
        while (iter.hasNext() && otherIter.hasNext()) {
            if (!Objects.equals(iter.next(), otherIter.next())) return false;
        }
        return true;
    }

    public int size() {
        return items().size();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public Iterator<T> iterator() {
        return items().iterator();
    }

    public Stream<T> stream() {
        return list.stream();
    }

    private final Supplier<String> lazyToString = new SoftLazy<>(() -> {
        final var builder = new StringBuilder();
        final var iter = iterator();

        builder.append("[ ");
        if (iter.hasNext()) builder.append(iter.next());
        while (iter.hasNext()) {
            builder.append(", ").append(iter.next());
        }
        builder.append(" ]");

        return builder.toString();
    });

    @Override
    public String toString() {
        return lazyToString.get();
    }
}

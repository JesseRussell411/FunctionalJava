package collections.records;

import collections.PersistentList;
import memoization.pure.Lazy;
import memoization.pure.WeakLazy;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ListRecord<T> implements Iterable<T>, java.io.Serializable {
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
        return items.stream();
    }

    private final Supplier<String> lazyToString = new WeakLazy<>(() -> {
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

package collections;

import concurrency.Concurrency;
import memoization.pure.Lazy;

import java.util.*;
import java.util.stream.Stream;

public class ListRecord<T> implements Iterable<T> {
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

        if (lazyHash.isCached() && hashCode() != other.hashCode()) {
            return false;
        } else {
            final var hashCodePromise = Concurrency.threadedCall(lazyHash);
            final var otherHash = other.hashCode();
            try {
                final var thisHash = hashCodePromise.join();
                if (!Objects.equals(thisHash, otherHash)) return false;
            } catch (Throwable e) {
            }
        }


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
}

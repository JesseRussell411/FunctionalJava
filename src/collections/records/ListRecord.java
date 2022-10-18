package collections.records;

import collections.persistent.PersistentList;
import collections.reference.WeakIdentityConcurrentHashMap;
import memoization.pure.supplier.MemoizedSupplier;
import memoization.pure.supplier.SoftMemoizedSupplier;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

// TODO add no-caching flag

/**
 * Immutable list data structure that assumes its contents are also immutable.
 * This assumption allows it to cache operations like toString or equality checking.
 * @param <T>
 */
public class ListRecord<T> implements Iterable<T>, java.io.Serializable {
    private final Map<ListRecord<?>, Boolean> equalityCache = new WeakIdentityConcurrentHashMap<>();
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
    private final MemoizedSupplier<Integer> lazyHash = new MemoizedSupplier<>(() -> {
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
        // check...
        // instance
        if (this == obj) return true;

        // type
        if (!(obj instanceof ListRecord<?> other)) return false;

        // internals
        if (list == other.list) return true;

        // size
        if (isEmpty() && other.isEmpty()) return true;
        if (size() != other.size()) return false;

        // cache
        final var fromCache = equalityCache.get(other);
        if (fromCache != null) return fromCache;
        else {
            final var fromOtherCache = other.equalityCache.get(this);
            if (fromOtherCache != null) return fromOtherCache;
        }

        // hash
        if (hashCode() != other.hashCode()) return false;

        // full contents
        final var iter = iterator();
        final var otherIter = other.iterator();
        while (iter.hasNext() && otherIter.hasNext()) {
            if (!Objects.equals(iter.next(), otherIter.next())) {
                equalityCache.put(other, false);
                return false;
            }
        }
        // equals = true

        equalityCache.put(other, true);
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

    private final Supplier<String> toString = new SoftMemoizedSupplier<>(() -> {
        final var builder = new StringBuilder();
        final var iter = iterator();

        builder.append("[ ");
        if (iter.hasNext()) builder.append(iter.next());
        while (iter.hasNext()) builder.append(", ").append(iter.next());
        builder.append(" ]");

        return builder.toString();
    });

    @Override
    public String toString() {
        return toString.get();
    }
}

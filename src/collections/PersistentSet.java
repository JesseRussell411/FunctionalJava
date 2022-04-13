package collections;

import collections.iteration.ArrayIterator;
import collections.iteration.EnumeratorIterator;
import collections.iteration.enumerable.Enumerable;
import collections.iteration.enumerator.BiDirectionalEnumerator;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class PersistentSet<T> implements Enumerable<T>, Set<T>, java.io.Serializable {
    private final PersistentTreeSet<Entry<T>> entries;
    private final int size;

    @SafeVarargs
    public static <T> PersistentSet<T> of(T... items) {
        return new PersistentSet<T>().withMany(items);
    }

    private PersistentSet(PersistentTreeSet<Entry<T>> entries, int size) {
        this.entries = entries;
        this.size = size;
        assert entries != null;
        assert Assertions.correctSize(this);
    }

    public PersistentSet() {
        this(new PersistentTreeSet<>(), 0);
    }

    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public Object[] toArray() {
        return stream().toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return CollectionUtils.toArray(this, a);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (final var value : c) {
            if (!contains(value)) return false;
        }
        return true;
    }

    @Override
    public boolean contains(Object o) {
        final var entry = getEntry(Objects.hashCode(o));
        return entry != null && entry.values.contains(o);
    }

    @Override
    public BiDirectionalEnumerator<T> enumerator() {
        return enumerator(false);
    }

    @Override
    public Spliterator<T> spliterator() {
        return Spliterators.spliterator(iterator(), size, Spliterator.IMMUTABLE | Spliterator.DISTINCT);
    }

    @Override
    public Iterator<T> iterator() {
        return new EnumeratorIterator<>(enumerator());
    }

    public Stream<T> stream(boolean parallel) {
        return StreamSupport.stream(this.spliterator(), parallel);
    }

    public Stream<T> stream() {
        return stream(true);
    }

    public BiDirectionalEnumerator<T> enumerator(boolean startAtEnd) {
        return new SelfEnumerator<>(entries, startAtEnd);
    }

    private Entry<T> getEntry(int hash) {
        return entries.get(new Entry<>(hash, null));
    }

    public T get(T value) {
        final var entry = getEntry(Objects.hashCode(value));
        if (entry != null) {
            return entry.values.getFirstOccurrence(value);
        } else return null;
    }

    public PersistentSet<T> with(T value) {
        final var hash = Objects.hashCode(value);
        final var existingEntry = getEntry(hash);

        if (existingEntry != null) {
            final var newEntry = existingEntry.with(value);
            final var newSize = size + (newEntry.values.size() - existingEntry.values.size());

            return new PersistentSet<>(entries.with(newEntry), newSize);
        } else {
            return new PersistentSet<>(
                    entries.with(new Entry<>(hash, PersistentList.of(value))),
                    size + 1);
        }
    }

    public PersistentSet<T> withMany(Iterator<T> valueIterator) {
        var result = this;
        while (valueIterator.hasNext()) {
            result = result.with(valueIterator.next());
        }
        return result;
    }

    public PersistentSet<T> withMany(Stream<T> valueStream) {
        return withMany(valueStream.iterator());
    }

    public PersistentSet<T> withMany(Iterable<T> values) {
        return withMany(values.iterator());
    }

    public PersistentSet<T> withMany(T[] values) {
        return withMany(new ArrayIterator<>(values));
    }

    public PersistentSet<T> without(T value) {
        final var hash = Objects.hashCode(value);
        final var existingEntry = getEntry(hash);

        if (existingEntry != null) {
            final var newEntry = existingEntry.without(value);
            final var newSize = size + (newEntry.values.size() - existingEntry.values.size());

            if (newEntry.values.isEmpty()) {
                return new PersistentSet<>(entries.without(existingEntry), newSize);
            } else {
                return new PersistentSet<>(entries.with(newEntry), newSize);
            }

        } else return this;
    }

    public PersistentSet<T> withoutMany(Iterator<T> valueIterator) {
        var result = this;
        while (valueIterator.hasNext()) {
            result = result.without(valueIterator.next());
        }

        return result;
    }

    public PersistentSet<T> withoutMany(Stream<T> valueStream) {
        return withoutMany(valueStream.iterator());
    }

    public PersistentSet<T> withoutMany(Iterable<T> values) {
        return withoutMany(values.iterator());
    }

    public PersistentSet<T> withoutMany(T[] values) {
        return withoutMany(new ArrayIterator<>(values));
    }

    private static class Entry<T> implements Comparable<Entry<T>>, java.io.Serializable {
        final int key;
        final PersistentList<T> values;

        public Entry(int key, PersistentList<T> values) {
            this.key = key;
            this.values = values;
            assert this.values != null;
        }

        Entry<T> with(T value) {
            return new Entry<>(key, values.replaceOrPut(value));
        }

        Entry<T> without(T value) {
            return new Entry<>(key, values.withoutFirstOccurrence(value));
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof PersistentSet.Entry<?> other) {
                return Objects.equals(key, other.key);
            } else return false;
        }

        @Override
        public int compareTo(Entry<T> o) {
            return Integer.compare(
                    Objects.hashCode(key),
                    Objects.hashCode(o.key));
        }
    }

    public static class SelfEnumerator<T> implements BiDirectionalEnumerator<T> {
        private final BiDirectionalEnumerator<Entry<T>> entries;
        private BiDirectionalEnumerator<T> localEnumerator = null;

        private SelfEnumerator(PersistentTreeSet<Entry<T>> entries, boolean startAtEnd) {
            this.entries = entries.enumerator(startAtEnd);
        }

        private boolean advanceNextNode() {
            do {
                if (entries.moveNext()) {
                    localEnumerator = entries.current().values.enumerator();
                } else return false;
            } while (!localEnumerator.moveNext());
            return true;
        }

        private boolean advancePreviousNode() {
            do {
                if (entries.movePrevious()) {
                    localEnumerator = entries.current().values.enumerator(true);
                } else return false;
            } while (!localEnumerator.movePrevious());
            return true;
        }

        @Override
        public boolean moveNext() {
            if (localEnumerator == null || !localEnumerator.moveNext()) return advanceNextNode();
            return true;
        }

        @Override
        public boolean movePrevious() {
            if (localEnumerator == null || !localEnumerator.movePrevious()) return advancePreviousNode();
            return true;
        }

        @Override
        public T current() {
            if (localEnumerator == null) throw new NoSuchElementException();
            return localEnumerator.current();
        }
    }

    static class Assertions {
        static <T> int actualSize(PersistentTreeSet<Entry<T>> entries) {
            int totalSize = 0;
            for (final var entry : entries) {
                totalSize += entry.values.size();
            }
            return totalSize;
        }

        static <T> boolean correctSize(PersistentSet<T> set) {
            return set.size() == actualSize(set.entries);
        }
    }

    // unsupported interface methods
    @Override
    public boolean add(T t) {
        return false;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    @Override
    public void clear() {

    }
}
package collections.decorators;

import annotations.UnsupportedOperation;
import collections.iteration.decorators.UnmodifiableIterator;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class UnmodifiableSet<T> implements Set<T> {
    @NotNull
    private final Set<T> base;

    public UnmodifiableSet(@NotNull Set<T> base) {
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
        return base.contains(o);
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return new UnmodifiableIterator<>(base.iterator());
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        base.forEach(action);
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
    public <T1> T1[] toArray(IntFunction<T1[]> generator) {
        return base.toArray(generator);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return base.containsAll(c);
    }

    @Override
    public Spliterator<T> spliterator() {
        return base.spliterator();
    }

    @Override
    public Stream<T> stream() {
        return base.stream();
    }

    @Override
    public Stream<T> parallelStream() {
        return base.parallelStream();
    }

    @UnsupportedOperation
    @Override
    public boolean add(T t) {
        throw new UnsupportedOperationException();
    }

    @UnsupportedOperation
    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @UnsupportedOperation
    @Override
    public boolean addAll(@NotNull Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @UnsupportedOperation
    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @UnsupportedOperation
    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @UnsupportedOperation
    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        throw new UnsupportedOperationException();
    }

    @UnsupportedOperation
    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }
}

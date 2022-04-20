package collections.decorators;

import collections.iteration.decorators.UnmodifiableIterator;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Stream;

public class UnmodifiableCollection<T> extends AbstractCollection<T> {
    @NotNull
    private final Collection<T> base;

    public UnmodifiableCollection(@NotNull Collection<T> base) {
        this.base = base;
    }

    @Override
    public int size() {
        return base.size();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
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

    public Stream<T> stream(boolean parallel) {
        return parallel ? base.stream() : base.parallelStream();
    }

    @Override
    public Stream<T> stream() {
        return stream(false);
    }

    @Override
    public Stream<T> parallelStream() {
        return stream(true);
    }
}

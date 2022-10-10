package collections.decorators;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;

public class AppendedCollection<T> extends AbstractCollection<T> {
    @NotNull
    private final Collection<T> base;
    private final T addition;

    public AppendedCollection(@NotNull Collection<T> base, T addition) {
        this.base = base;
        this.addition = addition;
    }

    @Override
    public int size() {
        return base.size() + 1;
    }

    @Override
    public boolean isEmpty() {
        // will never be empty because it contains at least the addition.
        return false;
    }

    @Override
    public boolean contains(Object o) {
        if (Objects.equals(addition, o)) {
            return true;
        } else {
            return base.contains(o);
        }
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return stream().iterator();
    }

    public Stream<T> stream(boolean parallel) {
        final var baseStream = parallel ? base.parallelStream() : base.stream();
        return Stream.concat(baseStream, Stream.of(addition));
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

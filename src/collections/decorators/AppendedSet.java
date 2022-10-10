package collections.decorators;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

public class AppendedSet<T> extends AbstractSet<T> {
    @NotNull
    private final Set<T> base;
    private final T extension;

    public AppendedSet(@NotNull Set<T> base, T extension) {
        this.base = Objects.requireNonNull(base);
        this.extension = extension;
    }

    @Override
    public Iterator<T> iterator() {
        return stream().iterator();
    }

    @Override
    public int size() {
        if (base.contains(extension)) {
            return base.size();
        } else {
            return base.size() + 1;
        }
    }

    @Override
    public boolean isEmpty() {
        // this set cannot be empty because it will always contain at least the additional value;
        return false;
    }

    @Override
    public boolean contains(Object obj) {
        if (Objects.equals(obj, extension)) {
            return true;
        } else {
            return base.contains(obj);
        }
    }

    public Stream<T> stream(boolean parallel) {
        final var baseStream = parallel ? base.parallelStream() : base.stream();
        return Stream.concat(baseStream, Stream.of(extension));
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

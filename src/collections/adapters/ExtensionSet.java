package collections.adapters;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

public class ExtensionSet<T> extends AbstractSet<T> {
    @NotNull
    private final Set<T> base;
    private final Set<T> extension;

    public ExtensionSet(@NotNull Set<T> base, Set<T> extension) {
        this.base = Objects.requireNonNull(base);
        this.extension = extension;
    }

    @Override
    public Iterator<T> iterator() {
        return stream().iterator();
    }

    @Override
    public int size() {
        if (extension == null) {
            return base.size();
        } else {
            return base.size() + extension.size();
        }
    }

    @Override
    public boolean contains(Object obj) {
        T t;
        try {
            t = (T) obj;
        } catch (ClassCastException cce) {
            return false;
        }

        return base.contains(t) || (extension != null && extension.contains(t));
    }

    public Stream<T> stream(boolean parallel) {
        final var baseStream = parallel ? base.parallelStream() : base.stream();
        if (extension == null) {
            return baseStream;
        } else {
            return Stream.concat(baseStream, parallel ? extension.parallelStream() : extension.stream());
        }
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

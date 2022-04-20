package collections.decorators;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Stream;

public class ExtensionCollection<T> extends AbstractCollection<T> {
    @NotNull
    private final Collection<T> base;
    private final Collection<T> extension;

    public ExtensionCollection(@NotNull Collection<T> base, Collection<T> extension) {
        this.base = base;
        this.extension = extension;
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
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean contains(Object o) {
        return base.contains(o) || (extension != null && extension.contains(o));
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return stream().iterator();
    }

    public Stream<T> stream(boolean parallel) {
        final var baseStream = parallel ? base.parallelStream() : base.stream();
        if (extension == null) {
            return baseStream;
        } else {
            return Stream.concat(baseStream, parallel ? base.parallelStream() : base.stream());
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

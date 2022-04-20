package collections.iteration.decorators;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class UnmodifiableIterator<T> implements Iterator<T> {
    @NotNull
    private final Iterator<T> base;

    public UnmodifiableIterator(@NotNull Iterator<T> base) {
        this.base = base;
    }

    @Override
    public boolean hasNext() {
        return base.hasNext();
    }

    @Override
    public T next() {
        return base.next();
    }
}

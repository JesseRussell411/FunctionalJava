package collections.iteration;

import reference.pointers.FinalPointer;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public class MergingIterator<T> implements Iterator<T> {
    private final Iterator<T> iterA;
    private final Iterator<T> iterB;
    private final Comparator<T> comparator;
    private FinalPointer<T> cacheA;
    private FinalPointer<T> cacheB;

    public MergingIterator(Iterator<T> iterA, Iterator<T> iterB, Comparator<T> comparator) {
        this.iterA = Objects.requireNonNull(iterA);
        this.iterB = Objects.requireNonNull(iterB);
        this.comparator = Objects.requireNonNull(comparator);
    }

    @Override
    public boolean hasNext() {
        return hasA() || hasB();
    }

    @Override
    public T next() {
        if (hasA() && hasB()) {
            final var a = getA();
            final var b = getB();
            if (comparator.compare(a, b) <= 0) {
                cacheA = null;
                return a;
            } else {
                cacheB = null;
                return b;
            }
        } else if (hasA()) {
            final var a = getA();
            cacheA = null;
            return a;
        } else if (hasB()) {
            final var b = getB();
            cacheB = null;
            return b;
        } else {
            throw new NoSuchElementException();
        }
    }

    private boolean hasA() {
        return iterA.hasNext() || cacheA != null;
    }

    private boolean hasB() {
        return iterB.hasNext() || cacheB != null;
    }

    private T getA() {
        if (cacheA != null) {
            return cacheA.current;
        } else {
            final var next = iterA.next();
            cacheA = new FinalPointer<>(next);
            return next;
        }
    }

    private T getB() {
        if (cacheB != null) {
            return cacheB.current;
        } else {
            final var next = iterB.next();
            cacheB = new FinalPointer<>(next);
            return next;
        }
    }
}

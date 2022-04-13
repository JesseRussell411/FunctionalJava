package collections.iteration;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;

public class MergingIterable<T> implements Iterable<T> {
    private final Iterable<T> iterA;
    private final Iterable<T> iterB;
    private final Comparator<T> comparator;

    public MergingIterable(Iterable<T> iterA, Iterable<T> iterB, Comparator<T> comparator) {
        this.iterA = Objects.requireNonNull(iterA);
        this.iterB = Objects.requireNonNull(iterB);
        this.comparator = Objects.requireNonNull(comparator);
    }

    @Override
    public Iterator<T> iterator() {
        return new MergingIterator<>(iterA.iterator(), iterB.iterator(), comparator);
    }
}

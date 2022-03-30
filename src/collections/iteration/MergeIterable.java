package collections.iteration;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;

public class MergeIterable<T> implements Iterable<T> {
    private final Iterable<T> iterA;
    private final Iterable<T> iterB;
    private final Comparator<T> comparator;

    public MergeIterable(Iterable<T> iterA, Iterable<T> iterB, Comparator<T> comparator) {
        this.iterA = Objects.requireNonNull(iterA);
        this.iterB = Objects.requireNonNull(iterB);
        this.comparator = Objects.requireNonNull(comparator);
    }

    @Override
    public Iterator<T> iterator() {
        return new MergeIterator<>(iterA.iterator(), iterB.iterator(), comparator);
    }
}

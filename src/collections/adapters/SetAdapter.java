package collections.adapters;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

public class SetAdapter<A, B> extends AbstractSet<B> {
    private final Set<A> base;
    private final Function<A, B> aToB;
    private final Function<B, A> bToA;

    public SetAdapter(Set<A> base, Function<A, B> aToB, Function<B, A> bToA) {
        this.base = Objects.requireNonNull(base);
        this.aToB = Objects.requireNonNull(aToB);
        this.bToA = Objects.requireNonNull(bToA);
    }

    @Override
    public Iterator<B> iterator() {
        return base.stream().map(aToB).iterator();
    }

    @Override
    public int size() {
        return base.size();
    }

    @Override
    public boolean contains(Object obj) {
        B b;
        try {
            b = (B) obj;
        } catch (ClassCastException cce) {
            return false;
        }
        return base.contains(bToA.apply(b));
    }

    @Override
    public Stream<B> stream() {
        return base.stream().map(aToB);
    }

    @Override
    public Stream<B> parallelStream() {
        return base.parallelStream().map(aToB);
    }
}

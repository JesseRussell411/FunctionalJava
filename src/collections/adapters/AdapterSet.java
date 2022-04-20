package collections.adapters;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

public class AdapterSet<A, B> extends AbstractSet<B> {
    @NotNull
    private final Set<A> base;
    @NotNull
    private final Function<A, B> aToB;
    @NotNull
    private final Function<B, A> bToA;

    public AdapterSet(@NotNull Set<A> base, @NotNull Function<A, B> aToB, @NotNull Function<B, A> bToA) {
        this.base = Objects.requireNonNull(base);
        this.aToB = Objects.requireNonNull(aToB);
        this.bToA = Objects.requireNonNull(bToA);
    }

    @Override
    public Iterator<B> iterator() {
        return stream().iterator();
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
        final var a = bToA.apply(b);

        return base.contains(a);
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

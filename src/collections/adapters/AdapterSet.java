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
    private final Set<B> additional;
    @NotNull
    private final Function<A, B> aToB;
    @NotNull
    private final Function<B, A> bToA;

    public AdapterSet(@NotNull Set<A> base, @NotNull Function<A, B> aToB, @NotNull Function<B, A> bToA, Set<B> additional) {
        this.base = Objects.requireNonNull(base);
        this.additional = additional;
        this.aToB = Objects.requireNonNull(aToB);
        this.bToA = Objects.requireNonNull(bToA);
    }

    public AdapterSet(@NotNull Set<A> base, @NotNull Function<A, B> aToB, @NotNull Function<B, A> bToA) {
        this(base, aToB, bToA, null);
    }

    @Override
    public Iterator<B> iterator() {
        return stream().iterator();
    }

    @Override
    public int size() {
        if (additional == null) {
            return base.size();
        } else {
            return base.size() + additional.size();
        }
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

        return base.contains(a) || (additional != null && additional.contains(a));
    }

    @Override
    public Stream<B> stream() {
        final var baseStream = base.stream().map(aToB);
        if (additional == null) {
            return baseStream;
        } else {
            return Stream.concat(baseStream, additional.stream());
        }
    }

    @Override
    public Stream<B> parallelStream() {
        final var baseStream = base.parallelStream().map(aToB);
        if (additional == null) {
            return baseStream;
        } else {
            return Stream.concat(baseStream, additional.parallelStream());
        }
    }
}

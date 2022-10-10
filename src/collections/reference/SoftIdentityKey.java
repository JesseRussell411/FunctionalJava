package collections.reference;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Objects;

class SoftIdentityKey<T> extends SoftReference<T> {
    public final T tHolder;
    public final int hashCode;

    public SoftIdentityKey(T key, ReferenceQueue<T> queue) {
        super(key, queue);
        tHolder = null;
        hashCode = Objects.hashCode(key);
    }

    /**
     * Creates new Key which maintains a strong reference, for querying purposes.
     */
    public SoftIdentityKey(T key) {
        super(key);
        tHolder = key;
        hashCode = System.identityHashCode(key);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (!(obj instanceof SoftIdentityKey<?> other)) return false;

        final var inst = this.get();
        final var otherInst = other.get();
        if (inst == null || otherInst == null) return false;

        return inst == otherInst;
    }
}

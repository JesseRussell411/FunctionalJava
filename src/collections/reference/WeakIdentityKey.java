package collections.reference;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Objects;

class WeakIdentityKey<T> extends WeakReference<T> {
    public final T tHolder;
    public final int hashCode;

    public WeakIdentityKey(T key, ReferenceQueue<T> queue) {
        super(key, queue);
        tHolder = null;
        hashCode = Objects.hashCode(key);
    }

    /**
     * Creates new Key which maintains a strong reference, for querying purposes.
     */
    public WeakIdentityKey(T key) {
        super(key);
        tHolder = key;
        hashCode = Objects.hashCode(key);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (!(obj instanceof WeakIdentityKey<?> other)) return false;

        final var inst = this.get();
        final var otherInst = other.get();
        if (inst == null || otherInst == null) return false;

        return inst == otherInst;
    }
}

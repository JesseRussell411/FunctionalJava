package reference;

import java.util.Objects;
import java.util.function.Supplier;

// not sure if this is actually better than just a volatile field, but it is a nice proof-of-concept.

/**
 * A type of reference that starts unset and can only be set once.
 * The value of this reference is stored in a volatile field until it is set, at which point the value is stored in a
 * non-volatile field.
 * @param <T>
 */
public class VolatileUntilSet<T> implements Supplier<T> {
    private volatile T current;
    private T cacheable;

    public T get() {
        if (cacheable != null) return cacheable;
        cacheable = current;
        return cacheable;
    }

    public boolean isSet() {
        if (cacheable != null) return true;
        cacheable = current;
        return cacheable != null;
    }

    public boolean set(T value) {
        Objects.requireNonNull(value);
        if (isSet()) return false;

        synchronized (this) {
            if (isSet()) return false;

            current = value;
            cacheable = value;
            return true;
        }
    }
}

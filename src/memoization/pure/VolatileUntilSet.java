package memoization.pure;

import java.util.Objects;

public class VolatileUntilSet<T> {
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

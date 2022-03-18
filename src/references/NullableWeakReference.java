package references;

import java.lang.ref.WeakReference;

public class NullableWeakReference<T> {
    private final WeakReference<T> reference;

    public NullableWeakReference(T value) {
        if (value == null) {
            reference = null;
        } else {
            reference = new WeakReference<>(value);
        }
    }

    public T get() {
        return isNull() ? null : reference.get();
    }

    public boolean isNull() {
        return reference == null;
    }

    public boolean isCollected() {
        return !isNull() && reference.get() == null;
    }
}
package collections;

import java.lang.ref.WeakReference;

public record NullableWeakReference<T>(WeakReference<T> reference) {
    public T get() {
        return isNull() ? null : reference().get();
    }

    public boolean isNull() {
        return reference() == null;
    }

    public boolean isCollected() {
        return !isNull() && reference.get() == null;
    }
}

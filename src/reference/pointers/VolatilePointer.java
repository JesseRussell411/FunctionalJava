package reference.pointers;

public class VolatilePointer<T> {
    public volatile T current;

    public VolatilePointer(T value) {
        current = value;
    }

    public VolatilePointer() {
        this(null);
    }
}

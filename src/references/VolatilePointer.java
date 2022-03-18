package references;

public class VolatilePointer<T> {
    public volatile T value;

    public VolatilePointer(T value) {
        this.value = value;
    }

    public VolatilePointer() {
        this(null);
    }
}

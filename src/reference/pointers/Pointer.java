package reference.pointers;

public class Pointer<T> {
    public T current;

    public Pointer(T value) {
        current = value;
    }

    public Pointer() {
        this(null);
    }
}

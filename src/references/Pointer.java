package references;

public class Pointer<T> {
    public T value;

    public Pointer(T value) {
        this.value = value;
    }

    public Pointer() {
        this(null);
    }
}

package reference;

public class FinalPointer<T> {
    public final T current;

    public FinalPointer(T value) {
        current = value;
    }

    public FinalPointer() {
        this(null);
    }
}

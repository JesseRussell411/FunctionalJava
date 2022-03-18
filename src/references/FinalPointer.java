package references;

public class FinalPointer<T> {
    public final T value;

    public FinalPointer(T value) {
        this.value = value;
    }


    public FinalPointer() {
        this(null);
    }
}

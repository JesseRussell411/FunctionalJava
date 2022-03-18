package references;

public class Init<T> {
    private volatile FinalPointer<T> value = null;
    private FinalPointer<T> cache = null;

    private T startingValue;

    public Init(T startingValue) {
        this.startingValue = startingValue;
    }

    public Init() {
        this(null);
    }

    public boolean isSet() {
        if (cache != null) return true;
        cache = value;
        return cache != null;
    }

    public T get() {
        if (isSet()) {
            if (cache != null) return cache.value;
            cache = value;
            if (cache != null) return cache.value;
        }
        return startingValue;
    }

    public boolean set(T value) {
        if (isSet()) return false;

        synchronized (this) {
            if (isSet()) return false;

            final var newValue = new FinalPointer<>(value);
            this.value = newValue;
            cache = newValue;
            startingValue = null;
            return true;
        }
    }
}

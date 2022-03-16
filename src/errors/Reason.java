package errors;

public class Reason {
    private final String message;

    public final String getMessage() {
        return message;
    }

    public Reason(String message) {
        this.message = message;
    }
}

package errors;

/**
 * The program has entered a state that is impossible.
 */
public class ImpossibleStateException extends IllegalStateException {
    public ImpossibleStateException() {
        super();
    }

    public ImpossibleStateException(String s) {
        super(s);
    }

    public ImpossibleStateException(String message, Throwable cause) {
        super(message, cause);
    }

    public ImpossibleStateException(Throwable cause) {
        super(cause);
    }
}

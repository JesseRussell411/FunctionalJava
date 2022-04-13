package errors;

/**
 * A lighter weight exception for non-exceptional circumstances.
 * Exceptions are created when something bad happens.
 * Sometimes you just need an explanation why something (not bad) happened and this is for that case.
 */
public class Reason {
    private final String message;

    public final String getMessage() {
        return message;
    }

    public Reason(String message) {
        this.message = message;
    }
}

package composition;

import errors.CancellationReason;

import java.util.Objects;

public class Task<T> {
    private final Promise<T>.Deferred deferred;
    private final Runnable onCancel;

    public Task(Promise<T> base, Runnable onCancel) {
        Objects.requireNonNull(base);

        deferred = base.defer();
        this.onCancel = onCancel;
    }

    public Task(Promise<T> base) {
        this(base, null);
    }

    public Promise<T> promise() {
        return deferred.promise();
    }

    public boolean cancel(CancellationReason reason) {
        if (onCancel != null) onCancel.run();
        return deferred.settle().cancel(reason);
    }
}

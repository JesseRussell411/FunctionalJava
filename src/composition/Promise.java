package composition;

import errors.CancellationReason;

import java.util.Collection;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Promise<T> {
    private volatile State state = State.PENDING;
    private volatile Object value = null;

    private Promise() {
    }

    public Promise(Consumer<Settle> init) {
        init.accept(new Settle());
    }

    public static <T> Promise<T> consolidate(Promise<Promise<T>> first) {
        if (first == null) return null;
        final var next = new Promise<T>();

        first.then(
                (promise) -> {
                    if (promise == null) {
                        next.settle().resolve(null);
                    } else {
                        promise.then(
                                (result) -> next.settle().resolve(result),
                                (error) -> next.settle().reject(error),
                                (reason) -> next.settle().cancel(reason));
                    }
                    return null;
                },
                (error) -> next.settle().reject(error),
                (reason) -> next.settle().cancel(reason));

        return next;
    }

    public Deferred defer() {
        return defer(Promise.pending());
    }

    public Deferred defer(Promise<T>.Deferred deferred) {
        then(
                result -> deferred.settle().resolve(result),
                error -> deferred.settle().reject(error),
                reason -> deferred.settle().cancel(reason));

        return deferred;
    }

    public static <T> Promise<T>.Deferred pending() {
        return new Promise<T>().new Deferred();
    }

    public static <T> Promise<T> resolved(T result) {
        final var promise = new Promise<T>();
        promise.new Settle().resolve(result);
        return promise;
    }

    public static <T> Promise<T> rejected(Throwable error) {
        final var promise = new Promise<T>();
        promise.new Settle().reject(error);
        return promise;
    }

    public static <T> Promise<T> canceled(CancellationReason reason) {
        final var promise = new Promise<T>();
        promise.new Settle().cancel(reason);
        return promise;
    }

    public static <T> Promise<T> fromCompletableFuture(CompletableFuture<T> future) {
        if (future == null) return null;

        final var deferred = Promise.<T>pending();

        future.thenAccept(deferred.settle()::resolve);
        future.exceptionally(error -> {
            deferred.settle().reject(error);
            return null;
        });

        return deferred.promise();
    }

    public CompletableFuture<T> toCompletableFuture() {
        final var future = new CompletableFuture<T>();
        then(
                future::complete,
                future::completeExceptionally,
                reason -> future.completeExceptionally(new CancellationException(reason.getMessage())));

        return future;
    }

    public T join() throws InterruptedException, ExecutionException, CancellationException {
        final var lock = new Object();

        onSettle(() -> {
            lock.notifyAll();
            return null;
        });

        synchronized (lock) {
            while (isPending()) lock.wait();
        }

        return switch (state) {
            case RESOLVED -> getResult();
            case REJECTED -> throw new ExecutionException(getError());
            case CANCELED -> throw new CancellationException(getCancelationReason().getMessage());
            default -> throw new IllegalStateException();
        };
    }

    public State getState() {
        return state;
    }

    public boolean isRejected() {
        return state == State.REJECTED;
    }

    public boolean isResolved() {
        return state == State.RESOLVED;
    }

    public boolean isCanceled() {
        return state == State.CANCELED;
    }

    public boolean isPending() {
        return state == State.PENDING;
    }

    public boolean isSettled() {
        return !isPending();
    }

    public T getResult() {
        if (isResolved()) {
            return (T) value;
        } else return null;
    }

    public Throwable getError() {
        if (isRejected()) {
            return (Throwable) value;
        } else return null;
    }

    public CancellationReason getCancelationReason() {
        if (isCanceled()) {
            return (CancellationReason) value;
        } else return null;
    }

    private Settle settle() {
        return new Settle();
    }

    public class Settle {
        private Settle() {
        }

        public boolean resolve(T result) {
            if (isSettled()) return false;
            synchronized (Promise.this) {
                if (isSettled()) return false;
                Promise.this.value = result;
                Promise.this.state = State.RESOLVED;
                settleAllReactions();
                return true;
            }
        }


        public boolean reject(Throwable error) {
            if (isSettled()) return false;
            synchronized (Promise.this) {
                if (isSettled()) return false;
                Promise.this.value = error;
                Promise.this.state = State.REJECTED;
                settleAllReactions();
                return true;
            }
        }

        public boolean cancel(CancellationReason reason) {
            if (isSettled()) return false;
            synchronized (Promise.this) {
                if (isSettled()) return false;
                Promise.this.value = reason;
                Promise.this.state = State.CANCELED;
                settleAllReactions();
                return true;
            }
        }

        public boolean resolveWith(Supplier<T> getResult) {
            if (isSettled()) return false;
            synchronized (Promise.this) {
                if (isSettled()) return false;
                Promise.this.value = getResult.get();
                Promise.this.state = State.RESOLVED;
                settleAllReactions();
                return true;
            }
        }

        public boolean rejectWith(Supplier<Throwable> getError) {
            if (isSettled()) return false;
            synchronized (Promise.this) {
                if (isSettled()) return false;
                Promise.this.value = getError.get();
                Promise.this.state = State.REJECTED;
                settleAllReactions();
                return true;
            }
        }

        public boolean cancelWith(Supplier<CancellationReason> getReason) {
            if (isSettled()) return false;
            synchronized (Promise.this) {
                if (isSettled()) return false;
                Promise.this.value = getReason.get();
                Promise.this.state = State.CANCELED;
                settleAllReactions();
                return true;
            }
        }
    }

    public enum State {
        REJECTED,
        RESOLVED,
        CANCELED,
        PENDING
    }

    public class Deferred {
        private Deferred() {
        }

        public Promise<T> promise() {
            return Promise.this;
        }

        public Promise<T>.Settle settle() {
            return new Settle();
        }
    }

    // reaction //
    private final Collection<Reaction<T, ?>> reactions = new ConcurrentLinkedDeque<>();
    private final ReadWriteLock reactionsLock = new ReentrantReadWriteLock();

    private boolean settleReaction(Reaction<T, ?> reaction) {
        switch (state) {
            case RESOLVED:
                reaction.resolve((T) value);
                break;
            case REJECTED:
                reaction.reject((Throwable) value);
                break;
            case CANCELED:
                reaction.cancel((CancellationReason) value);
                break;
            default:
                return false;
        }
        return true;
    }

    private boolean settleAllReactions() {
        if (reactions.isEmpty()) return false;

        reactionsLock.writeLock().lock();
        try {
            switch (state) {
                case RESOLVED:
                    final var result = (T) value;
                    for (final var reaction : reactions) reaction.resolve(result);
                    break;
                case REJECTED:
                    final var error = (Throwable) value;
                    for (final var reaction : reactions) reaction.reject(error);
                    break;
                case CANCELED:
                    final var reason = (CancellationReason) value;
                    for (final var reaction : reactions) reaction.cancel(reason);
                    break;
                default:
                    return false;
            }
        } finally {
            reactionsLock.writeLock().unlock();
        }

        reactions.clear();
        return true;
    }

    public <R> Promise<R> then(Function<T, R> ifResolved, Function<Throwable, R> ifRejected, Function<CancellationReason, R> ifCanceled) {
        final var reaction = new Reaction<>(ifResolved, ifRejected, ifCanceled);

        if (!settleReaction(reaction)) {
            reactionsLock.readLock().lock();
            try {
                if (!settleReaction(reaction)) {
                    reactions.add(reaction);
                }
            } finally {
                reactionsLock.readLock().unlock();
            }
        }

        return reaction.getPromise();
    }

    public <R> Promise<R> then(Function<T, R> ifResolved, Function<Throwable, R> ifRejected) {
        return then(ifResolved, ifRejected, null);
    }

    public <R> Promise<R> then(Function<T, R> ifResolved) {
        return then(ifResolved, null, null);
    }

    public <R> Promise<R> onError(Function<Throwable, R> catcher) {
        return then(null, catcher, null);
    }

    public <R> Promise<R> onCancel(Function<CancellationReason, R> reaction) {
        return then(null, null, reaction);
    }

    public <R> Promise<R> onSettle(Supplier<R> reaction) {
        return then(
                (result) -> reaction.get(),
                (result) -> reaction.get(),
                (result) -> reaction.get());
    }

    private static class Reaction<T, R> {
        private final Function<T, R> ifResolved;
        private final Function<Throwable, R> ifRejected;
        private final Function<CancellationReason, R> ifCanceled;
        private final Promise<R> promise = new Promise<>();

        public Reaction(
                Function<T, R> ifResolved,
                Function<Throwable, R> ifRejected,
                Function<CancellationReason, R> ifCanceled) {
            this.ifResolved = ifResolved;
            this.ifRejected = ifRejected;
            this.ifCanceled = ifCanceled;
        }

        public Promise<R> getPromise() {
            return promise;
        }

        public void resolve(T result) {
            if (ifResolved != null) {
                try {
                    promise.settle().resolveWith(() -> ifResolved.apply(result));
                } catch (Throwable err) {
                    promise.settle().reject(err);
                }
            } else {
                promise.settle().cancel(new CancellationReason("Original promise resolved"));
            }
        }

        public void reject(Throwable error) {
            if (ifRejected != null) {
                try {
                    promise.settle().resolveWith(() -> ifRejected.apply(error));
                } catch (Throwable err) {
                    promise.settle().reject(err);
                }
            } else {
                promise.settle().reject(error);
            }
        }

        public void cancel(CancellationReason reason) {
            if (ifCanceled != null) {
                try {
                    promise.settle().resolveWith(() -> ifCanceled.apply(reason));
                } catch (Throwable err) {
                    promise.settle().reject(err);
                }
            } else {
                promise.settle().cancel(reason);
            }
        }
    }
}

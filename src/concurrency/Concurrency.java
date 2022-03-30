package concurrency;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Supplier;

public class Concurrency {
    public static <T> Promise<T> threadedCall(Supplier<T> job, ThreadPoolExecutor pool) {
        return new Promise<>(settle -> pool.execute(() -> {
            try {
                settle.resolve(job.get());
            } catch (RuntimeException e) {
                settle.reject(e);
            }
        }));
    }

    public static <T> Promise<T> threadedCall(Supplier<T> job) {
        return new Promise<>(settle -> {
            final var thread = new Thread(() -> {
                try {
                    settle.resolve(job.get());
                } catch (RuntimeException e) {
                    settle.reject(e);
                }
            });
        });
    }
}

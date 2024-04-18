package cn.crane4j.core.util;

/**
 * A functional interface that can be used to perform a computation that may throw an exception.
 *
 * @author huangchengxing
 * @since 2.8.0
 */
@FunctionalInterface
public interface CheckedRunnable {

    /**
     * Run the task.
     *
     * @throws Throwable the exception that may be thrown when running the task
     */
    @SuppressWarnings("java:S112")
    void run() throws Throwable;

    /**
     * Convert the runnable to a supplier.
     *
     * @return the supplier
     */
    default CheckedSupplier<Void> toSupplier() {
        return () -> {
            run();
            return null;
        };
    }
}

package cn.crane4j.core.util;

/**
 * A functional interface that can be used to perform a computation that may throw an exception.
 *
 * @param <R> the type of the result
 * @author huangchengxing
 * @since 2.8.0
 */
@FunctionalInterface
public interface CheckedSupplier<R> {

    /**
     * Get the result.
     *
     * @return the result
     * @throws Throwable the exception that may be thrown when getting the result
     */
    @SuppressWarnings("java:S112")
    R get() throws Throwable;
}

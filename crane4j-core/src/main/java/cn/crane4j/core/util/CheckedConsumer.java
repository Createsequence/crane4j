package cn.crane4j.core.util;

/**
 * A functional interface that can be used to perform a computation that may throw an exception.
 *
 * @param <T> the type of the input parameter
 * @author huangchengxing
 * @since 2.8.0
 */
@FunctionalInterface
public interface CheckedConsumer<T> {

    /**
     * Accept the input parameter.
     *
     * @param t the result
     * @throws Throwable the exception that may be thrown when accepting the input parameter
     */
    @SuppressWarnings("java:S112")
    void accept(T t) throws Throwable;
}

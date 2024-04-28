package cn.crane4j.core.util;

/**
 * A functional interface that can be used to perform a computation that may throw an exception.
 *
 * @author huangchengxing
 * @param <T> the type of the argument
 * @param <R> the type of the result
 */
@FunctionalInterface
public interface CheckedFunction<T, R> {

    /**
     * Apply this function to the given argument.
     *
     * @param t the function argument
     * @return the function result
     * @throws Throwable the exception that may be thrown when applying the function
     */
    @SuppressWarnings("java:S112")
    R apply(T t) throws Throwable;
}

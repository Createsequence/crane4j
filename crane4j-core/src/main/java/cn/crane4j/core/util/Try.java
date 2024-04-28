package cn.crane4j.core.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A control structure that can be used to
 * perform a computation that may throw an exception.
 *
 * @author huangchengxing
 * @since 2.8.0
 */
@RequiredArgsConstructor
public class Try<T> {

    /**
     * Create an action that does nothing.
     *
     * @param runnable the runnable
     * @return the try action instance
     */
    public static Try<Void> of(CheckedRunnable runnable) {
        Asserts.isNotNull(runnable, "runnable must not be null");
        return new Try<>(runnable.toSupplier());
    }

    /**
     * Create an action that does nothing and returns a result.
     *
     * @param supplier the supplier of the result
     * @param <R> the type of the result
     * @return the try action instance
     */
    public static <R> Try<R> of(CheckedSupplier<R> supplier) {
        Asserts.isNotNull(supplier, "supplier must not be null");
        return new Try<>(supplier);
    }

    /**
     * Create a successful action.
     *
     * @param result the result
     * @param <R> the type of the result
     * @return the try action instance
     */
    public static <R> Try<R> success(R result) {
        Try<R> action = new Try<>(() -> result);
        action.performed = true;
        action.result = result;
        return action;
    }

    /**
     * Create a failed action.
     *
     * @param ex the exception
     * @param <R> the type of the result
     * @return the try action instance
     */
    public static <R> Try<R> failure(Throwable ex) {
        Try<R> action = new Try<>(() -> { throw ex; });
        action.performed = true;
        action.cause = ex;
        return action;
    }

    /**
     * The supplier of the result.
     */
    private final CheckedSupplier<T> supplier;

    /**
     * The subscriber of the exception
     */
    private List<Consumer<Throwable>> failureSubscribers;

    /**
     * The subscriber of the result.
     */
    private List<Consumer<T>> successSubscribers;

    /**
     * The result.
     */
    private T result;

    /**
     * The exception.
     */
    private Throwable cause;

    /**
     * Whether the computation is performed.
     */
    @Getter
    private boolean performed = false;

    /**
     * Perform the computation and subscribe the result and the exception.
     */
    public final void perform() {
        if (isPerformed()) {
            return;
        }
        try {
            result = supplier.get();
            if (Objects.nonNull(successSubscribers) && !successSubscribers.isEmpty()) {
                successSubscribers.forEach(subscriber -> subscriber.accept(result));
            }
        } catch (Throwable ex) {
            if (Objects.nonNull(successSubscribers) && !failureSubscribers.isEmpty()) {
                failureSubscribers.forEach(subscriber -> subscriber.accept(ex));
            }
            cause = ex;
        } finally {
            performed = true;
        }
    }

    // ======== status operations ========

    /**
     * Whether the computation is successful.
     *
     * @return whether the computation is successful
     */
    public boolean isSuccess() {
        perform();
        return Objects.isNull(cause);
    }

    /**
     * Whether the computation is failed.
     *
     * @return whether the computation is failed
     */
    public boolean isFailure() {
        perform();
        return Objects.nonNull(cause);
    }

    /**
     * Get the cause of the exception.
     *
     * @return the exception
     * @see #isFailure()
     */
    @Nullable
    public Throwable getCause() {
        perform();
        return cause;
    }

    /**
     * Subscribe the result.
     *
     * @param subscriber the subscriber of the result
     * @return this
     * @throws IllegalStateException if the computation has been performed
     */
    public Try<T> subscribeFailure(Consumer<Throwable> subscriber) {
        Asserts.isFalse(performed, "the computation has been performed");
        Asserts.isNotNull(subscriber, "subscriber must not be null");
        if (Objects.isNull(failureSubscribers)) {
            failureSubscribers = new ArrayList<>();
        }
        failureSubscribers.add(subscriber);
        return this;
    }

    /**
     * Subscribe the result.
     *
     * @param subscriber the subscriber of the result
     * @return this
     * @throws IllegalStateException if the computation has been performed
     */
    public Try<T> subscribeSuccess(Consumer<T> subscriber) {
        Asserts.isFalse(performed, "the computation has been performed");
        Asserts.isNotNull(subscriber, "subscriber must not be null");
        if (Objects.isNull(successSubscribers)) {
            successSubscribers = new ArrayList<>();
        }
        successSubscribers.add(subscriber);
        return this;
    }

    // ======== get operations ========

    /**
     * Get the result.
     *
     * @return the result
     * @throws Throwable the exception that may be thrown by the supplier
     */
    @SuppressWarnings("java:S112")
    public T get() throws Throwable {
        if (isSuccess()) {
            return result;
        }
        throw cause;
    }

    /**
     * Get the result or null if the supplier throws an exception.
     *
     * @return the result or null
     * @see #getOptional()
     */
    public T getOrNull() {
        return isSuccess() ? result : null;
    }

    /**
     * Get the optional result.
     *
     * @return the optional result
     * @see #getOrNull()
     */
    public Optional<T> getOptional() {
        return isSuccess() ?
            Optional.ofNullable(result) : Optional.empty();
    }

    /**
     * Get the result or the default value.
     *
     * @param defaultValue the default value to return if the supplier throws an exception
     * @return the result or the default value
     */
    public T getOrElse(T defaultValue) {
        return isSuccess() ? result : defaultValue;
    }

    /**
     * Get the result or the default value.
     *
     * @param function the function to return the default value if the supplier throws an exception
     * @return the result or the default value returned by the function
     */
    public T getOrElseGet(Function<Throwable, T> function) {
        return isSuccess() ? result : function.apply(cause);
    }

    /**
     * Get the result or throw an exception.
     *
     * @param function the function to throw an exception if the supplier throws an exception
     * @param <X> the type of the exception to throw
     * @return the result
     * @throws X the exception thrown by the function
     */
    public <X extends Throwable> T getOrElseThrow(Function<Throwable, X> function) throws X {
        if (isSuccess()) {
            return result;
        }
        throw function.apply(cause);
    }

    /**
     * Get this or the other instance if this action is failed.
     *
     * @param function the function to get the other instance
     * @return this or the other instance
     */
    public Try<T> getOrElseTry(CheckedFunction<Throwable, T> function) {
        return isSuccess() ? this : of(() -> function.apply(cause));
    }

    // ======== run operations ========

    /**
     * Run the supplier.
     *
     * @throws Throwable the exception that may be thrown by the supplier
     */
    @SuppressWarnings("java:S112")
    public void run() throws Throwable {
        if (isFailure()) {
            throw cause;
        }
    }

    /**
     * Run the supplier and throw an exception if the supplier throws an exception.
     *
     * @param function the function to handle the exception
     */
    public <X extends Throwable> void runOrThrow(Function<Throwable, X> function) throws X {
        if (isFailure()) {
            throw function.apply(cause);
        }
    }
}

package cn.crane4j.core.exception;

import cn.crane4j.core.util.StringUtils;

/**
 * Crane's runtime exception
 *
 * @author huangchengxing
 */
public class Crane4jException extends RuntimeException {

    /**
     * Wrap the specified exception if necessary.
     *
     * @param cause the cause
     * @return cause if it is a runtime exception, otherwise a new Crane4jException
     */
    public static RuntimeException wrapIfNecessary(Throwable cause) {
        return cause instanceof RuntimeException ?
            (RuntimeException)cause : new Crane4jException(cause);
    }

    /**
     * Constructs a new runtime exception with the specified detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     *
     * @param messageTemplate the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     * @param args args of message template
     */
    public Crane4jException(String messageTemplate, Object... args) {
        super(StringUtils.format(messageTemplate, args));
    }

    /**
     * Constructs a new runtime exception with the specified cause and a
     * detail message of <tt>(cause==null ? null : cause.toString())</tt>
     * (which typically contains the class and detail message of
     * <tt>cause</tt>).  This constructor is useful for runtime exceptions
     * that are little more than wrappers for other throwables.
     *
     * @param cause the cause (which is saved for later retrieval by the
     *              {@link #getCause()} method).  (A <tt>null</tt> value is
     *              permitted, and indicates that the cause is nonexistent or
     *              unknown.)
     * @since 1.4
     */
    public Crane4jException(Throwable cause) {
        super(cause);
    }
}

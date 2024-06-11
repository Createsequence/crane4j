package cn.crane4j.core.exception;

import org.junit.Assert;
import org.junit.Test;

/**
 * test for {@link Crane4jException}
 *
 * @author huangchengxing
 */
public class Crane4jExceptionTest {

    @Test
    public void wrapIfNecessary() {
        Throwable cause = new IllegalArgumentException();
        Assert.assertSame(cause, Crane4jException.wrapIfNecessary(cause));
        cause = new OutOfMemoryError();
        Assert.assertTrue(Crane4jException.wrapIfNecessary(cause) instanceof Crane4jException);
    }

    @Test
    public void test() {
        Assert.assertThrows("ex!", Crane4jException.class, () -> { throw new Crane4jException("{}!", "ex"); });
        RuntimeException e = new RuntimeException("ex!");
        Assert.assertThrows("ex!", Crane4jException.class, () -> { throw new Crane4jException(e); });
    }
}

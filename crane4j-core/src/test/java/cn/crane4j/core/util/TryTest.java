package cn.crane4j.core.util;

import cn.crane4j.core.exception.Crane4jException;
import org.junit.Assert;
import org.junit.Test;

import java.util.Objects;
import java.util.Optional;

/**
 * test for {@link Try}
 *
 * @author huangchengxing
 */
public class TryTest {

    @Test
    public void testApply() {
        Object object = new Object();
        Try<Object> objectTry = Try.of(() -> object);

        objectTry.subscribeSuccess(e -> Assert.assertSame(object, e));
        objectTry.subscribeFailure(ex -> Assert.fail());

        Assert.assertFalse(objectTry.isPerformed());
        Assert.assertTrue(objectTry.isSuccess());
        Assert.assertFalse(objectTry.isFailure());
        Assert.assertNull(objectTry.getCause());
        Assert.assertTrue(objectTry.isPerformed());

        try {
            objectTry.get();
        } catch (Throwable ex) {
            Assert.fail();
        }
        Assert.assertSame(object, objectTry.getOrNull());
        Assert.assertSame(object, objectTry.getOrElse(new Object()));
        Assert.assertSame(object, objectTry.getOrElseThrow(ex -> new IllegalStateException()));


        Try<Object> throwTry = Try.of(() -> {
            throw new IllegalArgumentException();
        }).getOrElseTry(e -> object);
        Optional<Object> optional = throwTry.getOptional();
        Assert.assertTrue(optional.isPresent());
        Assert.assertSame(object, optional.get());
    }

    @Test
    public void testApplyOnFailure() {
        Object object = new Object();
        Throwable ex = new IllegalArgumentException();
        Try<Object> objectTry = Try.of(() -> {
            throw ex;
        });

        Assert.assertSame(objectTry, objectTry.subscribeSuccess(e -> Assert.fail()));
        Assert.assertSame(objectTry, objectTry.subscribeFailure(e -> Assert.assertSame(ex, e)));

        Assert.assertFalse(objectTry.isPerformed());
        Assert.assertFalse(objectTry.isSuccess());
        Assert.assertTrue(objectTry.isFailure());
        Assert.assertSame(ex, objectTry.getCause());
        Assert.assertTrue(objectTry.isPerformed());

        Assert.assertThrows(IllegalArgumentException.class, objectTry::get);
        Assert.assertSame(object, objectTry.getOrElseGet(e -> object));
        Assert.assertSame(object, objectTry.getOrElse(object));
        Assert.assertThrows(IllegalStateException.class, () -> objectTry.getOrElseThrow(e -> new IllegalStateException()));

        Try<Object> throwTry = Try.of(() -> {
            throw ex;
        }).getOrElseTry(e -> {
            Assert.assertSame(ex, e);
            throw new IllegalStateException();
        });
        Assert.assertFalse(throwTry.isSuccess());
        Assert.assertTrue(throwTry.isFailure());
        Assert.assertThrows(IllegalStateException.class, throwTry::get);
    }

    @Test
    public void runTest() {
        Try<Void> voidTry = Try.of(() -> {});
        try {
            voidTry.run();
        } catch (Throwable ex) {
            Assert.fail();
        }
        voidTry.runOrThrow(ex -> {
            if (Objects.nonNull(ex)) {
                Assert.fail();
            }
            return null;
        });

        Try<Void> throwTry = Try.of(() -> {
            throw new IllegalArgumentException();
        });
        Assert.assertThrows(IllegalArgumentException.class, throwTry::run);
        Assert.assertThrows(IllegalStateException.class, () -> throwTry.runOrThrow(e -> new IllegalStateException()));
    }

    @Test
    public void successTest() {
        Object object = new Object();
        Try<Object> success = Try.success(object);
        Assert.assertTrue(success.isPerformed());
        Assert.assertTrue(success.isSuccess());
        Assert.assertFalse(success.isFailure());
        Assert.assertNull(success.getCause());

        Assert.assertThrows(Crane4jException.class, () -> success.subscribeFailure(e -> {}));
        Assert.assertThrows(Crane4jException.class, () -> success.subscribeSuccess(e -> {}));
    }

    @Test
    public void failureTest() {
        Throwable ex = new IllegalArgumentException();
        Try<Object> failure = Try.failure(ex);
        Assert.assertTrue(failure.isPerformed());
        Assert.assertFalse(failure.isSuccess());
        Assert.assertTrue(failure.isFailure());
        Assert.assertSame(ex, failure.getCause());

        Assert.assertThrows(Crane4jException.class, () -> failure.subscribeFailure(e -> {}));
        Assert.assertThrows(Crane4jException.class, () -> failure.subscribeSuccess(e -> {}));
    }
}

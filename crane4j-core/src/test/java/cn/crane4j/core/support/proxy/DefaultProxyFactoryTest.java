package cn.crane4j.core.support.proxy;

import cn.crane4j.core.exception.Crane4jException;
import lombok.RequiredArgsConstructor;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * test for {@link DefaultProxyFactory}
 *
 * @author huangchengxing
 */
public class DefaultProxyFactoryTest {

    @Test
    public void interfaceProxyTest() {
        DefaultProxyFactory proxyFactory = DefaultProxyFactory.INSTANCE;
        TestInvocationHandler handler = new TestInvocationHandler();
        TestInterface proxy = proxyFactory.createProxy(handler, TestInterface.class);
        Assert.assertTrue(proxy instanceof ProxyFactory.Proxied);
        Assert.assertEquals(1, proxy.count1());
        Assert.assertEquals(2, proxy.count1());
    }

    @Test
    public void abstractClassTest() {
        DefaultProxyFactory proxyFactory = DefaultProxyFactory.INSTANCE;
        TestInvocationHandler handler = new TestInvocationHandler();
        Object proxy = proxyFactory.createProxy(handler, TestAbstractClass.class, TestInterface.class);
        Assert.assertTrue(proxy instanceof ProxyFactory.Proxied);

        Assert.assertTrue(proxy instanceof TestInterface);
        Assert.assertEquals(1, ((TestInterface)proxy).count1());
        Assert.assertEquals(2, ((TestInterface)proxy).count1());

        Assert.assertTrue(proxy instanceof TestAbstractClass);
        Assert.assertEquals(-1, ((TestAbstractClass)proxy).notProxyCount1());
        Assert.assertEquals(3, ((TestAbstractClass)proxy).count2());
        Assert.assertEquals(4, ((TestAbstractClass)proxy).count2());
    }

    @Test
    public void noConstructorTest() {
        DefaultProxyFactory proxyFactory = DefaultProxyFactory.INSTANCE;
        TestInvocationHandler handler = new TestInvocationHandler();
        Assert.assertThrows(
            Crane4jException.class, () -> proxyFactory.createProxy(handler, ConstructorTestAbstractClass.class)
        );
    }

    @Test
    public void classTest() {
        DefaultProxyFactory proxyFactory = DefaultProxyFactory.INSTANCE;
        TestInvocationHandler handler = new TestInvocationHandler();
        Assert.assertThrows(
            Crane4jException.class, () -> proxyFactory.createProxy(handler, TestClass.class, TestInterface.class, TestAbstractClass.class)
        );

        Object proxy = proxyFactory.createProxy(handler, TestInterface.class, TestClass.class);
        Assert.assertTrue(proxy instanceof ProxyFactory.Proxied);

        Assert.assertTrue(proxy instanceof TestInterface);
        Assert.assertEquals(1, ((TestInterface)proxy).count1());
        Assert.assertEquals(2, ((TestInterface)proxy).count1());

        Assert.assertTrue(proxy instanceof TestClass);
        Assert.assertEquals(-1, ((TestClass)proxy).notProxyCount2());
        Assert.assertEquals(3, ((TestClass)proxy).count3());
        Assert.assertEquals(4, ((TestClass)proxy).count3());
    }

    public interface TestInterface {
        int count1();
    }

    public abstract static class TestAbstractClass {
        public abstract int count2();
        private int notProxyCount1() {
            return -1;
        }
    }

    @RequiredArgsConstructor
    public abstract static class ConstructorTestAbstractClass {
        private final int count;
    }

    public static class TestClass {
        public int count3() {
            return 0;
        }
        private int notProxyCount2() {
            return -1;
        }
    }

    private static class TestInvocationHandler implements InvocationHandler {
        private final AtomicInteger count = new AtomicInteger();
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            return count.incrementAndGet();
        }
    }
}

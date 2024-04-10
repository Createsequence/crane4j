package cn.crane4j.core.support.proxy;

import java.lang.reflect.InvocationHandler;

/**
 * A factory that can be used to create proxy objects.
 *
 * @author huangchengxing
 * @see DefaultProxyFactory
 * @since 2.8.0
 */
public interface ProxyFactory {

    /**
     * Create a proxy object for the specified interface.
     *
     * @param proxyTypes proxy type
     * @param handler invocation handler
     * @param <T> the type of the proxy object
     * @return the proxy object
     */
    <T> T createProxy(InvocationHandler handler, Class<?>... proxyTypes);

    /**
     * A marker interface that indicates the object is a proxy object.
     */
    interface Proxied { }
}

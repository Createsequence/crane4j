package cn.crane4j.core.support.proxy;

import cn.crane4j.core.exception.Crane4jException;
import cn.crane4j.core.util.ArrayUtils;
import cn.crane4j.core.util.Asserts;
import cn.crane4j.core.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.ModifierReviewable;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The default implementation of {@link ProxyFactory}.
 * Supports JDK dynamic proxy and ByteButty proxy.
 *
 * @author huangchengxing
 * @since 2.8.0
 */
@Slf4j
public class DefaultProxyFactory implements ProxyFactory {

    public static final DefaultProxyFactory INSTANCE = new DefaultProxyFactory();

    /**
     * <p>Create a proxy object for the specified types.
     *
     * <p>If all proxy types are interfaces, use JDK dynamic proxy,
     * otherwise, use ByteButty proxy.<br/>
     * All proxy objects will implement the {@link Proxied} interface.
     *
     * @param handler    invocation handler
     * @param proxyTypes proxy type
     * @param <T> the type of the proxy object
     * @return the proxy object
     */
    @Override
    public <T> T createProxy(
        InvocationHandler handler, Class<?>... proxyTypes) {
        Asserts.isNotEmpty(proxyTypes, "Proxy types must not be empty");
        proxyTypes = ArrayUtils.append(proxyTypes, Proxied.class);
        // If all proxy types are interfaces, use JDK dynamic proxy
        if (Stream.of(proxyTypes).allMatch(Class::isInterface)) {
            return createProxyByJdk(handler, proxyTypes);
        }
        // Otherwise, use ByteButty proxy
        Map<Boolean, Set<Class<?>>> types = Stream.of(proxyTypes)
            .collect(Collectors.partitioningBy(Class::isInterface, Collectors.toSet()));
        Class<?> parent = determineParentType(types.get(false));
        List<Class<?>> interfaces = new ArrayList<>(types.get(true));
        return createProxyByByteBuddy(handler, parent, interfaces);
    }

    @SuppressWarnings("unchecked")
    @NonNull
    private <T> T createProxyByJdk(InvocationHandler handler, Class<?>[] proxyTypes) {
        return (T)Proxy.newProxyInstance(Thread.currentThread()
            .getContextClassLoader(), proxyTypes, handler);
    }

    @SuppressWarnings("unchecked")
    @NonNull
    private <T> T createProxyByByteBuddy(InvocationHandler handler, Class<?> parent, List<Class<?>> interfaces) {
        try {
            Class<?> proxyType = makeProxyType(handler, parent, interfaces);
            return (T)proxyType.getDeclaredConstructor()
                .newInstance();
        } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            log.error("Failed to create proxy object", e);
            throw new Crane4jException(e);
        }
    }

    @NonNull
    private static Class<?> makeProxyType(
        InvocationHandler handler, Class<?> parent, List<Class<?>> interfaces) {
        DynamicType.Loaded<?> dynamicTypeloaded = new ByteBuddy()
            .subclass(parent)
            .implement(interfaces)
            .method(ModifierReviewable.OfByteCodeElement::isPublic)
            .intercept(InvocationHandlerAdapter.of(handler))
            .make()
            .load(Thread.currentThread().getContextClassLoader());
        return dynamicTypeloaded.getLoaded();
    }

    @SuppressWarnings("all")
    @NonNull
    private static Class<?> determineParentType(@Nullable Set<Class<?>> candidates) {
        Asserts.isNotNull(candidates, "No parent type candidates provided");
        Asserts.isTrue(candidates.size() == 1, "Only one parent class is allowed, but got: " + candidates);
        Class<?> parent = CollectionUtils.getFirstNotNull(candidates);
        return Objects.isNull(parent) ? Object.class : parent;
    }
}

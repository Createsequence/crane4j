package cn.crane4j.core.support.aop;

import cn.crane4j.annotation.ArgAutoOperate;
import cn.crane4j.annotation.AutoOperate;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.proxy.ProxyFactory;
import cn.crane4j.core.util.Asserts;
import cn.crane4j.core.util.ReflectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * A proxy factory for auto operates.
 *
 * @author huangchengxing
 * @since 2.8.0
 */
@Slf4j
@RequiredArgsConstructor
public class AutoOperateProxy {

    private final ResultHandler resultHandler = new ResultHandler();
    private final ArgumentsHandler argumentsHandler = new ArgumentsHandler();
    private final ResultAndArgumentsHandler resultAndArgumentsHandler = new ResultAndArgumentsHandler();

    private final MethodArgumentAutoOperateSupport methodArgumentAutoOperateSupport;
    private final MethodResultAutoOperateSupport methodResultAutoOperateSupport;
    private final AnnotationFinder annotationFinder;
    private final ProxyFactory proxyFactory;

    /**
     * Wrap a target object if necessary.
     *
     * @param target target object
     * @param <T> target type
     * @return wrapped object
     */
    public <T> T wrapIfNecessary(T target) {
        Class<?> proxyType = target.getClass();
        Asserts.isFalse(Proxy.isProxyClass(proxyType), "target is already proxied: {}", proxyType);
        Map<Method, MethodHandler> methodHandles = resolveMethodHandler(proxyType);
        if (methodHandles.isEmpty()) {
            log.info("no method need to be proxied: {}", proxyType);
            return target;
        }
        return proxyFactory.createProxy(new AutoOperateMethodHandler(methodHandles, target), proxyType);
    }

    @NonNull
    private Map<Method, MethodHandler> resolveMethodHandler(Class<?> targetType) {
        Map<Method, MethodHandler> methodHandles = new HashMap<>(16);
        ReflectUtils.traverseTypeHierarchy(targetType, type -> {
            if (!Objects.equals(Object.class, type)) {
                processHandleableMethods(type, methodHandles);
            }
        });
        return methodHandles;
    }

    private void processHandleableMethods(Class<?> t, Map<Method, MethodHandler> methodHandles) {
        Stream.of(ReflectUtils.getDeclaredMethods(t))
            .filter(this::isHandleableMethod)
            .forEach(m -> {
                MethodHandler handler = determineHandler(m);
                if (Objects.nonNull(handler)) {
                    // keep only the override method
                    methodHandles.putIfAbsent(m, handler);
                }
            });
    }

    private boolean isHandleableMethod(Method method) {
        if (method.isSynthetic()) {
            return false;
        }
        int modifiers = method.getModifiers();
        return Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers);
    }

    @Nullable
    private MethodHandler determineHandler(Method method) {
        AutoOperate annotation = annotationFinder.getAnnotation(method, AutoOperate.class);
        if (Objects.isNull(annotation)) {
            return needHandleArgs(method) ?
                argumentsHandler : null;
        }
        return needHandleArgs(method) ?
            resultAndArgumentsHandler : resultHandler;
    }

    @RequiredArgsConstructor
    private static class AutoOperateMethodHandler implements InvocationHandler {
        private final Map<Method, MethodHandler> methodHandles;
        private final Object target;
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            MethodHandler handler = methodHandles.get(method);
            return Objects.isNull(handler) ?
                ReflectUtils.invokeRaw(target, method, args) : handler.invoke(target, method, args);
        }
    }

    private boolean needHandleArgs(Method method) {
        return annotationFinder.hasAnnotation(method, ArgAutoOperate.class)
            || Stream.of(method.getParameters()).anyMatch(m -> annotationFinder.hasAnnotation(m, AutoOperate.class));
    }

    // ========= handles =========

    private interface MethodHandler {

        /**
         * invoke method
         *
         * @param target target
         * @param method method
         * @param arguments arguments
         * @return result of invocation
         */
        Object invoke(Object target, Method method, Object[] arguments);
    }

    @RequiredArgsConstructor
    private class ResultHandler implements MethodHandler {
        @Override
        public Object invoke(Object target, Method method, Object[] arguments) {
            Object result = ReflectUtils.invoke(target, method, arguments);
            methodResultAutoOperateSupport.afterMethodInvoke(method, result, arguments);
            return result;
        }
    }

    @RequiredArgsConstructor
    private class ArgumentsHandler implements MethodHandler {
        @Override
        public Object invoke(Object target, Method method, Object[] arguments) {
            methodArgumentAutoOperateSupport.beforeMethodInvoke(method, arguments);
            return ReflectUtils.invoke(target, method, arguments);
        }
    }

    @RequiredArgsConstructor
    private class ResultAndArgumentsHandler implements MethodHandler {
        @Override
        public Object invoke(Object target, Method method, Object[] arguments) {
            methodArgumentAutoOperateSupport.beforeMethodInvoke(method, arguments);
            Object result = ReflectUtils.invoke(target, method, arguments);
            methodResultAutoOperateSupport.afterMethodInvoke(method, result, arguments);
            return result;
        }
    }
}

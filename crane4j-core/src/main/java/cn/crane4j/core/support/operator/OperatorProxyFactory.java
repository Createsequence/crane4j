package cn.crane4j.core.support.operator;

import cn.crane4j.annotation.Operator;
import cn.crane4j.core.exception.Crane4jException;
import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.Crane4jGlobalSorter;
import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.support.SimpleAnnotationFinder;
import cn.crane4j.core.support.proxy.DefaultProxyFactory;
import cn.crane4j.core.support.proxy.ProxyFactory;
import cn.crane4j.core.util.Asserts;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.core.util.ReflectUtils;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * An operator proxy object factory that to generate proxy objects
 * that can call specific annotation marked interface methods.
 *
 * @author huangchengxing
 * @see Operator
 * @see OperatorProxyMethodFactory
 * @see ProxyFactory
 * @since 1.3.0
 */
@Builder
@Slf4j
public class OperatorProxyFactory {

    private static final Object NULL = new Object();
    private final Crane4jGlobalConfiguration globalConfiguration;
    @Builder.Default
    private final AnnotationFinder annotationFinder = SimpleAnnotationFinder.INSTANCE;
    private final List<OperatorProxyMethodFactory> proxyMethodFactories = new ArrayList<>(8);
    @Builder.Default
    private final ProxyFactory proxyFactory = DefaultProxyFactory.INSTANCE;
    private final Map<Class<?>, Object> proxyCaches = new ConcurrentHashMap<>(8);

    /**
     * Add a {@link OperatorProxyMethodFactory} instance.
     *
     * @param proxyMethodFactory proxy method factory
     */
    public void addProxyMethodFactory(OperatorProxyMethodFactory proxyMethodFactory) {
        proxyMethodFactories.remove(proxyMethodFactory);
        proxyMethodFactories.add(proxyMethodFactory);
        proxyMethodFactories.sort(Crane4jGlobalSorter.INSTANCE);
    }

    /**
     * Get the proxy object of the specified operator interface that annotated by {@link Operator}.
     *
     * @param operatorType operator type
     * @return proxy object
     * @see Operator
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T get(Class<T> operatorType) {
        Asserts.isTrue(
            Objects.nonNull(operatorType) && operatorType.isInterface(),
            "the operator type [{}] must be an interface.", operatorType
        );
        Object proxy = CollectionUtils.computeIfAbsent(proxyCaches, operatorType, this::doGetProxy);
        return proxy == NULL ? null : (T) proxy;
    }

    private Object doGetProxy(Class<?> operatorType) {
        Operator annotation = annotationFinder.findAnnotation(operatorType, Operator.class);
        if (Objects.isNull(annotation)) {
            return NULL;
        }

        // get component of executor and parser
        BeanOperationExecutor executor = globalConfiguration.getBeanOperationExecutor(annotation.executor(), annotation.executorType());
        BeanOperationParser parser = globalConfiguration.getBeanOperationsParser(annotation.parser(), annotation.executorType());

        // create proxy by executor and parser
        log.debug("create operator proxy for interface [{}].", operatorType);
        OperatorProxy proxy = createOperatorProxy(operatorType, parser, executor);
        return Proxy.newProxyInstance(
            operatorType.getClassLoader(), new Class[] { operatorType, ProxiedOperator.class }, proxy
        );
    }

    private <T> OperatorProxy createOperatorProxy(
        Class<T> operatorType, BeanOperationParser beanOperationParser, BeanOperationExecutor beanOperationExecutor) {
        Map<String, MethodInvoker> proxyMethods = new HashMap<>(8);
        ReflectUtils.traverseTypeHierarchy(operatorType, type -> Stream
            .of(ReflectUtils.getDeclaredMethods(type))
            .filter(method -> !method.isDefault())
            .map(beanOperationParser::parse)
            .forEach(operations -> {
                checkOperationOfMethod(operations);
                Method method = (Method)operations.getSource();
                MethodInvoker invoker = createOperatorMethod(operations, method, beanOperationExecutor);
                // using full name to distinguishing method overloading
                proxyMethods.put(method.toString(), invoker);
            })
        );
        return doCreateOperatorProxy(operatorType, proxyMethods);
    }

    /**
     * Create proxy object for operator interface.
     *
     * @param operatorType operator type
     * @param proxyMethods proxied methods with full method names;
     * @return OperatorProxy
     */
    @NonNull
    protected <T> OperatorProxy doCreateOperatorProxy(
        Class<T> operatorType, Map<String, MethodInvoker> proxyMethods) {
        return new OperatorProxy(proxyMethods, operatorType);
    }

    private void checkOperationOfMethod(BeanOperations operations) {
        Method method = (Method)operations.getSource();
        if (method.getParameterCount() < 1) {
            throw new Crane4jException(
                "the method [{}] parameter count is less than 1.", method.getName()
            );
        }
        if (operations.isEmpty()) {
            throw new Crane4jException(
                "the method [{}] are no executable operations found.", method.getName()
            );
        }
    }

    private MethodInvoker createOperatorMethod(
        BeanOperations beanOperations, Method method, BeanOperationExecutor beanOperationExecutor) {
        return proxyMethodFactories.stream()
            .map(factory ->factory.get(beanOperations, method, beanOperationExecutor))
            .filter(Objects::nonNull)
            .findFirst()
            .orElseThrow(() -> new Crane4jException("cannot create proxy for method [{}]", method));
    }

    /**
     * Operator proxy.
     *
     * @author huangchengxing
     * @since  1.3.0
     */
    @ToString(onlyExplicitlyIncluded = true)
    @RequiredArgsConstructor
    protected static class OperatorProxy implements InvocationHandler {
        private final Map<String, MethodInvoker> proxiedMethods;
        @ToString.Include
        private final Class<?> proxyClass;
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // fix https://gitee.com/opengoofy/crane4j/issues/I8MZOK
            if (Object.class.equals(method.getDeclaringClass())) {
                return method.invoke(this, args);
            }
            MethodInvoker invoker = proxiedMethods.get(method.toString());
            if (Objects.nonNull(invoker)) {
                return invoker.invoke(proxy, args);
            }
            throw new Crane4jException(
                "method [{}] is not declaring by proxied class [{}] or Object.class", method, proxyClass
            );
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if ((o instanceof ProxiedOperator) && Proxy.isProxyClass(o.getClass())) {
                o = Proxy.getInvocationHandler(o);
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            OperatorProxy that = (OperatorProxy)o;
            return Objects.equals(proxiedMethods, that.proxiedMethods)
                && Objects.equals(proxyClass, that.proxyClass);
        }

        @Override
        public int hashCode() {
            return Objects.hash(proxiedMethods, proxyClass);
        }
    }

    /**
     * Identification interface, used to indicate that the current object is a proxy object.
     */
    public interface ProxiedOperator { }
}

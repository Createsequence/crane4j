package cn.crane4j.springboot.support;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.exception.Crane4jException;
import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.executor.handler.AssembleOperationHandler;
import cn.crane4j.core.executor.handler.DisassembleOperationHandler;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.TypeResolver;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.hutool.core.lang.Assert;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>The global configuration class implemented based on the Spring context,
 * when obtaining the required components from the instance,
 * will directly obtain the corresponding bean through the {@link ApplicationContext} held.
 *
 * @author huangchengxing
 * @see ApplicationContext
 */
@Slf4j
@RequiredArgsConstructor
public class Crane4jApplicationContext
    implements Crane4jGlobalConfiguration, SmartInitializingSingleton, DisposableBean {

    /**
     * application context
     */
    private final ApplicationContext applicationContext;

    /**
     * registered containers
     */
    @Getter
    private final Map<String, Container<?>> registeredContainers = new ConcurrentHashMap<>();

    /**
     * Get property operator.
     *
     * @return property operator
     */
    @Override
    public PropertyOperator getPropertyOperator() {
        return applicationContext.getBean(PropertyOperator.class);
    }

    /**
     * Get type resolver.
     *
     * @return type resolver
     */
    @Override
    public TypeResolver getTypeResolver() {
        return applicationContext.getBean(TypeResolver.class);
    }

    /**
     * Get data source container.
     *
     * @param namespace namespace
     * @return container
     */
    @Override
    public Container<?> getContainer(String namespace) {
        Container<?> container = registeredContainers.get(namespace);
        if (Objects.isNull(container)) {
            container = applicationContext.getBean(namespace, Container.class);
        }
        return Objects.requireNonNull(container, () -> "cannot find container [" + namespace + "]");
    }

    /**
     * Get bean operation executor.
     *
     * @param executorType executor type
     * @return executor
     */
    @Override
    public BeanOperationExecutor getBeanOperationExecutor(Class<? extends BeanOperationExecutor> executorType) {
        return applicationContext.getBean(executorType);
    }

    /**
     * Get bean operation executor.
     *
     * @param executorName executor name
     * @return executor
     */
    @Override
    public BeanOperationExecutor getBeanOperationExecutor(String executorName) {
        return applicationContext.getBean(executorName, BeanOperationExecutor.class);
    }

    /**
     * Get bean operation parser.
     *
     * @param parserType parser type
     * @return parser
     */
    @Override
    public BeanOperationParser getBeanOperationsParser(Class<? extends BeanOperationParser> parserType) {
        return applicationContext.getBean(parserType);
    }

    /**
     * Get bean operation parser.
     *
     * @param parserName parser name
     * @return parser
     */
    @Override
    public BeanOperationParser getBeanOperationsParser(String parserName) {
        return applicationContext.getBean(parserName, BeanOperationParser.class);
    }

    /**
     * Get assemble operation handler.
     *
     * @param handlerType handler type
     * @return handler
     */
    @Override
    public AssembleOperationHandler getAssembleOperationHandler(Class<? extends AssembleOperationHandler> handlerType) {
        return applicationContext.getBean(handlerType);
    }

    /**
     * Get assemble operation handler.
     *
     * @param  handlerName handler name
     * @return handler
     */
    @Override
    public AssembleOperationHandler getAssembleOperationHandler(String handlerName) {
        return applicationContext.getBean(handlerName, AssembleOperationHandler.class);
    }

    /**
     * Get disassemble operation handler.
     *
     * @param handlerType handler type
     * @return handler
     */
    @Override
    public DisassembleOperationHandler getDisassembleOperationHandler(Class<? extends DisassembleOperationHandler> handlerType) {
        return applicationContext.getBean(handlerType);
    }

    /**
     * Get disassemble operation handler.
     *
     * @param handlerName handler name
     * @return handler
     */
    @Override
    public DisassembleOperationHandler getDisassembleOperationHandler(String handlerName) {
        return applicationContext.getBean(handlerName, DisassembleOperationHandler.class);
    }

    /**
     * Register container.
     *
     * @param container container
     * @throws Crane4jException thrown when the namespace of the container has been registered
     */
    public void registerContainer(Container<?> container) {
        String namespace = container.getNamespace();
        Container<?> old = registeredContainers.put(namespace, container);
        Assert.isNull(old, () -> new Crane4jException("the container [{}] has been registered", namespace));
        log.info("register data source container [{}]", container.getNamespace());
    }

    /**
     * After Spring initializes all singleton beans,
     * register all beans that implement the {@link Container} interface with the current context.
     */
    @Override
    public void afterSingletonsInstantiated() {
        applicationContext.getBeansOfType(Container.class)
            .values().forEach(this::registerContainer);
    }

    /**
     * Clear container cache on destruction.
     */
    @Override
    public void destroy() {
        log.info("global configuration has been destroyed.");
        registeredContainers.clear();
    }
}

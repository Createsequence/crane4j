package cn.crane4j.extension.spring;

import cn.crane4j.core.cache.CacheManager;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerDefinition;
import cn.crane4j.core.container.ContainerProvider;
import cn.crane4j.core.container.DefaultContainerManager;
import cn.crane4j.core.container.lifecycle.ContainerLifecycleProcessor;
import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.executor.handler.AssembleOperationHandler;
import cn.crane4j.core.executor.handler.DisassembleOperationHandler;
import cn.crane4j.core.executor.handler.key.KeyResolver;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.parser.handler.strategy.PropertyMappingStrategy;
import cn.crane4j.core.parser.handler.strategy.PropertyMappingStrategyManager;
import cn.crane4j.core.parser.handler.strategy.SimplePropertyMappingStrategyManager;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.Crane4jGlobalSorter;
import cn.crane4j.core.support.TypeResolver;
import cn.crane4j.core.support.converter.ConverterManager;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.util.ConfigurationUtil;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.core.DecoratingProxy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;

import java.lang.reflect.AnnotatedElement;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
public class Crane4jApplicationContext extends DefaultContainerManager
    implements Crane4jGlobalConfiguration, SmartInitializingSingleton, DisposableBean, InitializingBean {

    static {
        // support compare by @Order annotation and Ordered interface
        Crane4jGlobalSorter.INSTANCE.addCompareValueExtractor(t -> {
            if (Objects.isNull(t)) {
                return null;
            }
            if (t instanceof  Ordered) {
                return ((Ordered)t).getOrder();
            }
            Integer order = findOrderFromAnnotation(t);
            if (Objects.isNull(order) && t instanceof DecoratingProxy) {
                order = findOrderFromAnnotation(((DecoratingProxy)t).getDecoratedClass());
            }
            return order;
        });
    }

    @Nullable
    private static Integer findOrderFromAnnotation(Object t) {
        if (!(t instanceof AnnotatedElement)) {
            return null;
        }
        Order annotation = AnnotatedElementUtils.findMergedAnnotation(((AnnotatedElement) t), Order.class);
        return Objects.nonNull(annotation) ? annotation.value() : null;
    }

    /**
     * application context
     */
    @Getter
    private final ApplicationContext applicationContext;

    /**
     * bean name <-> container namespace
     */
    private final BiMap<String, String> beanNameNamespaceMapping = HashBiMap.create();

    /**
     * property mapping strategy manager
     */
    @Delegate
    private final PropertyMappingStrategyManager propertyMappingStrategyManager = new SimplePropertyMappingStrategyManager();

    /**
     * cache manager map
     */
    private final Map<String, CacheManager> cacheManagerMap = new HashMap<>(8);

    /**
     * Get {@link ConverterManager}
     *
     * @return {@link ConverterManager}
     */
    @Override
    public ConverterManager getConverterManager() {
        return applicationContext.getBean(ConverterManager.class);
    }

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
     * Get type handler.
     *
     * @return type handler
     */
    @Override
    public TypeResolver getTypeResolver() {
        return applicationContext.getBean(TypeResolver.class);
    }

    /**
     * Get {@link ContainerProvider} by given name.
     *
     * @param name name
     * @return {@link ContainerProvider} instance
     */
    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T extends ContainerProvider> T getContainerProvider(String name) {
        T provider = super.getContainerProvider(name);
        return Objects.isNull(provider) && applicationContext.containsBean(name) ?
                (T)applicationContext.getBean(name, ContainerProvider.class) : provider;
    }

    /**
     * Obtaining and caching container instances from provider or definition.
     *
     * @param namespace namespace of container, which can also be the cache name for the container instance.
     * @return container instance
     * @see ContainerLifecycleProcessor#whenCreated
     */
    @Nullable
    @Override
    public <K> Container<K> getContainer(String namespace) {
        // if namespace is a bean name, convert it to namespace, otherwise use it directly
        namespace = beanNameNamespaceMapping.getOrDefault(namespace, namespace);
        return super.getContainer(namespace);
    }

    /**
     * Whether this provider has container of given {@code namespace}.
     *
     * @param namespace namespace
     * @return boolean
     */
    @Override
    public boolean containsContainer(String namespace) {
        return super.containsContainer(namespace) || applicationContext.containsBean(namespace);
    }

    /**
     * Get bean operation executor.
     *
     * @param executorName executor name
     * @param executorType executor type
     * @return executor
     */
    @NonNull
    @Override
    public BeanOperationExecutor getBeanOperationExecutor(
        @Nullable String executorName, Class<?> executorType) {
        return ConfigurationUtil.getComponentFromConfiguration(
            BeanOperationExecutor.class, executorType, executorName,
            (t, n) -> applicationContext.getBean(n, t), applicationContext::getBean
        );
    }

    /**
     * Get bean operation parser.
     *
     * @param parserName parser name
     * @param parserType parser type
     * @return parser
     */
    @NonNull
    @Override
    public BeanOperationParser getBeanOperationsParser(@Nullable String parserName, Class<?> parserType) {
        return ConfigurationUtil.getComponentFromConfiguration(
            BeanOperationParser.class, parserType, parserName,
            (t, n) -> applicationContext.getBean(n, t), applicationContext::getBean
        );
    }

    /**
     * Get assemble operation handler.
     *
     * @param handlerName handler name
     * @param handlerType handler type
     * @return handler
     */
    @NonNull
    @Override
    public AssembleOperationHandler getAssembleOperationHandler(@Nullable String handlerName, Class<?> handlerType) {
        return ConfigurationUtil.getComponentFromConfiguration(
            AssembleOperationHandler.class, handlerType, handlerName,
            (t, n) -> applicationContext.getBean(n, t), applicationContext::getBean
        );
    }

    /**
     * Get disassemble operation handler.
     *
     * @param handlerName handler name
     * @param handlerType handler type
     * @return handler
     */
    @NonNull
    @Override
    public DisassembleOperationHandler getDisassembleOperationHandler(@Nullable String handlerName, Class<?> handlerType) {
        return ConfigurationUtil.getComponentFromConfiguration(
            DisassembleOperationHandler.class, handlerType, handlerName,
            (t, n) -> applicationContext.getBean(n, t), applicationContext::getBean
        );
    }

    /**
     * Get cache factory.
     *
     * @param name cache factory name
     * @return cache factory
     * @since 2.4.0
     */
    @NonNull
    @Override
    public CacheManager getCacheManager(String name) {
        return cacheManagerMap.getOrDefault(
            name, applicationContext.getBean(name, CacheManager.class)
        );
    }

    /**
     * Register cache manager.
     *
     * @param cacheManager cache manager
     * @since 2.8.0
     */
    @Override
    public void registerCacheManager(CacheManager cacheManager) {
        cacheManagerMap.put(cacheManager.getName(), cacheManager);
    }

    /**
     * Get bean name by namespace.
     *
     * @param namespace namespace
     * @return bean name
     */
    @Nullable
    public String getBeanNameByNamespace(String namespace) {
        return beanNameNamespaceMapping.inverse().get(namespace);
    }

    /**
     * Get namespace by bean name.
     *
     * @param beanName namespace
     * @return namespace
     */
    @Nullable
    public String getNamespaceByBeanName(String beanName) {
        return beanNameNamespaceMapping.get(beanName);
    }

    /**
     * Get key resolver.
     *
     * @param resolverType resolver type
     * @return key resolver
     * @since 2.7.0
     */
    @Override
    public KeyResolver getKeyResolver(Class<? extends KeyResolver> resolverType) {
        if (applicationContext.getBeanNamesForType(resolverType).length > 0) {
            return applicationContext.getBean(resolverType);
        }
        return applicationContext.getAutowireCapableBeanFactory()
            .createBean(resolverType);
    }

    // ============================ life cycle ============================

    /**
     * Load all {@link ContainerLifecycleProcessor} from spring context.
     */
    @Override
    public void afterPropertiesSet() {
        applicationContext.getBeansOfType(ContainerLifecycleProcessor.class).forEach((name, processor) -> {
            log.info("install container lifecycle processor [{}]", name);
            registerContainerLifecycleProcessor(processor);
        });
    }

    /**
     * After Spring initializes all singleton beans,
     * register all beans that implement the {@link Container} interface with the current context.
     */
    @Override
    public void afterSingletonsInstantiated() {
        applicationContext.getBeansOfType(ContainerLifecycleProcessor.class).forEach((name, processor) -> {
            if (registerContainerLifecycleProcessor(processor)) {
                log.info("install container lifecycle processor [{}]", name);
            }
        });
        applicationContext.getBeansOfType(ContainerDefinition.class).forEach((beanName, definition) -> {
            beanNameNamespaceMapping.put(beanName, definition.getNamespace());
            log.info("register container definition bean [{}] from spring context, actual namespace is [{}]", beanName, definition.getNamespace());
            registerContainer(definition);
        });
        applicationContext.getBeansOfType(Container.class).forEach((beanName, container) -> {
            beanNameNamespaceMapping.put(beanName, container.getNamespace());
            log.info("register container bean [{}] bean from spring context, actual namespace is [{}]", beanName, container.getNamespace());
            registerContainer(container);
        });
        applicationContext.getBeansOfType(ContainerProvider.class)
            .forEach((beanName, provider) -> {
                log.info("register container provider [{}] from spring context", beanName);
                registerContainerProvider(beanName, provider);
            });
        applicationContext.getBeansOfType(PropertyMappingStrategy.class)
            .forEach((beanName, strategy) -> {
                log.info("register property mapping strategy manager [{}]({}) from spring context", beanName, strategy);
                propertyMappingStrategyManager.addPropertyMappingStrategy(strategy);
            });
    }

    /**
     * Clear container cache on destruction.
     */
    @Override
    public void destroy() {
        log.info("global configuration has been destroyed.");
        clear();
        // TODO clear propertyMappingStrategyManager and keyResolverRegistry
    }
}

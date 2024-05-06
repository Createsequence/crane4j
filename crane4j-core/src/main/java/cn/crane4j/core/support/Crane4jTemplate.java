package cn.crane4j.core.support;

import cn.crane4j.annotation.ContainerConstant;
import cn.crane4j.annotation.ContainerEnum;
import cn.crane4j.core.cache.CacheDefinition;
import cn.crane4j.core.cache.CacheManager;
import cn.crane4j.core.cache.CacheableContainer;
import cn.crane4j.core.condition.ConditionParser;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerProvider;
import cn.crane4j.core.container.Containers;
import cn.crane4j.core.container.lifecycle.ContainerLifecycleProcessor;
import cn.crane4j.core.executor.AsyncBeanOperationExecutor;
import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.executor.OrderedBeanOperationExecutor;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.ConditionalTypeHierarchyBeanOperationParser;
import cn.crane4j.core.parser.TypeHierarchyBeanOperationParser;
import cn.crane4j.core.parser.handler.AssembleMethodAnnotationHandler;
import cn.crane4j.core.parser.handler.OperationAnnotationHandler;
import cn.crane4j.core.parser.handler.strategy.PropertyMappingStrategy;
import cn.crane4j.core.parser.operation.KeyTriggerOperation;
import cn.crane4j.core.support.aop.AutoOperateProxy;
import cn.crane4j.core.support.container.ContainerMethodAnnotationProcessor;
import cn.crane4j.core.support.container.MethodContainerFactory;
import cn.crane4j.core.support.operator.OperatorProxyFactory;
import cn.crane4j.core.support.operator.OperatorProxyMethodFactory;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.util.ConfigurationUtil;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A facade service for the crane4j framework.
 *
 * @author huangchengxing
 * @since 2.8.0
 */
@Accessors(fluent = true)
@Builder
public class Crane4jTemplate {

    @Getter
    private final Crane4jGlobalConfiguration configuration;
    @Getter
    private final OperateTemplate operateTemplate;
    private final AnnotationFinder annotationFinder;
    private final PropertyOperator propertyOperator;
    private final AutoOperateProxy autoOperateProxy;
    private final OperatorProxyFactory operatorProxyFactory;
    private final ContainerMethodAnnotationProcessor containerMethodAnnotationProcessor;

    @Getter
    private final OpsForProxy opsForProxy = new OpsForProxy();
    @Getter
    private final OpsForContainer opsForContainer = new OpsForContainer();
    @Getter
    private final OpsForComponent opsForComponent = new OpsForComponent();

    /**
     * Create a {@link Crane4jTemplate} instance with default configuration.
     *
     * @return crane4j service
     * @see SimpleCrane4jGlobalConfiguration
     */
    public static Crane4jTemplate withDefaultConfiguration() {
        return withDefaultConfiguration(builder -> {});
    }

    /**
     * Create a {@link Crane4jTemplate} instance with default configuration.
     *
     * @param builderConsumer builder consumer for {@link SimpleCrane4jGlobalConfiguration.Builder}
     * @return crane4j service
     * @see SimpleCrane4jGlobalConfiguration
     */
    public static Crane4jTemplate withDefaultConfiguration(
        @NonNull Consumer<SimpleCrane4jGlobalConfiguration.Builder> builderConsumer) {
        SimpleCrane4jGlobalConfiguration.Builder confBuilder = SimpleCrane4jGlobalConfiguration.builder();
        builderConsumer.accept(confBuilder);
        SimpleCrane4jGlobalConfiguration conf = confBuilder.build();
        return Crane4jTemplate.builder()
            .configuration(conf)
            .annotationFinder(conf.getAnnotationFinder())
            .propertyOperator(conf.getPropertyOperator())
            .autoOperateProxy(ConfigurationUtil.createAutoOperateProxy(conf))
            .operateTemplate(ConfigurationUtil.createOperateTemplate(conf))
            .operatorProxyFactory(ConfigurationUtil.createOperatorProxyFactory(conf))
            .containerMethodAnnotationProcessor(ConfigurationUtil.createContainerMethodAnnotationProcessor(conf))
            .build();
    }

    /**
     * Get the bean operations from the element.
     *
     * @param element element
     * @return bean operations
     */
    public BeanOperations getBeanOperations(@NonNull AnnotatedElement element) {
        BeanOperationParser parser = configuration.getBeanOperationsParser(BeanOperationParser.class);
        return parser.parse(element);
    }

    /**
     * Execute the fill operation.
     *
     * @param targets targets
     * @param executor executor
     * @param filter filter
     */
    public void execute(
        Collection<?> targets, BeanOperationExecutor executor, Predicate<? super KeyTriggerOperation> filter) {
        operateTemplate.execute(targets, executor, filter);
    }

    /**
     * Execute the fill operation.
     *
     * @param targets targets
     * @param executor executor
     */
    public void execute(
        Collection<?> targets, BeanOperationExecutor executor) {
        execute(targets, executor, ops -> true);
    }

    /**
     * Execute the fill operation.
     *
     * @param targets targets
     * @param executorType executor type
     */
    public void execute(
        Collection<?> targets, Class<? extends BeanOperationExecutor> executorType) {
        BeanOperationExecutor executor = configuration.getBeanOperationExecutor(executorType);
        execute(targets, executor, ops -> true);
    }

    /**
     * Execute the fill operation.
     *
     * @param targets targets
     */
    public void execute(Collection<?> targets) {
        operateTemplate.execute(targets);
    }

    /**
     * Execute the fill operation.
     *
     * @param targets targets
     * @param beanOperations bean operations
     */
    public void execute(Collection<?> targets, BeanOperations beanOperations) {
        operateTemplate.execute(targets, beanOperations);
    }

    /**
     * Execute the fill operation asynchronously.
     *
     * @param targets targets
     * @param filter filter
     * @see AsyncBeanOperationExecutor
     */
    public void executeAsync(
        Collection<?> targets, Predicate<? super KeyTriggerOperation> filter) {
        BeanOperationExecutor executor = configuration.getBeanOperationExecutor(AsyncBeanOperationExecutor.class);
        operateTemplate.execute(targets, executor, filter);
    }

    /**
     * Execute the fill operation asynchronously.
     *
     * @param targets targets
     * @see AsyncBeanOperationExecutor
     */
    public void executeAsync(Collection<?> targets) {
        executeAsync(targets, ops -> true);
    }

    /**
     * Execute the fill operation in order.
     *
     * @param targets targets
     * @param filter filter
     * @see OrderedBeanOperationExecutor
     */
    public void executeOrdered(
        Collection<?> targets, Predicate<? super KeyTriggerOperation> filter) {
        BeanOperationExecutor executor = configuration.getBeanOperationExecutor(OrderedBeanOperationExecutor.class);
        operateTemplate.execute(targets, executor, filter);
    }

    /**
     * Execute the fill operation in order.
     *
     * @param targets targets
     * @see OrderedBeanOperationExecutor
     */
    public void executeOrdered(Collection<?> targets) {
        executeOrdered(targets, ops -> true);
    }

    // endregion

    /**
     * Operations for datasource containers.
     *
     * @author huangchengxing
     */
    public class OpsForContainer {

        /**
         * Get ops for execute.
         *
         * @return crane4j service
         */
        public Crane4jTemplate opsForExecute() {
            return Crane4jTemplate.this;
        }

        /**
         * Get ops for components.
         *
         * @return container configuration
         */
        public OpsForComponent opsForComponent() {
            return opsForComponent;
        }

        /**
         * Get ops for proxies.
         *
         * @return container configuration
         */
        public OpsForProxy opsForProxy() {
            return opsForProxy;
        }

        /**
         * Wrap the container with cache.
         *
         * @param container container
         * @param cacheManagerName cache manager name
         * @param expireTimeMilli expire time in milliseconds
         * @param <K> key type
         * @return cacheable container
         * @see OpsForComponent#registerCacheManager
         * @see CacheableContainer
         * @see CacheManager
         */
        public <K> Container<K> wrapContainerWithCache(
            Container<K> container, String cacheManagerName, long expireTimeMilli) {
            CacheDefinition cacheDefinition = new CacheDefinition.Impl(
                container.getNamespace(), cacheManagerName, expireTimeMilli, TimeUnit.MILLISECONDS
            );
            CacheManager cacheManager = configuration.getCacheManager(cacheDefinition.getCacheManager());
            return new CacheableContainer<>(container, cacheDefinition, cacheManager);
        }

        /**
         * Get method containers from target
         *
         * @param target target
         * @return containers
         * @see ContainerMethodAnnotationProcessor#process
         * @see OpsForComponent#registerMethodContainerFactory
         */
        public Collection<Container<Object>> getMethodContainersFromTarget(@NonNull Object target) {
            return containerMethodAnnotationProcessor.process(target, target.getClass());
        }

        /**
         * register container
         *
         * @param constantType constant type
         * @return this
         * @see Containers#forConstantClass
         * @see ContainerConstant
         */
        @NonNull
        public OpsForContainer registerConstantContainer(Class<?> constantType) {
            Container<?> container = Containers.forConstantClass(constantType, annotationFinder);
            configuration.registerContainer(container);
            return this;
        }

        /**
         * register container
         *
         * @param enumType enum type
         * @param <E> enum type
         * @return this
         * @see Containers#forEnum
         * @see ContainerEnum
         */
        @NonNull
        public <E extends Enum<E>> OpsForContainer registerEnumContainer(Class<E> enumType) {
            Container<?> container = Containers.forEnum(enumType, annotationFinder, propertyOperator);
            configuration.registerContainer(container);
            return this;
        }

        /**
         * register container
         *
         * @param namespace namespace
         * @param enumType enum type
         * @param keyMapper key mapper
         * @param valueMapper value mapper
         * @param <E> enum type
         * @param <K> key type
         * @param <V> value type
         * @return this
         */
        @NonNull
        public <E extends Enum<E>, K, V> OpsForContainer registerEnumContainer(
            @NonNull String namespace, Class<E> enumType,
            Function<E, K> keyMapper, Function<E, V> valueMapper) {
            Map<K, V> enumData = Stream.of(enumType.getEnumConstants())
                .collect(Collectors.toMap(keyMapper, valueMapper));
            registerMapContainer(namespace, enumData);
            return this;
        }

        /**
         * register container
         *
         * @param namespace namespace
         * @param enumType enum type
         * @param keyMapper key mapper
         * @param <E> enum type
         * @param <K> key type
         * @return this
         */
        @NonNull
        public <E extends Enum<E>, K> OpsForContainer registerEnumContainer(
            @NonNull String namespace, Class<E> enumType, Function<E, K> keyMapper) {
            registerEnumContainer(namespace, enumType, keyMapper, Function.identity());
            return this;
        }

        /**
         * register container
         *
         * @param namespace namespace
         * @param map data map
         * @return this
         */
        @NonNull
        public <K> OpsForContainer registerMapContainer(
            @NonNull String namespace, @NonNull Map<K, ?> map) {
            Container<K> container = Containers.forMap(namespace, map);
            configuration.registerContainer(container);
            return this;
        }

        /**
         * register container
         *
         * @param namespace namespace
         * @param datasource data source
         * @param keyMapper key mapper
         * @param valueMapper value mapper
         * @param <T> data source type
         * @param <K> key type
         * @param <V> value type
         * @return this
         */
        @NonNull
        public <T, K, V> OpsForContainer registerMapContainer(
            @NonNull String namespace, @NonNull Collection<T> datasource,
            Function<T, K> keyMapper, Function<T, V> valueMapper) {
            Map<K, V> map = datasource.stream()
                .collect(Collectors.toMap(keyMapper, valueMapper));
            return registerMapContainer(namespace, map);
        }

        /**
         * register container
         *
         * @param namespace namespace
         * @param dataProvider a lambda function that returns the data source object
         * @return this
         * @see Crane4jGlobalConfiguration#registerContainer
         */
        @NonNull
        public <K> OpsForContainer registerLambdaContainer(
            @NonNull String namespace, DataProvider<K, ?> dataProvider) {
            Container<K> container = Containers.forLambda(namespace, dataProvider);
            configuration.registerContainer(container);
            return this;
        }

        /**
         * register method containers
         *
         * @param target target
         * @return this
         * @see ContainerMethodAnnotationProcessor#process
         * @see OpsForComponent#registerMethodContainerFactory
         * @see #registerContainer
         */
        @NonNull
        public OpsForContainer registerMethodContainers(@NonNull Object target) {
            Collection<Container<Object>> containers = getMethodContainersFromTarget(target);
            containers.forEach(configuration::registerContainer);
            return this;
        }

        /**
         * register container
         *
         * @param container container
         * @return this
         * @see Crane4jGlobalConfiguration#registerContainer
         */
        @NonNull
        public OpsForContainer registerContainer(Container<?> container) {
            configuration.registerContainer(container);
            return this;
        }

        /**
         * get container
         *
         * @param namespace namespace
         * @return container
         * @see Crane4jGlobalConfiguration#getContainer
         */
        @Nullable
        public Container<Object> getContainer(String namespace) {
            return configuration.getContainer(namespace);
        }

        /**
         * register container provider.
         *
         * @param providerName provider name
         * @param containerProvider container provider
         * @see Crane4jGlobalConfiguration#registerContainerProvider
         */
        public OpsForContainer registerContainerProvider(
            String providerName, @NonNull ContainerProvider containerProvider) {
            configuration.registerContainerProvider(providerName, containerProvider);
            return this;
        }

        /**
         * configure container provider.
         *
         * @param providerName provider name
         * @param provider provider
         * @param consumer consumer
         * @param <P> provider type
         * @return this
         */
        public <P extends ContainerProvider> OpsForContainer configureContainerProvider(
            String providerName, Class<P> provider, BiConsumer<Crane4jTemplate, ContainerProvider> consumer) {
            Optional.ofNullable(configuration.getContainerProvider(providerName))
                .filter(provider::isInstance)
                .map(provider::cast)
                .ifPresent(p -> consumer.accept(Crane4jTemplate.this, p));
            return this;
        }

        /**
         * get container provider.
         *
         * @param providerName provider name
         * @return container provider
         * @see Crane4jGlobalConfiguration#getContainerProvider
         */
        @Nullable
        public ContainerProvider getContainerProvider(String providerName) {
            return configuration.getContainerProvider(providerName);
        }
    }

    /**
     * Operations for components.
     *
     * @author huangchengxing
     */
    public class OpsForComponent {

        /**
         * Get ops for execute.
         *
         * @return crane4j service
         */
        public Crane4jTemplate opsForExecute() {
            return Crane4jTemplate.this;
        }

        /**
         * Get ops for proxies.
         *
         * @return container configuration
         */
        public OpsForProxy opsForProxy() {
            return opsForProxy;
        }

        /**
         * Get ops for container.
         *
         * @return container configuration
         */
        public OpsForContainer opsForContainer() {
            return opsForContainer;
        }

        /**
         * register method containers
         *
         * @param methodContainerFactory method container factory
         * @return this
         * @see AssembleMethodAnnotationHandler#registerMethodContainerFactory
         */
        public OpsForComponent registerMethodContainerFactory(@NonNull MethodContainerFactory methodContainerFactory) {
            containerMethodAnnotationProcessor.registerMethodContainerFactory(methodContainerFactory);
            configureOperationAnnotationHandler(
                AssembleMethodAnnotationHandler.class,
                (service, handler) -> handler.registerMethodContainerFactory(methodContainerFactory)
            );
            return this;
        }

        /**
         * Register property mapping strategy.
         *
         * @param propertyMappingStrategy property mapping strategy
         * @return this
         * @see Crane4jGlobalConfiguration#addPropertyMappingStrategy
         */
        public OpsForComponent registerPropertyMappingStrategy(@NonNull PropertyMappingStrategy propertyMappingStrategy) {
            configuration.addPropertyMappingStrategy(propertyMappingStrategy);
            return this;
        }

        /**
         * Register {@link ContainerLifecycleProcessor}.
         *
         * @param lifecycle lifecycle
         * @return this
         * @see Crane4jGlobalConfiguration#registerContainerLifecycleProcessor
         */
        public OpsForComponent registerContainerLifecycleProcessor(@NonNull ContainerLifecycleProcessor lifecycle) {
            configuration.registerContainerLifecycleProcessor(lifecycle);
            return this;
        }

        /**
         * Register operation annotation handler.
         *
         * @param operationAnnotationHandler operation annotation handler
         * @return this
         * @see TypeHierarchyBeanOperationParser#addOperationAnnotationHandler
         */
        public OpsForComponent registerOperationAnnotationHandler(OperationAnnotationHandler operationAnnotationHandler) {
            Optional.of(configuration.getBeanOperationsParser(TypeHierarchyBeanOperationParser.class))
                .map(TypeHierarchyBeanOperationParser.class::cast)
                .ifPresent(parser -> parser.addOperationAnnotationHandler(operationAnnotationHandler));
            return this;
        }

        /**
         * Register condition parser.
         *
         * @param conditionParser condition parser
         * @return this
         * @see ConditionalTypeHierarchyBeanOperationParser#registerConditionParser
         */
        public OpsForComponent registerConditionParser(ConditionParser conditionParser) {
            Optional.of(configuration.getBeanOperationsParser(ConditionalTypeHierarchyBeanOperationParser.class))
                .map(ConditionalTypeHierarchyBeanOperationParser.class::cast)
                .ifPresent(parser -> parser.registerConditionParser(conditionParser));
            return this;
        }

        /**
         * Register container provider.
         *
         * @param cacheManager cache manager
         * @see Crane4jGlobalConfiguration#registerCacheManager
         */
        public OpsForComponent registerCacheManager(CacheManager cacheManager) {
            configuration.registerCacheManager(cacheManager);
            return this;
        }

        /**
         * Get cache manager.
         *
         * @param name cache manager name
         * @return cache manager
         * @see Crane4jGlobalConfiguration#getCacheManager
         */
        public CacheManager getCacheManager(String name) {
            return configuration.getCacheManager(name);
        }

        /**
         * Configure cache manager.
         *
         * @param name cache manager name
         * @param managerType cache manager type
         * @param consumer consumer
         * @param <M> cache manager type
         * @return this
         */
        public <M extends CacheManager> OpsForComponent configureCacheManager(
            String name, Class<M> managerType, BiConsumer<Crane4jTemplate, M> consumer) {
            Optional.of(configuration.getCacheManager(name))
                .filter(managerType::isInstance)
                .map(managerType::cast)
                .ifPresent(manager -> consumer.accept(Crane4jTemplate.this, manager));
            return this;
        }

        /**
         * Configure operation annotation handler.
         *
         * @param handlerType handler type
         * @param consumer consumer
         * @return this
         * @see TypeHierarchyBeanOperationParser#getOperationAnnotationHandlers
         */
        public <H extends OperationAnnotationHandler> OpsForComponent configureOperationAnnotationHandler(
            Class<H> handlerType, BiConsumer<Crane4jTemplate, H> consumer) {
            Optional.of(configuration.getBeanOperationsParser(TypeHierarchyBeanOperationParser.class))
                .map(TypeHierarchyBeanOperationParser.class::cast)
                .map(TypeHierarchyBeanOperationParser::getOperationAnnotationHandlers)
                .map(handlers -> handlers.stream().filter(handlerType::isInstance).map(handlerType::cast))
                .ifPresent(handlers -> handlers.forEach(h -> consumer.accept(Crane4jTemplate.this, h)));
            return this;
        }
    }

    /**
     * Operations for proxies.
     *
     * @author huangchengxing
     */
    public class OpsForProxy {

        /**
         * Get ops for execute.
         *
         * @return crane4j service
         */
        public Crane4jTemplate opsForExecute() {
            return Crane4jTemplate.this;
        }

        /**
         * Get ops for components.
         *
         * @return container configuration
         */
        public OpsForComponent opsForComponent() {
            return opsForComponent;
        }

        /**
         * Get ops for container.
         *
         * @return container configuration
         */
        public OpsForContainer opsForContainer() {
            return opsForContainer;
        }

        /**
         * <p>Create a proxy instance for the target object
         * if the target object has methods annotated with {@link cn.crane4j.annotation.AutoOperate}.<br/>
         * When calling the annotated method of the target object,
         * the method will be automatically filled with the necessary parameters or return values.
         *
         * @param target target object
         * @param <T> target type
         * @return proxy instance if it has method annotated with {@link cn.crane4j.annotation.AutoOperate},
         * otherwise return the original target object.
         * @see AutoOperateProxy#wrapIfNecessary
         */
        @NonNull
        public <T> T createAutoOperateProxy(@NonNull T target) {
            return autoOperateProxy.wrapIfNecessary(target);
        }

        /**
         * <p>Create a proxy instance for the specified operator interface.
         *
         * @param operator operator
         * @param <T> operator type
         * @return operator proxy
         * @see OperatorProxyFactory#get
         * @see OpsForProxy#registerOperatorProxyMethodFactory
         */
        public <T> T createOperatorProxy(@NonNull Class<T> operator) {
            return operatorProxyFactory.get(operator);
        }

        /**
         * Register operator proxy method factory.
         *
         * @param proxyMethodFactory proxy method factory
         * @return this
         * @see OperatorProxyFactory#addProxyMethodFactory
         */
        public OpsForProxy registerOperatorProxyMethodFactory(@NonNull OperatorProxyMethodFactory proxyMethodFactory) {
            operatorProxyFactory.addProxyMethodFactory(proxyMethodFactory);
            return this;
        }
    }
}

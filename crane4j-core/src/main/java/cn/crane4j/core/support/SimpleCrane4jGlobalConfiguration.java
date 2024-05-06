package cn.crane4j.core.support;

import cn.crane4j.core.cache.AbstractMapCacheManager;
import cn.crane4j.core.cache.CacheManager;
import cn.crane4j.core.cache.GuavaCacheManager;
import cn.crane4j.core.condition.ConditionOnContainerParser;
import cn.crane4j.core.condition.ConditionOnPropertyNotEmptyParser;
import cn.crane4j.core.condition.ConditionOnPropertyNotNullParser;
import cn.crane4j.core.condition.ConditionOnPropertyParser;
import cn.crane4j.core.condition.ConditionOnTargetTypeParser;
import cn.crane4j.core.container.ContainerProvider;
import cn.crane4j.core.container.DefaultContainerManager;
import cn.crane4j.core.container.lifecycle.ContainerRegisterLogger;
import cn.crane4j.core.executor.AsyncBeanOperationExecutor;
import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.executor.DisorderedBeanOperationExecutor;
import cn.crane4j.core.executor.OrderedBeanOperationExecutor;
import cn.crane4j.core.executor.handler.AssembleOperationHandler;
import cn.crane4j.core.executor.handler.DisassembleOperationHandler;
import cn.crane4j.core.executor.handler.ManyToManyAssembleOperationHandler;
import cn.crane4j.core.executor.handler.OneToManyAssembleOperationHandler;
import cn.crane4j.core.executor.handler.OneToOneAssembleOperationHandler;
import cn.crane4j.core.executor.handler.ReflectiveDisassembleOperationHandler;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.parser.ConditionalTypeHierarchyBeanOperationParser;
import cn.crane4j.core.parser.TypeHierarchyBeanOperationParser;
import cn.crane4j.core.parser.handler.AssembleAnnotationHandler;
import cn.crane4j.core.parser.handler.AssembleConstantAnnotationHandler;
import cn.crane4j.core.parser.handler.AssembleEnumAnnotationHandler;
import cn.crane4j.core.parser.handler.AssembleKeyAnnotationHandler;
import cn.crane4j.core.parser.handler.AssembleMethodAnnotationHandler;
import cn.crane4j.core.parser.handler.DisassembleAnnotationHandler;
import cn.crane4j.core.parser.handler.strategy.OverwriteMappingStrategy;
import cn.crane4j.core.parser.handler.strategy.OverwriteNotNullMappingStrategy;
import cn.crane4j.core.parser.handler.strategy.PropertyMappingStrategyManager;
import cn.crane4j.core.parser.handler.strategy.ReferenceMappingStrategy;
import cn.crane4j.core.parser.handler.strategy.SimplePropertyMappingStrategyManager;
import cn.crane4j.core.support.container.DefaultMethodContainerFactory;
import cn.crane4j.core.support.container.MethodInvokerContainerCreator;
import cn.crane4j.core.support.converter.ConverterManager;
import cn.crane4j.core.support.converter.HutoolConverterManager;
import cn.crane4j.core.support.reflect.CacheablePropertyOperator;
import cn.crane4j.core.support.reflect.ChainAccessiblePropertyOperator;
import cn.crane4j.core.support.reflect.MapAccessiblePropertyOperator;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.support.reflect.PropertyOperatorHolder;
import cn.crane4j.core.support.reflect.ReflectivePropertyOperator;
import cn.crane4j.core.util.Asserts;
import cn.crane4j.core.util.ConfigurationUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.Delegate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Basic implementation of {@link Crane4jGlobalConfiguration}.
 *
 * @author huangchengxing
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class SimpleCrane4jGlobalConfiguration
    extends DefaultContainerManager implements Crane4jGlobalConfiguration {

    private final AnnotationFinder annotationFinder;
    @Setter
    private TypeResolver typeResolver;
    @Setter
    private PropertyOperator propertyOperator;
    @Setter
    private ConverterManager converterManager;
    private final Map<String, BeanOperationParser> beanOperationParserMap = new HashMap<>(16);
    private final Map<String, AssembleOperationHandler> assembleOperationHandlerMap = new HashMap<>(4);
    private final Map<String, DisassembleOperationHandler> disassembleOperationHandlerMap = new HashMap<>(4);
    private final Map<String, BeanOperationExecutor> beanOperationExecutorMap = new HashMap<>(4);
    private final Map<String, CacheManager> cacheManagerMap = new HashMap<>(4);
    @Delegate
    private final PropertyMappingStrategyManager propertyMappingStrategyManager = new SimplePropertyMappingStrategyManager();

    /**
     * Create a {@link SimpleCrane4jGlobalConfiguration} using the default configuration.
     *
     * @return configuration
     * @deprecated use {@link #builder()}
     */
    @Deprecated
    public static SimpleCrane4jGlobalConfiguration create() {
        return builder().build();
    }

    /**
     * Create a {@link SimpleCrane4jGlobalConfiguration} using the default configuration.
     *
     * @param annotationFinder annotation finder
     * @param converter converter manager
     * @param operator property operator
     * @return configuration
     * @deprecated use {@link #builder()}
     */
    @Deprecated
    public static SimpleCrane4jGlobalConfiguration create(
        AnnotationFinder annotationFinder, ConverterManager converter, PropertyOperator operator) {
        return builder()
            .annotationFinder(annotationFinder)
            .converterManager(converter)
            .propertyOperator(operator)
            .build();
    }

    /**
     * Create a {@link SimpleCrane4jGlobalConfiguration} builder.
     *
     * @return builder
     */
    public static Builder builder() {
        return new Builder();
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
        BeanOperationExecutor executor = ConfigurationUtil.getComponentFromConfiguration(
            BeanOperationExecutor.class, executorType, executorName,
            (t, n) -> {
                BeanOperationExecutor r = beanOperationExecutorMap.get(n);
                return t.isAssignableFrom(r.getClass()) ? r : null;
            },
            t -> beanOperationExecutorMap.get(t.getSimpleName())
        );
        Asserts.isNotNull(executor, "cannot find executor [{}]({})", executorName, executorType);
        return executor;
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
        BeanOperationParser parser = ConfigurationUtil.getComponentFromConfiguration(
            BeanOperationParser.class, parserType, parserName,
            (t, n) -> {
                BeanOperationParser r = beanOperationParserMap.get(n);
                return t.isAssignableFrom(r.getClass()) ? r : null;
            },
            t -> beanOperationParserMap.get(t.getSimpleName())
        );
        Asserts.isNotNull(parser, "cannot find parser [{}]({})", parserName, parserType);
        return parser;
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
        AssembleOperationHandler parser = ConfigurationUtil.getComponentFromConfiguration(
            AssembleOperationHandler.class, handlerType, handlerName,
            (t, n) -> {
                AssembleOperationHandler r = assembleOperationHandlerMap.get(n);
                return t.isAssignableFrom(r.getClass()) ? r : null;
            },
            t -> assembleOperationHandlerMap.get(t.getSimpleName())
        );
        Asserts.isNotNull(parser, "cannot find assemble handler [{}]({})", handlerName, handlerType);
        return parser;
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
    public DisassembleOperationHandler getDisassembleOperationHandler(@Nullable String handlerName, Class<?> handlerType) {
        DisassembleOperationHandler parser = ConfigurationUtil.getComponentFromConfiguration(
            DisassembleOperationHandler.class, handlerType, handlerName,
            (t, n) -> {
                DisassembleOperationHandler r = disassembleOperationHandlerMap.get(n);
                return t.isAssignableFrom(r.getClass()) ? r : null;
            },
            t -> disassembleOperationHandlerMap.get(t.getSimpleName())
        );
        Asserts.isNotNull(parser, "cannot find disassemble handler [{}]({})", handlerName, handlerType);
        return parser;
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
        CacheManager factory = cacheManagerMap.get(name);
        Asserts.isNotNull(factory, "cannot find cache manager [{}]", name);
        return factory;
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
     * Builder for {@link SimpleCrane4jGlobalConfiguration}.
     *
     * @author huangchengxing
     */
    @Accessors(chain = true, fluent = true)
    @Setter
    public static class Builder {
        @NonNull
        private AnnotationFinder annotationFinder = SimpleAnnotationFinder.INSTANCE;
        @NonNull
        private ConverterManager converterManager = HutoolConverterManager.INSTANCE;
        @NonNull
        private PropertyOperator propertyOperator = new ReflectivePropertyOperator(HutoolConverterManager.INSTANCE);
        private boolean enablePropertyOperatorDecorator = true;
        @NonNull
        private TypeResolver typeResolver = new SimpleTypeResolver();
        public SimpleCrane4jGlobalConfiguration build() {
            if (enablePropertyOperatorDecorator) {
                propertyOperator = new CacheablePropertyOperator(propertyOperator);
                propertyOperator = new MapAccessiblePropertyOperator(propertyOperator);
                propertyOperator = new ChainAccessiblePropertyOperator(propertyOperator);
                propertyOperator = new PropertyOperatorHolder(propertyOperator);
            }
            SimpleCrane4jGlobalConfiguration configuration = new SimpleCrane4jGlobalConfiguration(annotationFinder, typeResolver, propertyOperator, converterManager);
            initDefaultContainerComponents(configuration);
            initDefaultParserComponents(configuration);
            initDefaultExecutorComponents(configuration);
            initDefaultCacheComponents(configuration);
            return configuration;
        }

        private static void initDefaultCacheComponents(SimpleCrane4jGlobalConfiguration configuration) {
            // cache manager
            GuavaCacheManager guavaCacheManager = new GuavaCacheManager();
            configuration.getCacheManagerMap()
                .put(guavaCacheManager.getName(), guavaCacheManager);
            AbstractMapCacheManager mapCacheManager = AbstractMapCacheManager.newWeakConcurrentMapCacheManager();
            configuration.getCacheManagerMap()
                .put(mapCacheManager.getName(), mapCacheManager);
        }

        private static void initDefaultContainerComponents(SimpleCrane4jGlobalConfiguration configuration) {
            // container container lifecycle lifecycle
            Logger logger = LoggerFactory.getLogger(ContainerRegisterLogger.class);
            configuration.registerContainerLifecycleProcessor(new ContainerRegisterLogger(logger::info));
            // container providers
            configuration.registerContainerProvider(configuration.getClass().getSimpleName(), configuration);
            configuration.registerContainerProvider(ContainerProvider.class.getSimpleName(), configuration);
        }

        private void initDefaultParserComponents(SimpleCrane4jGlobalConfiguration configuration) {
            // parser and condition parser
            ConditionalTypeHierarchyBeanOperationParser beanOperationParser = new ConditionalTypeHierarchyBeanOperationParser();
            beanOperationParser.registerConditionParser(new ConditionOnPropertyParser(annotationFinder, propertyOperator, converterManager));
            beanOperationParser.registerConditionParser(new ConditionOnPropertyNotNullParser(annotationFinder, propertyOperator));
            beanOperationParser.registerConditionParser(new ConditionOnPropertyNotEmptyParser(annotationFinder, propertyOperator));
            beanOperationParser.registerConditionParser(new ConditionOnTargetTypeParser(annotationFinder));
            beanOperationParser.registerConditionParser(new ConditionOnContainerParser(annotationFinder, configuration));
            configuration.getBeanOperationParserMap().put(BeanOperationParser.class.getSimpleName(), beanOperationParser);
            configuration.getBeanOperationParserMap().put(TypeHierarchyBeanOperationParser.class.getSimpleName(), beanOperationParser);
            configuration.getBeanOperationParserMap().put(beanOperationParser.getName(), beanOperationParser);

            // annotation handler
            AssembleAnnotationHandler assembleAnnotationHandler = new AssembleAnnotationHandler(annotationFinder, configuration, configuration);
            beanOperationParser.addOperationAnnotationHandler(assembleAnnotationHandler);
            AssembleEnumAnnotationHandler assembleEnumAnnotationHandler = new AssembleEnumAnnotationHandler(annotationFinder, configuration, propertyOperator, configuration);
            beanOperationParser.addOperationAnnotationHandler(assembleEnumAnnotationHandler);
            DisassembleAnnotationHandler disassembleAnnotationHandler = new DisassembleAnnotationHandler(annotationFinder, configuration);
            beanOperationParser.addOperationAnnotationHandler(disassembleAnnotationHandler);
            MethodInvokerContainerCreator creator = new MethodInvokerContainerCreator(
                configuration.getPropertyOperator(), configuration.getConverterManager()
            );
            AssembleMethodAnnotationHandler annotationHandler = new AssembleMethodAnnotationHandler(annotationFinder, configuration,
                Collections.singletonList(new DefaultMethodContainerFactory(creator, annotationFinder)), new SimplePropertyMappingStrategyManager()
            );
            beanOperationParser.addOperationAnnotationHandler(annotationHandler);
            AssembleConstantAnnotationHandler assembleConstantAnnotationHandler = new AssembleConstantAnnotationHandler(
                annotationFinder, configuration, configuration
            );
            beanOperationParser.addOperationAnnotationHandler(assembleConstantAnnotationHandler);
            AssembleKeyAnnotationHandler assembleKeyAnnotationHandler = new AssembleKeyAnnotationHandler(
                annotationFinder, configuration, configuration
            );
            beanOperationParser.addOperationAnnotationHandler(assembleKeyAnnotationHandler);
        }

        private void initDefaultExecutorComponents(SimpleCrane4jGlobalConfiguration configuration) {
            // operation executor
            DisorderedBeanOperationExecutor disorderedBeanOperationExecutor = new DisorderedBeanOperationExecutor(configuration);
            configuration.getBeanOperationExecutorMap().put(BeanOperationExecutor.class.getSimpleName(), disorderedBeanOperationExecutor);
            configuration.getBeanOperationExecutorMap().put(disorderedBeanOperationExecutor.getName(), disorderedBeanOperationExecutor);
            OrderedBeanOperationExecutor orderedBeanOperationExecutor = new OrderedBeanOperationExecutor(configuration, Crane4jGlobalSorter.comparator());
            configuration.getBeanOperationExecutorMap().put(orderedBeanOperationExecutor.getName(), orderedBeanOperationExecutor);

            // create default thread pool executor for async executor
            int corePoolSize = Runtime.getRuntime().availableProcessors();
            ThreadPoolExecutor executor = new ThreadPoolExecutor(
                corePoolSize * 2, corePoolSize * 2,
                10L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1), new ThreadPoolExecutor.CallerRunsPolicy()
            );
            executor.allowCoreThreadTimeOut(true);
            AsyncBeanOperationExecutor asyncBeanOperationExecutor = new AsyncBeanOperationExecutor(configuration, executor);
            configuration.getBeanOperationExecutorMap().put(asyncBeanOperationExecutor.getName(), asyncBeanOperationExecutor);

            // property mapping strategy
            configuration.addPropertyMappingStrategy(OverwriteMappingStrategy.INSTANCE);
            configuration.addPropertyMappingStrategy(OverwriteNotNullMappingStrategy.INSTANCE);
            configuration.addPropertyMappingStrategy(new ReferenceMappingStrategy(propertyOperator));

            // operation handlers
            OneToOneAssembleOperationHandler oneToOneReflexAssembleOperationHandler = new OneToOneAssembleOperationHandler(propertyOperator, converterManager);
            configuration.getAssembleOperationHandlerMap().put(AssembleOperationHandler.class.getSimpleName(), oneToOneReflexAssembleOperationHandler);
            configuration.getAssembleOperationHandlerMap().put(oneToOneReflexAssembleOperationHandler.getName(), oneToOneReflexAssembleOperationHandler);
            OneToManyAssembleOperationHandler oneToManyReflexAssembleOperationHandler = new OneToManyAssembleOperationHandler(propertyOperator, converterManager);
            configuration.getAssembleOperationHandlerMap().put(oneToManyReflexAssembleOperationHandler.getName(), oneToManyReflexAssembleOperationHandler);
            ManyToManyAssembleOperationHandler manyToManyReflexAssembleOperationHandler = new ManyToManyAssembleOperationHandler(propertyOperator, converterManager);
            configuration.getAssembleOperationHandlerMap().put(manyToManyReflexAssembleOperationHandler.getName(), manyToManyReflexAssembleOperationHandler);
            ReflectiveDisassembleOperationHandler reflectiveDisassembleOperationHandler = new ReflectiveDisassembleOperationHandler(propertyOperator);
            configuration.getDisassembleOperationHandlerMap().put(DisassembleOperationHandler.class.getSimpleName(), reflectiveDisassembleOperationHandler);
            configuration.getDisassembleOperationHandlerMap().put(reflectiveDisassembleOperationHandler.getName(), reflectiveDisassembleOperationHandler);
        }
    }
}

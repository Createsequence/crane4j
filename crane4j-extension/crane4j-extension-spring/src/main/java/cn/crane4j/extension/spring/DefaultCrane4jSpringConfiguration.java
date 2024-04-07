package cn.crane4j.extension.spring;

import cn.crane4j.core.cache.GuavaCacheManager;
import cn.crane4j.core.cache.MapCacheManager;
import cn.crane4j.core.condition.ConditionOnContainerParser;
import cn.crane4j.core.condition.ConditionOnExpressionParser;
import cn.crane4j.core.condition.ConditionOnPropertyNotEmptyParser;
import cn.crane4j.core.condition.ConditionOnPropertyNotNullParser;
import cn.crane4j.core.condition.ConditionOnPropertyParser;
import cn.crane4j.core.condition.ConditionOnTargetTypeParser;
import cn.crane4j.core.condition.ConditionParser;
import cn.crane4j.core.container.ContainerManager;
import cn.crane4j.core.container.lifecycle.ContainerInstanceLifecycleProcessor;
import cn.crane4j.core.container.lifecycle.ContainerRegisterLogger;
import cn.crane4j.core.executor.AsyncBeanOperationExecutor;
import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.executor.DisorderedBeanOperationExecutor;
import cn.crane4j.core.executor.OrderedBeanOperationExecutor;
import cn.crane4j.core.executor.handler.ManyToManyAssembleOperationHandler;
import cn.crane4j.core.executor.handler.OneToManyAssembleOperationHandler;
import cn.crane4j.core.executor.handler.OneToOneAssembleOperationHandler;
import cn.crane4j.core.executor.handler.ReflectiveDisassembleOperationHandler;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.parser.ConditionalTypeHierarchyBeanOperationParser;
import cn.crane4j.core.parser.handler.AssembleConstantAnnotationHandler;
import cn.crane4j.core.parser.handler.AssembleEnumAnnotationHandler;
import cn.crane4j.core.parser.handler.AssembleKeyAnnotationHandler;
import cn.crane4j.core.parser.handler.DisassembleAnnotationHandler;
import cn.crane4j.core.parser.handler.OperationAnnotationHandler;
import cn.crane4j.core.parser.handler.strategy.OverwriteMappingStrategy;
import cn.crane4j.core.parser.handler.strategy.OverwriteNotNullMappingStrategy;
import cn.crane4j.core.parser.handler.strategy.PropertyMappingStrategy;
import cn.crane4j.core.parser.handler.strategy.PropertyMappingStrategyManager;
import cn.crane4j.core.parser.handler.strategy.ReferenceMappingStrategy;
import cn.crane4j.core.parser.handler.strategy.SimplePropertyMappingStrategyManager;
import cn.crane4j.core.parser.operation.AssembleOperation;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.ContainerAdapterRegister;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.DefaultContainerAdapterRegister;
import cn.crane4j.core.support.OperateTemplate;
import cn.crane4j.core.support.ParameterNameFinder;
import cn.crane4j.core.support.SimpleTypeResolver;
import cn.crane4j.core.support.TypeResolver;
import cn.crane4j.core.support.auto.AutoOperateAnnotatedElementResolver;
import cn.crane4j.core.support.auto.ClassBasedAutoOperateAnnotatedElementResolver;
import cn.crane4j.core.support.auto.ComposableAutoOperateAnnotatedElementResolver;
import cn.crane4j.core.support.auto.MethodBasedAutoOperateAnnotatedElementResolver;
import cn.crane4j.core.support.container.CacheableMethodContainerFactory;
import cn.crane4j.core.support.container.MethodContainerFactory;
import cn.crane4j.core.support.container.MethodInvokerContainerCreator;
import cn.crane4j.core.support.converter.ConverterManager;
import cn.crane4j.core.support.expression.ExpressionEvaluator;
import cn.crane4j.core.support.expression.MethodBasedExpressionEvaluator;
import cn.crane4j.core.support.operator.ArgAutoOperateProxyMethodFactory;
import cn.crane4j.core.support.operator.DynamicContainerOperatorProxyMethodFactory;
import cn.crane4j.core.support.operator.OperationAnnotationProxyMethodFactory;
import cn.crane4j.core.support.operator.OperatorProxyFactory;
import cn.crane4j.core.support.operator.OperatorProxyMethodFactory;
import cn.crane4j.core.support.operator.ParametersFillProxyMethodFactory;
import cn.crane4j.core.support.reflect.CacheablePropertyOperator;
import cn.crane4j.core.support.reflect.ChainAccessiblePropertyOperator;
import cn.crane4j.core.support.reflect.MapAccessiblePropertyOperator;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.support.reflect.PropertyOperatorHolder;
import cn.crane4j.core.support.reflect.ReflectivePropertyOperator;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.extension.spring.aop.MethodArgumentAutoOperateAdvisor;
import cn.crane4j.extension.spring.aop.MethodResultAutoOperateAdvisor;
import cn.crane4j.extension.spring.expression.SpelExpressionContext;
import cn.crane4j.extension.spring.expression.SpelExpressionEvaluator;
import cn.crane4j.extension.spring.scanner.ClassScanner;
import cn.crane4j.extension.spring.scanner.ScannedContainerRegistrar;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.expression.BeanResolver;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Default configuration class for crane4j.
 *
 * @author huangchengxing
 */
@Slf4j
@EnableAspectJAutoProxy
public class DefaultCrane4jSpringConfiguration implements SmartInitializingSingleton {

    // ============== basic components ==============

    @Bean
    public NamedComponentAliasProcessor namedComponentAliasProcessor() {
        return new NamedComponentAliasProcessor();
    }

    @Bean
    public ClassScanner classScanner() {
        return ClassScanner.INSTANCE;
    }

    @Bean
    public SpringConverterManager springConverterManager() {
        return new SpringConverterManager(DefaultConversionService.getSharedInstance());
    }

    @Primary
    @Bean
    public Crane4jApplicationContext crane4jApplicationContext(ApplicationContext applicationContext) {
        return new Crane4jApplicationContext(applicationContext);
    }

    @Bean
    public ScannedContainerRegistrar scannedContainerRegister() {
        return new ScannedContainerRegistrar();
    }

    @Bean
    public PropertyOperator propertyOperator(ConverterManager converterManager) {
        PropertyOperator operator = new ReflectivePropertyOperator(converterManager);
        operator = new CacheablePropertyOperator(operator);
        operator = new MapAccessiblePropertyOperator(operator);
        operator = new ChainAccessiblePropertyOperator(operator);
        return new PropertyOperatorHolder(operator);
    }

    @Bean
    public MergedAnnotationFinder mergedAnnotationFinder() {
        return new MergedAnnotationFinder();
    }

    @Bean
    public SimpleTypeResolver simpleTypeResolver() {
        return new SimpleTypeResolver();
    }

    @Bean
    public SpelExpressionEvaluator spelExpressionEvaluator() {
        return new SpelExpressionEvaluator(new SpelExpressionParser());
    }

    @Primary
    @Bean
    public MapCacheManager mapCacheManager() {
        return MapCacheManager.newWeakConcurrentMapCacheManager();
    }

    @Bean
    public GuavaCacheManager guavaCacheManager() {
        return new GuavaCacheManager();
    }


    @Order(0)
    @Bean
    public ContainerInstanceLifecycleProcessor containerInstanceLifecycleProcessor() {
        return new ContainerInstanceLifecycleProcessor();
    }

    @Order(1)
    @Bean
    public ContainerRegisterLogger containerRegisterLogger() {
        Logger logger = LoggerFactory.getLogger(ContainerRegisterLogger.class);
        return new ContainerRegisterLogger(logger::info);
    }

    @Order(3)
    @Bean
    public SpringCacheableContainerProcessor springCacheableContainerProcessor(Crane4jApplicationContext configuration) {
        return new SpringCacheableContainerProcessor(configuration);
    }

    // ============== execute components ==============

    @Bean
    public SimplePropertyMappingStrategyManager simplePropertyMappingStrategyManager(Collection<PropertyMappingStrategy> propertyMappingStrategies) {
        SimplePropertyMappingStrategyManager manager = new SimplePropertyMappingStrategyManager();
        propertyMappingStrategies.forEach(manager::addPropertyMappingStrategy);
        return manager;
    }

    @Bean
    public OverwriteNotNullMappingStrategy overwriteNotNullMappingStrategy() {
        return OverwriteNotNullMappingStrategy.INSTANCE;
    }

    @Bean
    public OverwriteMappingStrategy overwriteMappingStrategy() {
        return OverwriteMappingStrategy.INSTANCE;
    }

    @Bean
    public ReferenceMappingStrategy referenceMappingStrategy(PropertyOperator propertyOperator) {
        return new ReferenceMappingStrategy(propertyOperator);
    }

    @Bean
    public BeanFactoryResolver beanFactoryResolver(ApplicationContext applicationContext) {
        return new BeanFactoryResolver(applicationContext);
    }

    @Bean
    public ValueResolveAssembleAnnotationHandler valueResolveAssembleAnnotationHandler(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration configuration,
        ExpressionEvaluator evaluator, BeanResolver beanResolver,
        PropertyMappingStrategyManager propertyMappingStrategyManager) {
        return new ValueResolveAssembleAnnotationHandler(
            annotationFinder, configuration, evaluator, beanResolver, propertyMappingStrategyManager
        );
    }

    @Bean
    public BeanAwareAssembleMethodAnnotationHandler beanAwareAssembleMethodAnnotationHandler(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration globalConfiguration,
        Collection<MethodContainerFactory>  methodContainerFactories,
        PropertyMappingStrategyManager propertyMappingStrategyManager, ApplicationContext applicationContext) {
        return new BeanAwareAssembleMethodAnnotationHandler(
            annotationFinder, globalConfiguration,
            methodContainerFactories,
            applicationContext, propertyMappingStrategyManager
        );
    }

    @Bean
    public AssembleEnumAnnotationHandler assembleEnumAnnotationHandler(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration globalConfiguration,
        PropertyOperator propertyOperator,
        PropertyMappingStrategyManager propertyMappingStrategyManager) {
        return new AssembleEnumAnnotationHandler(annotationFinder, globalConfiguration, propertyOperator, propertyMappingStrategyManager);
    }

    @Bean
    public AssembleConstantAnnotationHandler assembleConstantAnnotationHandler(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration configuration, PropertyMappingStrategyManager propertyMappingStrategyManager) {
        return new AssembleConstantAnnotationHandler(annotationFinder, configuration, propertyMappingStrategyManager);
    }

    @SuppressWarnings("all")
    @Bean
    public AssembleKeyAnnotationHandler assembleKeyAnnotationHandler(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration configuration,
        PropertyMappingStrategyManager propertyMappingStrategyManager,
        @Nullable Map<String, AssembleKeyAnnotationHandler.ValueMapperProvider> providers) {
        AssembleKeyAnnotationHandler handler = new AssembleKeyAnnotationHandler(annotationFinder, configuration, propertyMappingStrategyManager);
        if (CollectionUtils.isNotEmpty(providers)) {
            providers.forEach(handler::registerValueMapperProvider);
        }
        return handler;
    }

    @Bean
    public DisassembleAnnotationHandler disassembleAnnotationOperationsHandler(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration configuration) {
        return new DisassembleAnnotationHandler(annotationFinder, configuration);
    }

    @Bean
    public ConditionOnExpressionParser conditionOnExpressionParser(
        AnnotationFinder annotationFinder, ExpressionEvaluator expressionEvaluator, BeanFactoryResolver beanFactoryResolver) {
        ConditionOnExpressionParser.ContextFactory contextFactory = (t, op) -> {
            SpelExpressionContext context = new SpelExpressionContext();
            context.setBeanResolver(beanFactoryResolver);
            return context;
        };
        return new ConditionOnExpressionParser(annotationFinder, expressionEvaluator, contextFactory);
    }

    @Bean
    public ConditionOnPropertyNotNullParser conditionOnPropertyNotNullParser(
        AnnotationFinder annotationFinder, PropertyOperator propertyOperator) {
        return new ConditionOnPropertyNotNullParser(annotationFinder, propertyOperator);
    }

    @Bean
    public ConditionOnPropertyNotEmptyParser conditionOnPropertyNotEmptyParser(
        AnnotationFinder annotationFinder, PropertyOperator propertyOperator) {
        return new ConditionOnPropertyNotEmptyParser(annotationFinder, propertyOperator);
    }

    @Bean
    public ConditionOnPropertyParser conditionOnPropertyParser(
        AnnotationFinder annotationFinder, PropertyOperator propertyOperator, ConverterManager converterManager) {
        return new ConditionOnPropertyParser(annotationFinder, propertyOperator, converterManager);
    }

    @Bean
    public ConditionOnContainerParser conditionOnContainerParser(
        AnnotationFinder annotationFinder, ContainerManager containerManager) {
        return new ConditionOnContainerParser(annotationFinder, containerManager);
    }

    @Bean
    public ConditionOnTargetTypeParser conditionOnTargetTypeParser(AnnotationFinder annotationFinder) {
        return new ConditionOnTargetTypeParser(annotationFinder);
    }

    @Bean
    public ConditionalTypeHierarchyBeanOperationParser conditionalTypeHierarchyBeanOperationParser(
        Collection<OperationAnnotationHandler> handlers, Collection<ConditionParser> parsers) {
        ConditionalTypeHierarchyBeanOperationParser parser = new ConditionalTypeHierarchyBeanOperationParser();
        handlers.forEach(parser::addOperationAnnotationHandler);
        parsers.forEach(parser::registerConditionParser);
        return parser;
    }

    @Primary
    @Bean
    public DisorderedBeanOperationExecutor disorderedBeanOperationExecutor(ContainerManager containerManager) {
        return new DisorderedBeanOperationExecutor(containerManager);
    }

    @SuppressWarnings("all")
    @Bean
    public AsyncBeanOperationExecutor asyncBeanOperationExecutor(ContainerManager containerManager) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int processors = Runtime.getRuntime().availableProcessors();
        executor.setCorePoolSize(processors);
        executor.setMaxPoolSize(processors);
        executor.setQueueCapacity(1);
        executor.setThreadNamePrefix("crane4j-async-executor");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setAllowCoreThreadTimeOut(true);
        executor.initialize();
        return new AsyncBeanOperationExecutor(containerManager, executor);
    }

    @Bean
    public OrderedBeanOperationExecutor orderedBeanOperationExecutor(ContainerManager containerManager) {
        return new OrderedBeanOperationExecutor(containerManager, Comparator.comparing(AssembleOperation::getSort));
    }

    @Bean
    public MethodInvokerContainerCreator methodInvokerContainerCreator(PropertyOperator propertyOperator, ConverterManager converterManager) {
        return new MethodInvokerContainerCreator(propertyOperator, converterManager);
    }

    @Bean
    public DefaultContainerAdapterRegister defaultContainerAdapterRegister() {
        return DefaultContainerAdapterRegister.INSTANCE;
    }

    @Order
    @Bean
    public DynamicContainerOperatorProxyMethodFactory dynamicContainerOperatorProxyMethodFactory(
        ConverterManager converterManager, ParameterNameFinder parameterNameFinder,
        AnnotationFinder annotationFinder, ContainerAdapterRegister containerAdapterRegister) {
        return new DynamicContainerOperatorProxyMethodFactory(
            converterManager, parameterNameFinder, annotationFinder, containerAdapterRegister
        );
    }

    @Order
    @Bean
    public ParametersFillProxyMethodFactory parametersFillProxyMethodFactory(
        BeanOperationParser beanOperationParser) {
        return new ParametersFillProxyMethodFactory(beanOperationParser);
    }

    @Order
    @Bean
    public ArgAutoOperateProxyMethodFactory argAutoOperateProxyMethodFactory(
        AutoOperateAnnotatedElementResolver elementResolver, MethodBasedExpressionEvaluator expressionExecuteDelegate,
        ParameterNameFinder parameterNameFinder, AnnotationFinder annotationFinder) {
        return new ArgAutoOperateProxyMethodFactory(
            elementResolver, expressionExecuteDelegate, parameterNameFinder, annotationFinder
        );
    }

    @Order(Ordered.LOWEST_PRECEDENCE - 1)
    @Bean
    public CacheableMethodContainerFactory cacheableMethodContainerFactory(
        Crane4jGlobalConfiguration configuration, MethodInvokerContainerCreator methodInvokerContainerCreator, AnnotationFinder annotationFinder) {
        return new CacheableMethodContainerFactory(methodInvokerContainerCreator, annotationFinder, configuration);
    }

    @Primary
    @Bean
    public OneToOneAssembleOperationHandler oneToOneAssembleOperationHandler(
        PropertyOperator propertyOperator, ConverterManager converterManager) {
        return new OneToOneAssembleOperationHandler(propertyOperator, converterManager);
    }

    @Bean
    public ManyToManyAssembleOperationHandler manyToManyAssembleOperationHandler(
        PropertyOperator propertyOperator, ConverterManager converterManager) {
        return new ManyToManyAssembleOperationHandler(propertyOperator, converterManager);
    }

    @Bean
    public OneToManyAssembleOperationHandler oneToManyAssembleOperationHandler(
        PropertyOperator propertyOperator, ConverterManager converterManager) {
        return new OneToManyAssembleOperationHandler(propertyOperator, converterManager);
    }

    @Primary
    @Bean
    public ReflectiveDisassembleOperationHandler reflectiveDisassembleOperationHandler(PropertyOperator propertyOperator) {
        return new ReflectiveDisassembleOperationHandler(propertyOperator);
    }

    // ============== operator interface components ==============

    @Bean
    public OperationAnnotationProxyMethodFactory operationAnnotationProxyMethodFactory(ConverterManager converterManager) {
        return new OperationAnnotationProxyMethodFactory(converterManager);
    }

    @Bean
    public OperatorProxyFactory operatorProxyFactory(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration configuration,
        Collection<OperatorProxyMethodFactory> factories) {
        OperatorProxyFactory proxyFactory = new OperatorProxyFactory(configuration, annotationFinder);
        factories.forEach(proxyFactory::addProxyMethodFactory);
        return proxyFactory;
    }

    // ============== extension components ==============

    @Bean
    public OperateTemplate operateTemplate(
        BeanOperationParser parser, BeanOperationExecutor executor, TypeResolver typeResolver) {
        return new OperateTemplate(parser, executor, typeResolver);
    }

    @Bean
    public SpringParameterNameFinder springParameterNameFinder() {
        return new SpringParameterNameFinder(new DefaultParameterNameDiscoverer());
    }

    @Bean
    public MethodBasedAutoOperateAnnotatedElementResolver methodBasedAutoOperateAnnotatedElementResolver(
        Crane4jGlobalConfiguration crane4jGlobalConfiguration, TypeResolver typeResolver) {
        return new MethodBasedAutoOperateAnnotatedElementResolver(crane4jGlobalConfiguration, typeResolver);
    }

    @Bean
    public ClassBasedAutoOperateAnnotatedElementResolver classBasedAutoOperateAnnotatedElementResolver(
        Crane4jGlobalConfiguration crane4jGlobalConfiguration, ExpressionEvaluator expressionEvaluator) {
        return new ClassBasedAutoOperateAnnotatedElementResolver(
            crane4jGlobalConfiguration, expressionEvaluator, SpelExpressionContext::new
        );
    }

    @Primary
    @Bean
    public ComposableAutoOperateAnnotatedElementResolver composableAutoOperateAnnotatedElementResolver(
        Collection<AutoOperateAnnotatedElementResolver> autoOperateAnnotatedElementResolvers) {
        return new ComposableAutoOperateAnnotatedElementResolver(new ArrayList<>(autoOperateAnnotatedElementResolvers));
    }

    @Bean
    public MethodBasedExpressionEvaluator methodBaseExpressionEvaluator(
        ExpressionEvaluator expressionEvaluator, ParameterNameFinder parameterNameFinder, BeanResolver beanResolver) {
        return new MethodBasedExpressionEvaluator(
            parameterNameFinder, expressionEvaluator, method -> {
            SpelExpressionContext context = new SpelExpressionContext(method);
            context.setBeanResolver(beanResolver);
            return context;
        });
    }

    @Bean
    public MethodResultAutoOperateAdvisor methodResultAutoOperateAdvisor(
        AutoOperateAnnotatedElementResolver autoOperateAnnotatedElementResolver,
        MethodBasedExpressionEvaluator methodBasedExpressionEvaluator) {
        return new MethodResultAutoOperateAdvisor(autoOperateAnnotatedElementResolver, methodBasedExpressionEvaluator);
    }

    @Bean
    public MethodArgumentAutoOperateAdvisor methodArgumentAutoOperateAdvisor(
        MethodBasedExpressionEvaluator methodBasedExpressionEvaluator,
        AutoOperateAnnotatedElementResolver autoOperateAnnotatedElementResolver,
        ParameterNameFinder parameterNameDiscoverer, AnnotationFinder annotationFinder) {
        return new MethodArgumentAutoOperateAdvisor(autoOperateAnnotatedElementResolver, methodBasedExpressionEvaluator,
            parameterNameDiscoverer, annotationFinder
        );
    }

    @Bean
    public BeanMethodContainerRegistrar beanMethodContainerRegistrar(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration configuration) {
        return new BeanMethodContainerRegistrar(annotationFinder, configuration);
    }

    /**
     * Invoked right at the end of the singleton pre-instantiation phase,
     * with a guarantee that all regular singleton beans have been created
     * already. {@link ListableBeanFactory#getBeansOfType} calls within
     * this method won't trigger accidental side effects during bootstrap.
     * <p><b>NOTE:</b> This callback won't be triggered for singleton beans
     * lazily initialized on demand after {@link BeanFactory} bootstrap,
     * and not for any other bean scope either. Carefully use it for beans
     * with the intended bootstrap semantics only.
     */
    @Override
    public void afterSingletonsInstantiated() {
        log.info("Initialized crane4j components......");
    }
}

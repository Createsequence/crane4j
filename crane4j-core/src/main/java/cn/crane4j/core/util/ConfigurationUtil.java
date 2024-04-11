package cn.crane4j.core.util;

import cn.crane4j.annotation.ContainerCache;
import cn.crane4j.annotation.Mapping;
import cn.crane4j.annotation.MappingTemplate;
import cn.crane4j.core.cache.CacheDefinition;
import cn.crane4j.core.cache.CacheManager;
import cn.crane4j.core.cache.CacheableContainer;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerDefinition;
import cn.crane4j.core.container.lifecycle.ContainerLifecycleProcessor;
import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.parser.PropertyMapping;
import cn.crane4j.core.parser.SimplePropertyMapping;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.DefaultContainerAdapterRegister;
import cn.crane4j.core.support.OperateTemplate;
import cn.crane4j.core.support.SimpleAnnotationFinder;
import cn.crane4j.core.support.SimpleParameterNameFinder;
import cn.crane4j.core.support.container.ContainerMethodAnnotationProcessor;
import cn.crane4j.core.support.container.DefaultMethodContainerFactory;
import cn.crane4j.core.support.container.MethodContainerFactory;
import cn.crane4j.core.support.container.MethodInvokerContainerCreator;
import cn.crane4j.core.support.operator.DynamicContainerOperatorProxyMethodFactory;
import cn.crane4j.core.support.operator.OperationAnnotationProxyMethodFactory;
import cn.crane4j.core.support.operator.OperatorProxyFactory;
import cn.crane4j.core.support.operator.ParametersFillProxyMethodFactory;
import cn.crane4j.core.support.proxy.DefaultProxyFactory;
import cn.crane4j.core.support.proxy.ProxyFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ConfigurationUtil
 *
 * @author huangchengxing
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConfigurationUtil {

    /**
     * Wrap method container as cacheable container.
     *
     * @param annotation annotation
     * @param configuration configuration
     * @param container container
     * @return cacheable container
     */
    public static <K> Container<K> wrapToCacheableContainer(
        ContainerCache annotation, Crane4jGlobalConfiguration configuration, Container<K> container) {
        // wrap method container as cacheable container
        String managerName = StringUtils.emptyToDefault(
            annotation.cacheManager(), CacheManager.DEFAULT_MAP_CACHE_MANAGER_NAME
        );
        CacheManager cacheManager = configuration.getCacheManager(managerName);
        Asserts.isNotNull(cacheManager, "cacheManager [{}] not found", managerName);
        CacheDefinition cacheDefinition = new CacheDefinition.Impl(
            container.getNamespace(), managerName,
            annotation.expirationTime(), annotation.timeUnit()
        );
        return new CacheableContainer<>(container, cacheDefinition, cacheManager);
    }

    /**
     * Get element identifier.
     *
     * @param source source
     * @param defaultValue default id
     * @return name if source is field, method or parameter, otherwise return defaultValue
     */
    public static String getElementIdentifier(AnnotatedElement source, String defaultValue) {
        if (StringUtils.isNotEmpty(defaultValue)) {
            return defaultValue;
        }
        if (source instanceof Field) {
            return ((Field)source).getName();
        }
        if (source instanceof Method) {
            return ((Method)source).getName();
        }
        if (source instanceof Parameter) {
            return ((Parameter)source).getName();
        }
        return defaultValue;
    }

    /**
     * Create {@link OperatorProxyFactory} instance.
     *
     * @param configuration configuration
     * @return {@link OperatorProxyFactory}
     * @since 2.3.0
     */
    public static OperatorProxyFactory createOperatorProxyFactory(Crane4jGlobalConfiguration configuration) {
        AnnotationFinder annotationFinder = SimpleAnnotationFinder.INSTANCE;
        OperatorProxyFactory operatorProxyFactory = OperatorProxyFactory.builder()
            .globalConfiguration(configuration)
            .annotationFinder(annotationFinder)
            .proxyFactory(DefaultProxyFactory.INSTANCE)
            .build();
        operatorProxyFactory.addProxyMethodFactory(new OperationAnnotationProxyMethodFactory(configuration.getConverterManager()));
        operatorProxyFactory.addProxyMethodFactory(new DynamicContainerOperatorProxyMethodFactory(
            configuration.getConverterManager(), SimpleParameterNameFinder.INSTANCE,
            annotationFinder, DefaultContainerAdapterRegister.INSTANCE
        ));
        operatorProxyFactory.addProxyMethodFactory(new ParametersFillProxyMethodFactory(
            configuration.getBeanOperationsParser(BeanOperationParser.class)
        ));
        return operatorProxyFactory;
    }

    /**
     * Create {@link MethodInvokerContainerCreator} instance.
     *
     * @param configuration configuration
     * @return {@link MethodInvokerContainerCreator}
     * @since 2.3.0
     */
    public static MethodInvokerContainerCreator createMethodInvokerContainerCreator(Crane4jGlobalConfiguration configuration) {
        return new MethodInvokerContainerCreator(configuration.getPropertyOperator(), configuration.getConverterManager());
    }
    
    /**
     * Create {@link ContainerMethodAnnotationProcessor} instance.
     *
     * @param configuration configuration
     * @return {@link ContainerMethodAnnotationProcessor}
     * @since 2.3.0
     */
    public static ContainerMethodAnnotationProcessor createContainerMethodAnnotationProcessor(Crane4jGlobalConfiguration configuration) {
        MethodInvokerContainerCreator methodInvokerContainerCreator = createMethodInvokerContainerCreator(configuration);
        AnnotationFinder annotationFinder = SimpleAnnotationFinder.INSTANCE;
        DefaultMethodContainerFactory factory = new DefaultMethodContainerFactory(methodInvokerContainerCreator, annotationFinder);
        List<MethodContainerFactory> methodContainerFactories = CollectionUtils.newCollection(ArrayList::new, factory);
        return new ContainerMethodAnnotationProcessor(methodContainerFactories, annotationFinder);
    }

    /**
     * Create {@link OperateTemplate} instance by given configuration.
     *
     * @param configuration configuration
     * @return {@link OperateTemplate}
     * @since 2.3.0
     */
    public static OperateTemplate createOperateTemplate(Crane4jGlobalConfiguration configuration) {
        return new OperateTemplate(
            configuration.getBeanOperationsParser(BeanOperationParser.class),
            configuration.getBeanOperationExecutor(BeanOperationExecutor.class),
            configuration.getTypeResolver()
        );
    }

    /**
     * Get component from configuration,
     *
     * @param resultType result type
     * @param componentType component type
     * @param componentName component name
     * @param getByTypeAndName get by type and name method
     * @param getByType get by type method
     * @return component instance
     */
    public static <T> T getComponentFromConfiguration(
        Class<T> resultType, Class<?> componentType, @Nullable String componentName,
        BiFunction<Class<T>, String, T> getByTypeAndName, Function<Class<T>, T> getByType) {
        // resolved type
        @SuppressWarnings("unchecked")
        Class<T> actualComponentType = ClassUtils.isObjectOrVoid(componentType) ?
            resultType : (Class<T>)componentType;
        return StringUtils.isEmpty(componentName) ?
            getByType.apply(actualComponentType) : getByTypeAndName.apply(actualComponentType, componentName);
    }

    // ================ trigger lifecycle callback ================

    /**
     * trigger {@link ContainerLifecycleProcessor#whenDestroyed}
     *
     * @param target container instance or container definition
     */
    public static void triggerWhenDestroyed(
        Object target, Collection<ContainerLifecycleProcessor> containerLifecycleProcessorList) {
        containerLifecycleProcessorList.forEach(processor -> processor.whenDestroyed(target));
    }

    /**
     * trigger {@link ContainerLifecycleProcessor#whenRegistered}
     *
     * @param definition definition
     * @param namespace namespace
     * @param old old container instance or container definition
     * @return container definition
     */
    @Nullable
    public static ContainerDefinition triggerWhenRegistered(
        ContainerDefinition definition, String namespace, Object old,
        Collection<ContainerLifecycleProcessor> containerLifecycleProcessorList, Logger log) {
        for (ContainerLifecycleProcessor containerLifecycleProcessor : containerLifecycleProcessorList) {
            definition = containerLifecycleProcessor.whenRegistered(old, definition);
            if (Objects.isNull(definition)) {
                log.info("not register container definition for [{}]", namespace);
                return null;
            }
        }
        return definition;
    }

    /**
     * trigger {@link ContainerLifecycleProcessor#whenCreated}
     *
     * @param namespace namespace
     * @param container container
     * @param definition definition
     * @return container instance
     */
    @Nullable
    public static Container<Object> triggerWhenCreated(
        String namespace, ContainerDefinition definition, Container<Object> container,
        Collection<ContainerLifecycleProcessor> containerLifecycleProcessorList, Logger log) {
        for (ContainerLifecycleProcessor containerLifecycleProcessor : containerLifecycleProcessorList) {
            container = containerLifecycleProcessor.whenCreated(definition, container);
            if (Objects.isNull(container)) {
                log.warn(
                        "not create container for [{}], because of container lifecycle processor [{}] return null",
                        namespace, containerLifecycleProcessor.getClass().getSimpleName()
                );
                break;
            }
        }
        return container;
    }

    // ==================== parsing ====================

    public static List<PropertyMapping> parsePropTemplateClasses(Class<?>[] annotatedTypes, AnnotationFinder annotationFinder) {
        return Stream.of(annotatedTypes)
            .map(type -> annotationFinder.findAnnotation(type, MappingTemplate.class))
            .filter(Objects::nonNull)
            .map(ConfigurationUtil::parsePropTemplate)
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    public static List<PropertyMapping> parsePropTemplate(MappingTemplate annotation) {
        return Stream.of(annotation.value())
            .map(ConfigurationUtil::createPropertyMapping)
            .collect(Collectors.toList());
    }

    public static PropertyMapping createPropertyMapping(Mapping annotation) {
        return createPropertyMapping(annotation, "");
    }

    public static PropertyMapping createPropertyMapping(Mapping annotation, String defaultReference) {
        if (StringUtils.isNotEmpty(annotation.value())) {
            return new SimplePropertyMapping(annotation.value(), annotation.value());
        }
        String ref = StringUtils.emptyToDefault(annotation.ref(), defaultReference);
        return new SimplePropertyMapping(annotation.src(), ref);
    }
}

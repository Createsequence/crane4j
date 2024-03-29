package cn.crane4j.core.parser.handler;

import cn.crane4j.annotation.Mapping;
import cn.crane4j.annotation.MappingTemplate;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerManager;
import cn.crane4j.core.executor.handler.AssembleOperationHandler;
import cn.crane4j.core.executor.key.KeyResolver;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.PropertyMapping;
import cn.crane4j.core.parser.SimplePropertyMapping;
import cn.crane4j.core.parser.handler.strategy.OverwriteNotNullMappingStrategy;
import cn.crane4j.core.parser.handler.strategy.PropertyMappingStrategy;
import cn.crane4j.core.parser.handler.strategy.PropertyMappingStrategyManager;
import cn.crane4j.core.parser.operation.AssembleOperation;
import cn.crane4j.core.parser.operation.KeyTriggerOperation;
import cn.crane4j.core.parser.operation.SimpleAssembleOperation;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.util.ClassUtils;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.core.util.ConfigurationUtil;
import cn.crane4j.core.util.StringUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>An abstract {@link OperationAnnotationHandler} implementation
 * for resolve assemble operation annotation.<br />
 * pre-implements the defined the logic of parsing and
 * constructing {@link AssembleOperation} based on standard components.
 *
 * @author huangchengxing
 * @param <A> annotation type
 * @see StandardAssembleAnnotation
 * @see PropertyMappingStrategy
 * @since 1.3.0
 */
@Accessors(chain = true)
@Slf4j
public abstract class AbstractStandardAssembleAnnotationHandler<A extends Annotation>
    extends AbstractStandardOperationAnnotationHandler<A> {

    protected final Crane4jGlobalConfiguration globalConfiguration;
    private final PropertyMappingStrategyManager propertyMappingStrategyManager;

    /**
     * Create an {@link AbstractStandardAssembleAnnotationHandler} instance.
     *
     * @param annotationType annotation type
     * @param annotationFinder annotation finder
     * @param operationComparator operation comparator
     * @param globalConfiguration global configuration
     * @param propertyMappingStrategyManager property mapping strategy manager
     */
    protected AbstractStandardAssembleAnnotationHandler(
        Class<A> annotationType, AnnotationFinder annotationFinder,
        @NonNull Comparator<KeyTriggerOperation> operationComparator, Crane4jGlobalConfiguration globalConfiguration,
        PropertyMappingStrategyManager propertyMappingStrategyManager) {
        super(annotationType, annotationFinder, operationComparator);
        this.globalConfiguration = globalConfiguration;
        this.propertyMappingStrategyManager = propertyMappingStrategyManager;
    }

    /**
     * Do resolve for operations.
     *
     * @param beanOperations bean operations
     * @param operations     operations
     */
    @Override
    protected void doResolve(BeanOperations beanOperations, List<KeyTriggerOperation> operations) {
        operations.stream()
            .map(AssembleOperation.class::cast)
            .forEach(beanOperations::addAssembleOperations);
    }

    /**
     * Create assemble operation for given {@code element} and {@code annotation}
     *
     * @param parser         bean operation parser
     * @param beanOperations bean operations to resolve
     * @param standardAnnotation standard annotation
     * @return {@link KeyTriggerOperation} instance if element and annotation is resolvable, null otherwise
     */
    @Nullable
    @Override
    protected AssembleOperation createOperation(
        BeanOperationParser parser, BeanOperations beanOperations, StandardAnnotation<A> standardAnnotation) {
        StandardAssembleAnnotation<A> standardAssembleAnnotation = (StandardAssembleAnnotation<A>)standardAnnotation;
        KeyTriggerOperation keyTriggerOperation = super.createOperation(parser, beanOperations, standardAnnotation);

        Class<?> keyType = parseKeyType(standardAssembleAnnotation);
        keyType = ClassUtils.isObjectOrVoid(keyType) ? null : keyType;

        AssembleOperationHandler assembleOperationHandler = parseAssembleOperationHandler(standardAssembleAnnotation);
        Set<PropertyMapping> propertyMappings = parsePropertyMappings(standardAssembleAnnotation, keyTriggerOperation.getKey());
        // fix https://github.com/opengoofy/crane4j/issues/190
        // if no property mapping is specified, the default property mapping is the key itself
        propertyMappings = propertyMappings.isEmpty() ?
            CollectionUtils.newCollection(LinkedHashSet::new, new SimplePropertyMapping("", keyTriggerOperation.getKey())) : propertyMappings;
        PropertyMappingStrategy propertyMappingStrategy = parserPropertyMappingStrategy(standardAssembleAnnotation);

        // create operation
        String namespace = getContainerNamespace(standardAssembleAnnotation);
        AssembleOperation operation = SimpleAssembleOperation.builder()
            .id(keyTriggerOperation.getId())
            .key(keyTriggerOperation.getKey())
            .sort(keyTriggerOperation.getSort())
            .groups(keyTriggerOperation.getGroups())
            .source(keyTriggerOperation.getSource())
            .keyDescription(standardAssembleAnnotation.getKeyDesc())
            .propertyMappings(propertyMappings)
            .container(namespace)
            .assembleOperationHandler(assembleOperationHandler)
            .propertyMappingStrategy(propertyMappingStrategy)
            .keyType(keyType)
            .build();

        // determine key resolver
        Optional.ofNullable(standardAssembleAnnotation.getKeyResolver())
            .filter(StringUtils::isNotEmpty)
            .map(globalConfiguration::getKeyResolver)
            .map(p -> p.getResolver(operation))
            .ifPresent(operation::setKeyResolver);
        return operation;
    }

    /**
     * Get container from given {@code annotation}.
     *
     * @param standardAnnotation standard annotation
     * @return namespace of {@link Container}
     * @implNote if the container needs to be obtained through a specific provider,
     * the name of the provider and the namespace of the container need to be concatenated through {@link ContainerManager#canonicalNamespace}
     * @see ContainerManager#canonicalNamespace
     */
    protected abstract String getContainerNamespace(StandardAssembleAnnotation<A> standardAnnotation);

    /**
     * Get {@link StandardAssembleAnnotation}.
     *
     * @param beanOperations bean operations
     * @param element element
     * @param annotation annotation
     * @return {@link StandardAssembleAnnotation} instance
     */
    @Override
    protected abstract StandardAssembleAnnotation<A> getStandardAnnotation(
        BeanOperations beanOperations, AnnotatedElement element, A annotation);

    // =============== process standard configuration ===============

    /**
     * Get assemble operation groups from given {@link StandardAssembleAnnotation}.
     *
     * @param standardAnnotation standard annotation
     * @return assemble operation groups
     */
    @SuppressWarnings("unused")
    protected AssembleOperationHandler parseAssembleOperationHandler(
        StandardAssembleAnnotation<A> standardAnnotation) {
        return globalConfiguration.getAssembleOperationHandler(
            standardAnnotation.getHandler(), standardAnnotation.getHandlerType()
        );
    }

    /**
     * Get property mapping from given {@link StandardAssembleAnnotation}.
     *
     * @param standardAnnotation standard annotation
     * @param key key
     * @return assemble operation groups
     */
    protected Set<PropertyMapping> parsePropertyMappings(
        StandardAssembleAnnotation<A> standardAnnotation, String key) {
        // add mappings from props
        Mapping[] props = standardAnnotation.getProps();
        Set<PropertyMapping> propertyMappings = Stream.of(props)
            .map(m -> ConfigurationUtil.createPropertyMapping(m, key))
            .collect(Collectors.toSet());
        // add mappings from prop
        Set<PropertyMapping> propMappings = parsePropToMapping(standardAnnotation, key);
        if (CollectionUtils.isNotEmpty(propMappings)) {
            propertyMappings.addAll(propMappings);
        }
        // add template mappings
        Class<?>[] propTemplates = standardAnnotation.getMappingTemplates();
        List<PropertyMapping> templateMappings = ConfigurationUtil.parsePropTemplateClasses(propTemplates, annotationFinder);
        if (CollectionUtils.isNotEmpty(templateMappings)) {
            propertyMappings.addAll(templateMappings);
        }
        return propertyMappings;
    }

    /**
     * Get property mapping from given {@link StandardAssembleAnnotation#getProp()}.
     *
     * @param standardAnnotation standard annotation
     * @param key key
     * @return assemble operation groups
     */
    protected Set<PropertyMapping> parsePropToMapping(
        StandardAssembleAnnotation<A> standardAnnotation, String key) {
        String[] props = standardAnnotation.getProp();
        return Stream.of(props)
            .map(SimplePropertyMapping::of)
            .collect(Collectors.toSet());
    }

    /**
     * Parse {@link PropertyMappingStrategy} instance from given annotation.
     *
     * @param standardAnnotation standard annotation
     * @return {@link PropertyMappingStrategy} instance
     * @since 2.1.0
     */
    @SuppressWarnings("unused")
    @NonNull
    protected PropertyMappingStrategy parserPropertyMappingStrategy(
        StandardAssembleAnnotation<A> standardAnnotation) {
        String propertyMappingStrategyName = standardAnnotation.getPropertyMappingStrategy();
        // fix https://gitee.com/opengoofy/crane4j/issues/I7X36D
        if (StringUtils.isEmpty(propertyMappingStrategyName)) {
            // if no policy is specified, the default policy is actually required
            return OverwriteNotNullMappingStrategy.INSTANCE;
        }

        PropertyMappingStrategy propertyMappingStrategy = propertyMappingStrategyManager.getPropertyMappingStrategy(propertyMappingStrategyName);
        if (Objects.isNull(propertyMappingStrategy)) {
            propertyMappingStrategy = OverwriteNotNullMappingStrategy.INSTANCE;
            if (StringUtils.isEmpty(propertyMappingStrategyName)) {
                log.warn("unable to find property mapping strategy [{}], use default strategy [{}]", standardAnnotation.getPropertyMappingStrategy(), propertyMappingStrategy.getName());
            }
        }
        return propertyMappingStrategy;
    }

    /**
     * Parse key property type.
     *
     * @param standardAnnotation standard annotation
     * @return key type
     * @since 2.2.0
     */
    @SuppressWarnings("unused")
    @Nullable
    protected Class<?> parseKeyType(
        StandardAssembleAnnotation<A> standardAnnotation) {
        Class<?> keyType = standardAnnotation.getKeyType();
        return ClassUtils.isObjectOrVoid(keyType) ? null : keyType;
    }

    /**
     * Standard annotation
     *
     * @author huangchengxing
     * @see StandardAssembleAnnotationAdapter
     */
    public interface StandardAssembleAnnotation<A extends Annotation> extends StandardAnnotation<A> {

        /**
         * Get key property type.
         *
         * @return key type
         */
        Class<?> getKeyType();

        /**
         * The name of the handler to be used.
         *
         * @return name of the handler
         */
        String getHandler();

        /**
         * The type of the handler to be used.
         *
         * @return name of the handler
         */
        Class<?> getHandlerType();

        /**
         * <p>Mapping template classes.
         * specify a class, if {@link MappingTemplate} exists on the class,
         * it will scan and add {@link Mapping} to {@link #getProps}。
         *
         * @return java.lang.Class<?>[]
         */
        Class<?>[] getMappingTemplates();

        /**
         * Attributes that need to be mapped
         * between the data source object and the current object.
         *
         * @return attributes mappings
         */
        Mapping[] getProps();

        /**
         * Attributes that need to be mapped
         * between the data source object and the current object.
         *
         * @return attributes mappings
         */
        String[] getProp();

        /**
         * The name of property mapping strategy.
         *
         * @return strategy name
         */
        String getPropertyMappingStrategy();

        /**
         * The name of {@link KeyResolver} to be used.
         *
         * @return name
         * @since 2.7.0
         */
        @Nullable
        String getKeyResolver();

        /**
         * Some description of the key which
         * helps {@link KeyResolver} to resolve the key.
         *
         * @return description
         * @since 2.7.0
         */
        @Nullable
        String getKeyDesc();
    }

    /**
     * Adapting annotation to {@link StandardAssembleAnnotation}
     *
     * @author huangchengxing
     */
    @SuppressWarnings("all")
    @SuperBuilder
    @Getter
    public static class StandardAssembleAnnotationAdapter<A extends Annotation>
        extends StandardAnnotationAdapter<A> implements StandardAssembleAnnotation<A> {
        @Builder.Default
        private final Class<?> keyType = Object.class;
        @Builder.Default
        private final String handler = "";
        @Builder.Default
        private final Class<?> handlerType = Object.class;
        @Builder.Default
        private final Class<?>[] mappingTemplates = new Class<?>[0];
        @Builder.Default
        private final Mapping[] props = new Mapping[0];
        @Builder.Default
        private final String[] prop = new String[0];
        @Builder.Default
        private final String propertyMappingStrategy = "";
        @Builder.Default
        @Nullable
        private final String keyResolver = null;
        @Builder.Default
        @Nullable
        private final String keyDesc = null;
    }
}

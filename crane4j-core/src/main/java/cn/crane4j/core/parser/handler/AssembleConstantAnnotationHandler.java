package cn.crane4j.core.parser.handler;

import cn.crane4j.annotation.AssembleConstant;
import cn.crane4j.annotation.ContainerConstant;
import cn.crane4j.core.container.ConstantContainerBuilder;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.PropertyMapping;
import cn.crane4j.core.parser.SimplePropertyMapping;
import cn.crane4j.core.parser.handler.strategy.PropertyMappingStrategyManager;
import cn.crane4j.core.parser.operation.KeyTriggerOperation;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.Crane4jGlobalSorter;
import cn.crane4j.core.util.ClassUtils;
import cn.crane4j.core.util.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.reflect.AnnotatedElement;
import java.util.Comparator;
import java.util.Set;

/**
 * An {@link AbstractStandardAssembleAnnotationHandler} implementation for {@link AssembleConstant} annotation.
 *
 * @author huangchengxing
 * @see AssembleConstant
 * @since 2.6.0
 */
public class AssembleConstantAnnotationHandler extends InternalProviderAssembleAnnotationHandler<AssembleConstant> {

    /**
     * Create an {@link AbstractStandardAssembleAnnotationHandler} instance.
     *
     * @param annotationFinder    annotation finder
     * @param globalConfiguration globalConfiguration
     * @param propertyMappingStrategyManager property mapping strategy manager
     */
    public AssembleConstantAnnotationHandler(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration globalConfiguration,
        PropertyMappingStrategyManager propertyMappingStrategyManager) {
        this(annotationFinder, Crane4jGlobalSorter.comparator(), globalConfiguration, propertyMappingStrategyManager);
    }

    /**
     * Create an {@link AbstractStandardAssembleAnnotationHandler} instance.
     *
     * @param annotationFinder    annotation finder
     * @param operationComparator operation comparator
     * @param globalConfiguration globalConfiguration
     * @param propertyMappingStrategyManager property mapping strategy manager
     */
    public AssembleConstantAnnotationHandler(
        AnnotationFinder annotationFinder, Comparator<KeyTriggerOperation> operationComparator,
        Crane4jGlobalConfiguration globalConfiguration, PropertyMappingStrategyManager propertyMappingStrategyManager) {
        super(AssembleConstant.class, annotationFinder, operationComparator, globalConfiguration, propertyMappingStrategyManager);
    }

    /**
     * Create container by given annotation and namespace.
     *
     * @param standardAnnotation standard annotation
     * @param namespace  namespace
     * @return {@link Container} instant
     */
    @NonNull
    @Override
    protected Container<Object> createContainer(
        StandardAssembleAnnotation<AssembleConstant> standardAnnotation, String namespace) {
        AssembleConstant annotation = standardAnnotation.getAnnotation();
        Class<?> constantType = resolveConstantType(annotation);
        ConstantContainerBuilder builder = ConstantContainerBuilder.of(constantType)
            .namespace(namespace)
            .annotationFinder(annotationFinder);
        if (!annotation.followTypeConfig()) {
            ContainerConstant constant = annotation.constant();
            builder.reverse(constant.reverse())
                .onlyPublic(constant.onlyPublic())
                .onlyExplicitlyIncluded(constant.onlyExplicitlyIncluded());
        }
        return builder.build();
    }

    /**
     * Get the namespace from annotation.
     *
     * @param standardAnnotation standard annotation
     * @return namespace
     */
    @Override
    protected String determineNamespace(StandardAssembleAnnotation<AssembleConstant> standardAnnotation) {
        AssembleConstant annotation = standardAnnotation.getAnnotation();
        Class<?> constantType = resolveConstantType(annotation);
        String config = annotation.followTypeConfig() ? "FollowTypeConfig" : annotation.constant().toString();
        return StringUtils.md5DigestAsHex(StringUtils.join(
            String::valueOf, "#", constantType, config
        ));
    }

    private Class<?> resolveConstantType(AssembleConstant annotation) {
        Class<?> type = annotation.type();
        if (ClassUtils.isObjectOrVoid(type)) {
            type = ClassUtils.forName(annotation.typeName(), type);
        }
        return type;
    }

    /**
     * Get {@link StandardAssembleAnnotation}.
     *
     * @param beanOperations bean operations
     * @param element        element
     * @param annotation     annotation
     * @return {@link StandardAssembleAnnotation} instance
     */
    @Override
    protected StandardAssembleAnnotation<AssembleConstant> getStandardAnnotation(
        BeanOperations beanOperations, AnnotatedElement element, AssembleConstant annotation) {
        return StandardAssembleAnnotationAdapter.<AssembleConstant>builder()
            .annotatedElement(element)
            .annotation(annotation)
            .id(annotation.id())
            .key(annotation.key())
            .keyResolver(annotation.keyResolver())
            .keyDesc(annotation.keyDesc())
            .sort(annotation.sort())
            .groups(annotation.groups())
            .keyType(annotation.keyType())
            .handler(annotation.handler())
            .handlerType(annotation.handlerType())
            .mappingTemplates(annotation.propTemplates())
            .props(annotation.props())
            .prop(annotation.prop())
            .propertyMappingStrategy(annotation.propertyMappingStrategy())
            .build();
    }

    /**
     * Get property mapping from given {@link StandardAssembleAnnotation}.
     *
     * @param standardAnnotation standard annotation
     * @param key                key
     * @return assemble operation groups
     */
    @Override
    protected Set<PropertyMapping> parsePropertyMappings(
        StandardAssembleAnnotation<AssembleConstant> standardAnnotation, String key) {
        Set<PropertyMapping> propertyMappings = super.parsePropertyMappings(standardAnnotation, key);
        AssembleConstant annotation = standardAnnotation.getAnnotation();
        if (StringUtils.isNotEmpty(annotation.ref())) {
            propertyMappings.add(new SimplePropertyMapping("", annotation.ref()));
        }
        return propertyMappings;
    }
}

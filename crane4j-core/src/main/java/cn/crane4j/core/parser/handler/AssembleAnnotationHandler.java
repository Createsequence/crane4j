package cn.crane4j.core.parser.handler;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerManager;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.handler.strategy.PropertyMappingStrategyManager;
import cn.crane4j.core.parser.operation.KeyTriggerOperation;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.Crane4jGlobalSorter;
import cn.crane4j.core.support.Sorted;
import cn.crane4j.core.util.StringUtils;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.AnnotatedElement;
import java.util.Comparator;

/**
 * <p>Annotation-based {@link OperationAnnotationHandler} implementation
 * that the construction of operation configuration
 * by resolving annotations based on {@link Assemble} on classes and attributes.
 *
 * @author huangchengxing
 * @see Assemble
 * @since 1.2.0
 */
@Accessors(chain = true)
@Slf4j
public class AssembleAnnotationHandler extends AbstractStandardAssembleAnnotationHandler<Assemble> {

    /**
     * Create a {@link AssembleAnnotationHandler} instance.
     *
     * @param annotationFinder annotation finder
     * @param globalConfiguration global configuration
     * @param operationComparator operation comparator
     * @param propertyMappingStrategyManager property mapping strategy manager
     */
    public AssembleAnnotationHandler(
        AnnotationFinder annotationFinder,
        Crane4jGlobalConfiguration globalConfiguration,
        Comparator<KeyTriggerOperation> operationComparator,
        PropertyMappingStrategyManager propertyMappingStrategyManager) {
        super(Assemble.class, annotationFinder, operationComparator, globalConfiguration, propertyMappingStrategyManager);
    }

    /**
     * <p>Create a {@link AssembleAnnotationHandler} instance.<br />
     * The order of operation configurations is {@link Sorted#getSort} from small to large.
     *
     * @param annotationFinder annotation finder
     * @param globalConfiguration global configuration
     * @param propertyMappingStrategyManager property mapping strategy manager
     */
    public AssembleAnnotationHandler(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration globalConfiguration,
        PropertyMappingStrategyManager propertyMappingStrategyManager) {
        this(annotationFinder, globalConfiguration, Crane4jGlobalSorter.comparator(), propertyMappingStrategyManager);
    }

    /**
     * Get container from given {@code annotation}.
     *
     * @param standardAnnotation standard annotation
     * @return namespace of {@link Container}
     */
    @Override
    protected String getContainerNamespace(StandardAssembleAnnotation<Assemble> standardAnnotation) {
        Assemble annotation = standardAnnotation.getAnnotation();
        String namespace = annotation.container();
        if (StringUtils.isEmpty(namespace)) {
            return Container.EMPTY_CONTAINER_NAMESPACE;
        }
        return ContainerManager.canonicalNamespace(namespace, annotation.containerProvider());
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
    protected StandardAssembleAnnotation<Assemble> getStandardAnnotation(
        BeanOperations beanOperations, AnnotatedElement element, Assemble annotation) {
        return StandardAssembleAnnotationAdapter.<Assemble>builder()
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
}

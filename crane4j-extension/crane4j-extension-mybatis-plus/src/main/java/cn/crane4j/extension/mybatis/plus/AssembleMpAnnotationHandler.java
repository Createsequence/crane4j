package cn.crane4j.extension.mybatis.plus;

import cn.crane4j.annotation.AssembleMp;
import cn.crane4j.annotation.MappingType;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerManager;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.handler.AbstractStandardAssembleAnnotationHandler;
import cn.crane4j.core.parser.handler.OperationAnnotationHandler;
import cn.crane4j.core.parser.handler.strategy.PropertyMappingStrategyManager;
import cn.crane4j.core.parser.operation.AssembleOperation;
import cn.crane4j.core.parser.operation.KeyTriggerOperation;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.Crane4jGlobalSorter;
import lombok.experimental.Accessors;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.Comparator;

/**
 * <p>The implementation of {@link OperationAnnotationHandler}.<br />
 * It's used to scan the {@link AssembleMp} annotations on classes and their attributes,
 * And generate {@link AssembleOperation} for it using {@link MybatisPlusQueryContainerProvider.Query} as the data source container.
 *
 * @author huangchengxing
 * @see AssembleMp
 * @see MybatisPlusQueryContainerProvider
 * @since 1.2.0
 */
@Accessors(chain = true)
public class AssembleMpAnnotationHandler extends AbstractStandardAssembleAnnotationHandler<AssembleMp> {

    private static final String QUERY_CONTAINER_PROVIDER_NAME = "MybatisQueryContainerProvider";
    private final MybatisPlusQueryContainerProvider containerRegister;

    /**
     * Create a {@link AssembleMpAnnotationHandler} instance.
     *
     * @param annotationFinder annotation finder
     * @param containerRegister mybatis plus query container register
     * @param globalConfiguration global configuration
     * @param propertyMappingStrategyManager property mapping strategy manager
     */
    public AssembleMpAnnotationHandler(
        AnnotationFinder annotationFinder,
        MybatisPlusQueryContainerProvider containerRegister,
        Crane4jGlobalConfiguration globalConfiguration,
        PropertyMappingStrategyManager propertyMappingStrategyManager) {
        this(annotationFinder, Crane4jGlobalSorter.comparator(), containerRegister, globalConfiguration, propertyMappingStrategyManager);
    }

    /**
     * Create a {@link AssembleMpAnnotationHandler} instance.
     *
     * @param annotationFinder annotation finder
     * @param operationComparator operation comparator
     * @param containerRegister mybatis plus query container register
     * @param globalConfiguration global configuration
     * @param propertyMappingStrategyManager property mapping strategy manager
     */
    public AssembleMpAnnotationHandler(
        AnnotationFinder annotationFinder, Comparator<KeyTriggerOperation> operationComparator,
        MybatisPlusQueryContainerProvider containerRegister,
        Crane4jGlobalConfiguration globalConfiguration,
        PropertyMappingStrategyManager propertyMappingStrategyManager) {
        super(AssembleMp.class, annotationFinder, operationComparator, globalConfiguration, propertyMappingStrategyManager);
        this.containerRegister = containerRegister;
        globalConfiguration.registerContainerProvider(QUERY_CONTAINER_PROVIDER_NAME, containerRegister);
    }

    /**
     * Get container from given {@code annotation}.
     *
     * @param standardAnnotation standard annotation
     * @return namespace of {@link Container}
     */
    @Override
    protected String getContainerNamespace(StandardAssembleAnnotation<AssembleMp> standardAnnotation) {
        AssembleMp annotation = standardAnnotation.getAnnotation();
        String namespace = containerRegister.determineNamespace(
            annotation.mapper(), annotation.where(), Arrays.asList(annotation.selects())
        );
        if (annotation.mappingType() != MappingType.ONE_TO_ONE) {
            containerRegister.setMappingType(namespace, annotation.mappingType());
        }
        return ContainerManager.canonicalNamespace(namespace, QUERY_CONTAINER_PROVIDER_NAME);
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
    protected StandardAssembleAnnotation<AssembleMp> getStandardAnnotation(
        BeanOperations beanOperations, AnnotatedElement element, AssembleMp annotation) {
        return StandardAssembleAnnotationAdapter.<AssembleMp>builder()
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

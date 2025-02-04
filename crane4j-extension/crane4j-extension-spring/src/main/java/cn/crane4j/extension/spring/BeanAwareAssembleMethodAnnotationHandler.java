package cn.crane4j.extension.spring;

import cn.crane4j.annotation.AssembleMethod;
import cn.crane4j.core.parser.handler.AssembleMethodAnnotationHandler;
import cn.crane4j.core.parser.handler.strategy.PropertyMappingStrategyManager;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.container.MethodContainerFactory;
import cn.crane4j.core.util.Try;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;

import java.util.Collection;
import java.util.Optional;

/**
 * An {@link AssembleMethodAnnotationHandler} implementation,
 * support find target bean from {@link ApplicationContext}.
 *
 * @author huangchengxing
 * @see ApplicationContext
 */
public class BeanAwareAssembleMethodAnnotationHandler extends AssembleMethodAnnotationHandler {

    /**
     * Application context
     */
    private final ApplicationContext applicationContext;

    /**
     * Create an {@link AssembleMethodAnnotationHandler} instance.
     *
     * @param annotationFinder         annotation finder
     * @param globalConfiguration      global configuration
     * @param methodContainerFactories method container factories
     * @param applicationContext application context
     * @param propertyMappingStrategyManager property mapping strategy manager
     */
    public BeanAwareAssembleMethodAnnotationHandler(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration globalConfiguration,
        Collection<MethodContainerFactory> methodContainerFactories,
        ApplicationContext applicationContext,
        PropertyMappingStrategyManager propertyMappingStrategyManager) {
        super(annotationFinder, globalConfiguration, methodContainerFactories, propertyMappingStrategyManager);
        this.applicationContext = applicationContext;
    }

    /**
     * Resolve target class.
     *
     * @param annotation annotation
     * @return target type
     */
    @NonNull
    @Override
    protected Class<?> resolveTargetType(AssembleMethod annotation) {
        return findTargetFromSpring(annotation.targetType(), annotation.target())
            .<Class<?>>map(AopUtils::getTargetClass)
            .orElseGet(() -> super.resolveTargetType(annotation));
    }

    /**
     * Get target by given type and annotation.
     *
     * @param targetType target type
     * @param annotation annotation
     * @return target instance
     */
    @Nullable
    @Override
    protected Object getTargetInstance(Class<?> targetType, AssembleMethod annotation) {
        return findTargetFromSpring(targetType, annotation.target())
            .orElseGet(() -> applicationContext.getAutowireCapableBeanFactory().createBean(targetType));
    }

    private Optional<Object> findTargetFromSpring(Class<?> beanType, String beanName) {
        return Try.of(() -> applicationContext.getBean(beanName))
            .getOrElseTry(ex -> applicationContext.getBean(beanName, beanType))
            .getOrElseTry(ex -> applicationContext.getBean(beanType))
            .getOptional();
    }
}

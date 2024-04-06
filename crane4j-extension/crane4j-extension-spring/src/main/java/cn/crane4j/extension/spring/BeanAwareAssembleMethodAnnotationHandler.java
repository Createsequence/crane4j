package cn.crane4j.extension.spring;

import cn.crane4j.annotation.AssembleMethod;
import cn.crane4j.core.parser.handler.AssembleMethodAnnotationHandler;
import cn.crane4j.core.parser.handler.strategy.PropertyMappingStrategyManager;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.container.MethodContainerFactory;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Supplier;

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
     * Resolve target type.
     *
     * @param annotation annotation
     * @return target type
     */
    @NonNull
    @Override
    protected Class<?> resolveTargetType(AssembleMethod annotation) {
        Object target = findTargetFromSpring(annotation.targetType(), annotation.target());
        return Objects.isNull(target) ?
            super.resolveTargetType(annotation) : AopUtils.getTargetClass(target);
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
        Object target = findTargetFromSpring(targetType, annotation.target());
        if (target != null) {
            return target;
        }

        // TODO replace to applicationContext.getAutowireCapableBeanFactory().createBean()
        return super.getTargetInstance(targetType, annotation);
    }

    @Nullable
    private Object findTargetFromSpring(Class<?> beanType, String beanName) {
        // try to get bean from application context by name
        Object target = tryGet(() -> applicationContext.getBean(beanName));
        if (Objects.nonNull(target)) {
            return target;
        }

        // try to get bean from application context by name and type
        target = tryGet(() -> applicationContext.getBean(beanName, beanType));
        if (Objects.nonNull(target)) {
            return target;
        }

        // try to get bean from application context by type
        target = tryGet(() -> applicationContext.getBean(beanType));
        if (Objects.nonNull(target)) {
            return target;
        }
        return null;
    }

    private static <T> T tryGet(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception ex) {
            // ignore
        }
        return null;
    }
}

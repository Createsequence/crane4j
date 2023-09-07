package cn.crane4j.core.support.container;

import cn.crane4j.annotation.ContainerMethod;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.MethodInvokerContainer;
import cn.crane4j.core.support.AnnotationFinder;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>The basic implementation of {@link MethodContainerFactory},
 * build the method data source according to the method annotated by {@link ContainerMethod}.
 *
 * @author huangchengxing
 * @see ContainerMethod
 * @see MethodInvokerContainer
 * @see MethodInvokerContainerCreator
 */
@Slf4j
public class DefaultMethodContainerFactory implements MethodContainerFactory {

    public static final int ORDER = Integer.MAX_VALUE;
    protected final MethodInvokerContainerCreator methodInvokerContainerCreator;
    protected final AnnotationFinder annotationFinder;

    /**
     * Create a {@link MethodContainerFactory} instance.
     *
     * @param methodInvokerContainerCreator method invoker container creator
     * @param annotationFinder annotation finder
     */
    public DefaultMethodContainerFactory(
        MethodInvokerContainerCreator methodInvokerContainerCreator, AnnotationFinder annotationFinder) {
        this.methodInvokerContainerCreator = methodInvokerContainerCreator;
        this.annotationFinder = annotationFinder;
    }

    /**
     * <p>Gets the sorting value.<br />
     * The smaller the value, the higher the priority of the object.
     *
     * @return sorting value
     */
    @Override
    public int getSort() {
        return ORDER;
    }

    /**
     * Whether the method is supported.
     *
     * @param source method's calling object
     * @param method method
     * @param annotations annotations
     * @return true if supported, false otherwise
     */
    @Override
    public boolean support(@Nullable Object source, Method method, Collection<ContainerMethod> annotations) {
        return !Objects.equals(method.getReturnType(), Void.TYPE);
    }

    /**
     * Adapt methods to data source containers.
     *
     * @param source method's calling object
     * @param method method
     * @param annotations annotations
     * @return data source containers
     */
    @Override
    public List<Container<Object>> get(@Nullable Object source, Method method, Collection<ContainerMethod> annotations) {
        return annotations.stream()
            .map(annotation -> methodInvokerContainerCreator.createContainer(
                source, method, annotation.type(), annotation.namespace(),
                annotation.resultType(), annotation.resultKey()
            ))
            .collect(Collectors.toList());
    }
}

package cn.crane4j.core.support.container;

import cn.crane4j.annotation.ContainerMethod;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.support.Crane4jGlobalSorter;
import cn.crane4j.core.util.Asserts;
import cn.crane4j.core.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A support class for creating {@link Container} instances from method which annotated by {@link ContainerMethod}.
 *
 * @author huangchengxing
 * @see ContainerMethodAnnotationProcessor
 */
@Slf4j
public abstract class AbstractContainerMethodSupport {

    /**
     * method container factories
     */
    protected final List<MethodContainerFactory> methodContainerFactories;

    /**
     * Create a {@link AbstractContainerMethodSupport} instance.
     *
     * @param methodContainerFactories method container factories
     */
    protected AbstractContainerMethodSupport(Collection<MethodContainerFactory> methodContainerFactories) {
        this.methodContainerFactories = methodContainerFactories.stream()
            .sorted(Crane4jGlobalSorter.comparator())
            .collect(Collectors.toList());
    }

    /**
     * Register {@link MethodContainerFactory} instance.
     *
     * @param methodContainerFactory method container factory
     * @since 2.5.0
     */
    public void registerMethodContainerFactory(@NonNull MethodContainerFactory methodContainerFactory) {
        if (!methodContainerFactories.contains(methodContainerFactory)) {
            this.methodContainerFactories.add(methodContainerFactory);
            this.methodContainerFactories.sort(Crane4jGlobalSorter.comparator());
        }
    }

    /**
     * Create container instance with given bean and method by specific {@link MethodContainerFactory}.
     *
     * @param bean bean
     * @param method method
     * @param annotations annotations
     * @return container instances
     */
    protected Collection<Container<Object>> createMethodContainer(
        @Nullable Object bean, Method method, Collection<ContainerMethod> annotations) {
        return methodContainerFactories.stream()
            .filter(factory -> factory.support(bean, method, annotations))
            .findFirst()
            .map(factory -> factory.get(bean, method, annotations))
            .orElse(Collections.emptyList());
    }

    /**
     * <p>Find the most matched method by given annotation in the method group,
     * the method with the most matched parameter types will be returned.<br/>
     *
     * @param methods methods
     * @param annotation annotation
     * @return java.lang.reflect.Method
     */
    @Nullable
    protected Method findMatchedMethodForAnnotation(List<Method> methods, ContainerMethod annotation) {
        Map<String, List<Method>> methodGroup = methods.stream().collect(Collectors.groupingBy(Method::getName));
        String methodName = StringUtils.emptyToDefault(annotation.bindMethod(), annotation.namespace());
        List<Method> candidates = methodGroup.get(methodName);
        Asserts.isNotEmpty(
            candidates, "bound method not found: {}({})",
            annotation.bindMethod(), StringUtils.join(Class::getName, ", ", annotation.bindMethodParamTypes())
        );
        if (candidates.size() == 1) {
            return candidates.get(0);
        }
        Method matched = findMostMatchMethod(candidates, annotation.bindMethodParamTypes());
        if (Objects.nonNull(matched)) {
            return matched;
        }
        if (log.isDebugEnabled()) {
            log.debug("bound method not found: [{}] ({})", annotation.bindMethod(), Arrays.asList(annotation.bindMethodParamTypes()));
        }
        return null;
    }

    /**
     * <p>Find the most matched method by given method name and param types,
     * the method with the most matched parameter types will be returned.<br/>
     * If there are multiple methods with the same number of matched parameter types or no matched method,
     * the first one will be returned.
     *
     * @param candidates methods which have the same name, list size must be greater than 0
     * @param expectedTypes param types
     * @return most matched method
     */
    @Nullable
    protected Method findMostMatchMethod(@NonNull List<Method> candidates, @NonNull Class<?>[] expectedTypes) {
        // record the number of matched parameter types for each method:
        // n < 0: no match
        // n == 0: match, but the method has no parameter
        // n > 0: match and the parameter type is the same as expected
        int[] matchCounts = new int[candidates.size()];
        Arrays.fill(matchCounts, 0);
        Method mostMatchedMethod = matchMethods(candidates, matchCounts, expectedTypes);
        if (Objects.nonNull(mostMatchedMethod)) {
            return mostMatchedMethod;
        }

        // get index of the most matched method
        // if there are multiple methods with the same number of matched parameter types or no method is matched,
        // the first method will be returned
        int indexOfMostMatchMethod = -1;
        for (int i = 1; i < matchCounts.length; i++) {
            int matchCount = matchCounts[i];
            if (matchCount < 0) {
                continue;
            }
            if (indexOfMostMatchMethod < 0
                || matchCount > matchCounts[indexOfMostMatchMethod]) {
                indexOfMostMatchMethod = i;
            }
        }
        return indexOfMostMatchMethod < 0 ? null : candidates.get(indexOfMostMatchMethod);
    }

    private Method matchMethods(List<Method> candidates, int[] matchCounts, Class<?>[] expectedTypes) {
        int expectedCount = expectedTypes.length;
        for (int i = 0; i < candidates.size(); i++) {
            Method curr = candidates.get(i);
            Class<?>[] actualTypes = curr.getParameterTypes();
            int actualCount = actualTypes.length;
            if (actualCount < expectedCount) {
                // is not matched
                matchCounts[i] = -1;
                continue;
            }
            // record the number of matched parameter types
            int matchCount = 0;
            for (int j = 0; j < expectedCount; j++) {
                if (!expectedTypes[j].isAssignableFrom(actualTypes[j])) {
                    // is not matched
                    matchCounts[i] = -1;
                    break;
                }
                matchCount++;
            }
            // all parameter types are matched, it must be the most matched method
            if (matchCount == expectedCount && actualCount == expectedCount) {
                return curr;
            }
            matchCounts[i] = matchCount;
        }
        return null;
    }
}

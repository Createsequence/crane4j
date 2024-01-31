package cn.crane4j.core.container;

import cn.crane4j.annotation.DuplicateStrategy;
import cn.crane4j.annotation.MappingType;
import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.util.CollectionUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>Method data source container, specify any method without parameters
 * or the first parameter is {@link Collection} type method,
 * and adapt it to a data source container.
 *
 * <p>This container is generally not created directly by users,
 * but is used in conjunction with {@link cn.crane4j.core.support.container.MethodInvokerContainerCreator MethodInvokerContainerCreator} to scan the methods
 * in a large number of specific context objects in the framework
 * according to annotations or specific configurations,
 * automatically adapt them to data source containers and register them.
 *
 * <p>If only a few known methods need to be used as data sources,
 * you can directly use {@link LambdaContainer}.
 *
 * @author huangchengxing
 * @see cn.crane4j.core.support.container.MethodContainerFactory
 * @see cn.crane4j.core.support.container.MethodInvokerContainerCreator
 * @see MappingType
 */
@RequiredArgsConstructor
public class MethodInvokerContainer implements Container<Object> {

    /**
     * Namespace of method.
     */
    @Getter
    protected final String namespace;

    /**
     * Method to call.
     */
    protected final MethodInvoker methodInvoker;

    /**
     * Target object of method invocation,
     * if method is static, this field can be null.
     */
    @Nullable
    private final Object target;

    /**
     * Create a standard method data source container.
     *
     * @param namespace namespace
     * @param methodInvoker method to call
     * @param target object to be called, if the method is static, it can be null
     * @param isMapped {@link MappingType#NO_MAPPING}
     * @return {@link MethodInvokerContainer}
     * @since 2.4.0
     */
    public static MethodInvokerContainer create(
        String namespace, MethodInvoker methodInvoker, @Nullable Object target, boolean isMapped) {
        return isMapped ? new NoMapping(namespace, methodInvoker, target)
            : new MethodInvokerContainer(namespace, methodInvoker, target);
    }

    /**
     * Create a method data source container with a key value extractor.
     *
     * @param namespace namespace
     * @param methodInvoker method to call
     * @param target object to be called, if the method is static, it can be null
     * @param keyExtractor key value extraction method of the data source object
     * @param duplicateStrategy duplicate strategy
     * @return {@link MethodInvokerContainer}
     * @since 2.4.0
     */
    public static MethodInvokerContainer oneToOne(
        String namespace, MethodInvoker methodInvoker, @Nullable Object target,
        MethodInvokerContainer.KeyExtractor keyExtractor, DuplicateStrategy duplicateStrategy) {
        return new MethodInvokerContainer.OneToOne(namespace, methodInvoker, target, keyExtractor, duplicateStrategy);
    }

    /**
     * Create a method data source container with a key value extractor.
     *
     * @param namespace namespace
     * @param methodInvoker method to call
     * @param target object to be called, if the method is static, it can be null
     * @param keyExtractor key value extraction method of the data source object
     * @return {@link MethodInvokerContainer}
     * @since 2.4.0
     */
    public static MethodInvokerContainer oneToMany(
        String namespace, MethodInvoker methodInvoker, @Nullable Object target,
        MethodInvokerContainer.KeyExtractor keyExtractor) {
        return new MethodInvokerContainer.OneToMany(namespace, methodInvoker, target, keyExtractor);
    }

    /**
     * Enter a batch of key values to return data source objects grouped by key values.
     *
     * @param keys keys
     * @return data source objects grouped by key value
     */
    @Override
    public Map<Object, ?> get(Collection<Object> keys) {
        Object[] arguments = resolveArguments(keys);
        Object result = methodInvoker.invoke(target, arguments);
        if (Objects.isNull(result)) {
            return Collections.emptyMap();
        }
        Collection<?> results = CollectionUtils.adaptObjectToCollection(result);
        return resolveResult(keys, results);
    }

    /**
     * Resolve arguments.
     *
     * @param keys keys
     * @return arguments
     */
    protected Object[] resolveArguments(Collection<Object> keys) {
        return new Object[] {keys};
    }

    /**
     * Resolve result to map.
     *
     * @param keys    keys
     * @param results result
     * @return map
     */
    protected Map<Object, ?> resolveResult(Collection<Object> keys, Collection<?> results) {
        Map<Object, Object> resultMap = new HashMap<>(keys.size());
        Iterator<?> valueIterator = results.iterator();
        for (Object key : keys) {
            Object value = valueIterator.hasNext() ? valueIterator.next() : null;
            resultMap.put(key, value);
        }
        return resultMap;
    }

    /**
     * {@link MappingType#NO_MAPPING}
     *
     * @since 2.4.0
     */
    protected static class NoMapping extends MethodInvokerContainer {

        public NoMapping(String namespace, MethodInvoker methodInvoker, @Nullable Object target) {
            super(namespace, methodInvoker, target);
        }

        /**
         * Resolve result to map.
         *
         * @param keys    keys
         * @param results result
         * @return map
         */
        @SuppressWarnings("unchecked")
        @Override
        protected Map<Object, ?> resolveResult(Collection<Object> keys, Collection<?> results) {
            return (Map<Object, ?>)CollectionUtils.getFirstNotNull(results);
        }
    }

    /**
     * {@link MappingType#ONE_TO_ONE}
     *
     * @since 2.4.0
     */
    protected static class OneToOne extends MethodInvokerContainer {

        protected final KeyExtractor keyExtractor;
        private final DuplicateStrategy duplicateStrategy;

        public OneToOne(
            String namespace, MethodInvoker methodInvoker, @Nullable Object target,
            KeyExtractor keyExtractor, DuplicateStrategy duplicateStrategy) {
            super(namespace, methodInvoker, target);
            this.keyExtractor = keyExtractor;
            this.duplicateStrategy = duplicateStrategy;
        }

        /**
         * Resolve result to map.
         *
         * @param keys    keys
         * @param results result
         * @return map
         */
        @Override
        protected Map<Object, ?> resolveResult(Collection<Object> keys, Collection<?> results) {
            Map<Object, Object> resultMap = new HashMap<>(results.size());
            results.forEach(newVal -> {
                Object k = keyExtractor.getKey(newVal);
                resultMap.compute(k, (key, oldVal) -> Objects.isNull(oldVal) ? newVal : duplicateStrategy.choose(key, oldVal, newVal));
            });
            return resultMap;
        }
    }

    /**
     * {@link MappingType#ONE_TO_MANY}
     *
     * @since 2.4.0
     */
    protected static class OneToMany extends MethodInvokerContainer {

        private final KeyExtractor keyExtractor;

        public OneToMany(
            String namespace, MethodInvoker methodInvoker, @Nullable Object target,
            KeyExtractor keyExtractor) {
            super(namespace, methodInvoker, target);
            this.keyExtractor = keyExtractor;
        }

        /**
         * Resolve result to map.
         *
         * @param keys    keys
         * @param results result
         * @return map
         */
        @Override
        protected Map<Object, ?> resolveResult(Collection<Object> keys, Collection<?> results) {
            return results.stream()
                .collect(Collectors.groupingBy(keyExtractor::getKey));
        }
    }

    /**
     * The key value extractor is used to obtain the key value from the data source object.
     */
    @FunctionalInterface
    public interface KeyExtractor {

        /**
         * Get key value from a source object.
         *
         * @param source source object
         * @return key value
         */
        Object getKey(Object source);
    }
}

package cn.crane4j.core.executor.handler;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.executor.handler.key.KeyResolver;
import cn.crane4j.core.executor.handler.key.ReflectiveSeparablePropertyKeyResolver;
import cn.crane4j.core.parser.PropertyMapping;
import cn.crane4j.core.parser.handler.strategy.PropertyMappingStrategy;
import cn.crane4j.core.parser.operation.AssembleOperation;
import cn.crane4j.core.support.converter.ConverterManager;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.util.Asserts;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.core.util.StringUtils;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>An implementation of {@link AssembleOperationHandler}
 * for the one-to-one mapping between the target object and the data source object.
 *
 * <p>The difference between {@link ManyToManyAssembleOperationHandler} and {@link OneToManyAssembleOperationHandler}
 * is that {@link OneToManyAssembleOperationHandler} is used to handle the situation where
 * multiple values can be obtained through a key in the data source container,
 * while {@link ManyToManyAssembleOperationHandler} is used to handle the situation where only
 * one value can be obtained through a key, but there are multiple keys at the same time.
 *
 * @author huangchengxing
 * @see ReflectiveSeparablePropertyKeyResolver
 */
@RequiredArgsConstructor
public class ManyToManyAssembleOperationHandler extends AbstractAssembleOperationHandler {

    private static final String DEFAULT_KEY_SEPARATOR = ",";
    protected final PropertyOperator propertyOperator;
    protected final ConverterManager converterManager;

    /**
     * Obtain the corresponding data source object from the data source container based on the entity's key value.
     *
     * @param container container
     * @param targets   targets
     * @return source objects
     */
    @SuppressWarnings("unchecked")
    @Override
    protected Map<Object, Object> getSourcesFromContainer(Container<?> container, Collection<Target> targets) {
        Set<Object> keys = targets.stream()
            .map(Target::getKey)
            .map(k -> (Collection<?>)k)
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());
        return (Map<Object, Object>)((Container<Object>)container).get(keys);
    }

    /**
     * Get the data source object associated with the target object.
     *
     * @param target  target
     * @param sources sources
     * @return data source object associated with the target object
     */
    @Override
    protected Object getTheAssociatedSource(Target target, Map<Object, Object> sources) {
        return ((Collection<?>)target.getKey()).stream()
            .map(sources::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * Complete attribute mapping between the target object and the data source object.
     *
     * @param source source
     * @param target target
     */
    @Override
    protected void completeMapping(Object source, Target target) {
        AssembleOperation operation = target.getExecution().getOperation();
        PropertyMappingStrategy propertyMappingStrategy = operation.getPropertyMappingStrategy();
        Collection<?> sources = CollectionUtils.adaptObjectToCollection(source);
        Set<PropertyMapping> mappings = operation.getPropertyMappings();
        for (PropertyMapping mapping : mappings) {
            // there are always multiple source values,
            // so we need to merge the source objects after operation
            Collection<?> sourceValues = !mapping.hasSource() ? sources : sources.stream()
                .map(s -> propertyOperator.readProperty(s.getClass(), s, mapping.getSource()))
                .collect(Collectors.toList());
            Object origin = target.getOrigin();
            propertyMappingStrategy.doMapping(
                origin, source, sourceValues, mapping,
                sv -> propertyOperator.writeProperty(origin.getClass(), origin, mapping.getReference(), sourceValues)
            );
        }
    }

    /**
     * Determine key resolver for the operation.
     *
     * @param operation operation
     * @return key resolver
     * @since 2.7.0
     */
    @Override
    public KeyResolver determineKeyResolver(AssembleOperation operation) {
        KeyResolver specifiedkeyResolver = operation.getKeyResolver();
        if (Objects.nonNull(specifiedkeyResolver)) {
            return specifiedkeyResolver;
        }
        Asserts.isNotEmpty(operation.getKey(), "The key must be specified for the operation config on [{}]", operation.getSource());
        String separator = StringUtils.emptyToDefault(operation.getKeyDescription(), DEFAULT_KEY_SEPARATOR);
        return new ReflectiveSeparablePropertyKeyResolver(
            propertyOperator, converterManager, separator
        );
    }
}

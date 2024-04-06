package cn.crane4j.core.executor.handler;

import cn.crane4j.core.executor.handler.key.ReflectiveBeanKeyResolver;
import cn.crane4j.core.executor.handler.key.ReflectiveSeparablePropertyKeyResolver;
import cn.crane4j.core.parser.PropertyMapping;
import cn.crane4j.core.parser.handler.strategy.PropertyMappingStrategy;
import cn.crane4j.core.support.converter.ConverterManager;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.util.CollectionUtils;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>An implementation of {@link AssembleOperationHandler}
 * for the one-to-many mapping between the target object and the data source object.
 * 
 * <p>The difference between {@link ManyToManyAssembleOperationHandler} and {@link OneToManyAssembleOperationHandler}
 * is that {@link OneToManyAssembleOperationHandler} is used to handle the situation where
 * multiple values can be obtained through a key in the data source container,
 * while {@link ManyToManyAssembleOperationHandler} is used to handle the situation where only
 * one value can be obtained through a key, but there are multiple keys at the same time.
 *
 * @author huangchengxing
 * @see ReflectiveBeanKeyResolver
 * @see ReflectiveSeparablePropertyKeyResolver
 */
public class OneToManyAssembleOperationHandler extends OneToOneAssembleOperationHandler {

    /**
     * Create a new {@link OneToOneAssembleOperationHandler} instance.
     *
     * @param propertyOperator propertyOperator
     * @param converterManager converterManager
     */
    public OneToManyAssembleOperationHandler(
        PropertyOperator propertyOperator, ConverterManager converterManager) {
        super(propertyOperator, converterManager);
    }

    /**
     * Complete attribute mapping between the target object and the data source object.
     *
     * @param source source
     * @param target target
     */
    @Override
    protected void completeMapping(Object source, Target target) {
        PropertyMappingStrategy propertyMappingStrategy = target.getExecution().getOperation().getPropertyMappingStrategy();
        Collection<?> sources = CollectionUtils.adaptObjectToCollection(source);
        Set<PropertyMapping> mappings = target.getExecution().getOperation().getPropertyMappings();
        for (PropertyMapping mapping : mappings) {
            // there are always multiple source values,
            // so we need to merge the source objects after operation
            Collection<?> sourceValues = mapping.hasSource() ?
                sources.stream().map(s -> propertyOperator.readProperty(s.getClass(), s, mapping.getSource()))
                    .collect(Collectors.toList()) : sources;
            Object origin = target.getOrigin();
            propertyMappingStrategy.doMapping(
                origin, source, sourceValues, mapping,
                sv -> propertyOperator.writeProperty(origin.getClass(), origin, mapping.getReference(), sourceValues)
            );
        }
    }
}

package cn.crane4j.core.executor.handler;

import cn.crane4j.core.executor.AssembleExecution;
import cn.crane4j.core.executor.handler.key.IntrospectionKeyResolver;
import cn.crane4j.core.executor.handler.key.KeyResolver;
import cn.crane4j.core.executor.handler.key.ReflectiveBeanKeyResolver;
import cn.crane4j.core.executor.handler.key.ReflectivePropertyKeyResolver;
import cn.crane4j.core.parser.PropertyMapping;
import cn.crane4j.core.parser.SimplePropertyMapping;
import cn.crane4j.core.parser.handler.strategy.PropertyMappingStrategy;
import cn.crane4j.core.parser.operation.AssembleOperation;
import cn.crane4j.core.support.converter.ConverterManager;
import cn.crane4j.core.support.reflect.PropDesc;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.util.Asserts;
import cn.crane4j.core.util.ClassUtils;
import cn.crane4j.core.util.ReflectUtils;
import cn.crane4j.core.util.StringUtils;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An implementation of {@link AssembleOperationHandler}
 * for the one-to-one mapping between the target object and the data source object.
 *
 * @author huangchengxing
 */
public class OneToOneAssembleOperationHandler extends AbstractAssembleOperationHandler {

    protected final PropertyOperator propertyOperator;
    protected final ConverterManager converterManager;
    private final KeyResolver keyResolver;
    private final KeyResolver converterKeyResolver;

    /**
     * Create a new {@link OneToOneAssembleOperationHandler} instance.
     *
     * @param propertyOperator propertyOperator
     * @param converterManager converterManager
     */
    public OneToOneAssembleOperationHandler(
        PropertyOperator propertyOperator, ConverterManager converterManager) {
        this.propertyOperator = propertyOperator;
        this.converterManager = converterManager;
        this.keyResolver = ReflectivePropertyKeyResolver.create(propertyOperator);
        this.converterKeyResolver = ReflectivePropertyKeyResolver.create(propertyOperator, converterManager);
    }

    /**
     * Determine key resolver for the operation.
     *
     * @param operation operation
     * @return key resolver
     * @see ReflectivePropertyKeyResolver
     * @see ReflectiveBeanKeyResolver
     * @see IntrospectionKeyResolver
     * @since 2.7.0
     */
    @Override
    public KeyResolver determineKeyResolver(AssembleOperation operation) {
        KeyResolver specifiedkeyResolver = operation.getKeyResolver();
        if (Objects.nonNull(specifiedkeyResolver)) {
            return specifiedkeyResolver;
        }
        Class<?> keyType = operation.getKeyType();
        boolean specifiedKeyType = Objects.nonNull(keyType) && !ClassUtils.isObjectOrVoid(keyType);
        // specified key
        if (StringUtils.isNotEmpty(operation.getKey())) {
            return specifiedKeyType ? converterKeyResolver : keyResolver;
        }
        // not specified key and key type
        if (Objects.isNull(keyType)) {
            return IntrospectionKeyResolver.INSTANCE;
        }
        // if key is not specified, and the key type is an instantiable custom class,
        // its meaning we need make a bean instance as the key.
        if (isInstantiableCustomClass(keyType)) {
            String keyDescription = operation.getKeyDescription();
            Set<PropertyMapping> propertyMappings = StringUtils.isEmpty(keyDescription) ?
                resolvePropertyMappings(keyType) : resolvePropertyMappings(keyDescription);
            return new ReflectiveBeanKeyResolver(propertyOperator, propertyMappings.toArray(new PropertyMapping[0]));
        }
        return null;
    }

    private boolean isInstantiableCustomClass(@Nullable Class<?> keyType) {
        return Objects.nonNull(keyType)
            && (!ClassUtils.isJdkClass(keyType) || Map.class.isAssignableFrom(keyType))
            && ClassUtils.isInstantiable(keyType, null);
    }

    /**
     * Resolve the property mappings when no key description is specified.
     *
     * @param targetType target type
     * @return property mappings
     */
    private Set<PropertyMapping> resolvePropertyMappings(Class<?> targetType) {
        return Arrays.stream(ReflectUtils.getFields(targetType))
            .map(field -> new SimplePropertyMapping(field.getName(), field.getName()))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Resolve the property mappings when key description is specified.
     *
     * @param keyDescription key description
     * @return property mappings
     */
    private Set<PropertyMapping> resolvePropertyMappings(String keyDescription) {
        Set<PropertyMapping> mappings = SimplePropertyMapping.from(keyDescription);
        mappings.forEach(m -> Asserts.isTrue(
            m.hasSource() && StringUtils.isNotEmpty(m.getReference()),
            "The property mappings is illegal: {} -> {}", m.getReference(), m.getSource()
        ));
        return mappings;
    }

    /**
     * Complete attribute mapping between the target object and the data source object.
     *
     * @param source source
     * @param target target
     */
    @Override
    protected void completeMapping(Object source, Target target) {
        AssembleExecution execution = target.getExecution();
        AssembleOperation operation = execution.getOperation();
        PropertyMappingStrategy propertyMappingStrategy = operation.getPropertyMappingStrategy();
        Set<PropertyMapping> mappings = operation.getPropertyMappings();
        doCompleteMapping(source, target.getOrigin(), mappings, propertyMappingStrategy);
    }

    private void doCompleteMapping(
        Object source, Object target, Set<PropertyMapping> mappings, PropertyMappingStrategy propertyMappingStrategy) {
        PropDesc sourceDesc = propertyOperator.getPropertyDescriptor(source.getClass());
        PropDesc targetDesc = propertyOperator.getPropertyDescriptor(target.getClass());
        for (PropertyMapping mapping : mappings) {
            Object sourceValue = mapping.hasSource() ?
                sourceDesc.readProperty(source, mapping.getSource()) : source;
            propertyMappingStrategy.doMapping(
                target, source, sourceValue, mapping,
                sv -> targetDesc.writeProperty(target, mapping.getReference(), sourceValue)
            );
        }
    }
}

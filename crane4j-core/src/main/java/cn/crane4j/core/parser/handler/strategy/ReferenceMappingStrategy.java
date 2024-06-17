package cn.crane4j.core.parser.handler.strategy;

import cn.crane4j.core.parser.PropertyMapping;
import cn.crane4j.core.parser.operation.AssembleOperation;
import cn.crane4j.core.support.reflect.PropertyOperator;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Assignment of source values to the target object
 * is only allowed if the reference field value of the target object is null.
 *
 * @author huangchengxing
 * @since 2.1.0
 */
@RequiredArgsConstructor
public class ReferenceMappingStrategy implements PropertyMappingStrategy {

    private final PropertyOperator propertyOperator;

    /**
     * Map {@code sourceValue} to reference fields in target.
     *
     * @param operation assemble operation
     * @param target          target object
     * @param source          source object
     * @param sourceValue     source value
     * @param propertyMapping property mapping
     * @param mapping         mapping action
     */
    @Override
    public void doMapping(
        AssembleOperation operation,
        Object target, Object source, @Nullable Object sourceValue,
        PropertyMapping propertyMapping, Consumer<Object> mapping) {
        Object referencePropertyValue = propertyOperator.readProperty(target.getClass(), target, propertyMapping.getReference());
        if (Objects.isNull(referencePropertyValue)) {
            mapping.accept(sourceValue);
        }
    }
}

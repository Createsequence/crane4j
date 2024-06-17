package cn.crane4j.core.parser.handler.strategy;

import cn.crane4j.core.parser.PropertyMapping;
import cn.crane4j.core.parser.operation.AssembleOperation;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Assign the source value to the referenced field only if the referenced source value is not null.
 *
 * @author huangchengxing
 * @since 2.1.0
 */
public class OverwriteNotNullMappingStrategy implements PropertyMappingStrategy {

    public static final OverwriteNotNullMappingStrategy INSTANCE = new OverwriteNotNullMappingStrategy();

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
        if (Objects.nonNull(sourceValue)) {
            mapping.accept(sourceValue);
        }
    }
}

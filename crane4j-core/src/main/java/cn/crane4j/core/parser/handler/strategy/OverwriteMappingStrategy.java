package cn.crane4j.core.parser.handler.strategy;

import cn.crane4j.core.parser.PropertyMapping;
import cn.crane4j.core.parser.operation.AssembleOperation;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Consumer;

/**
 * Enforce overwriting the original value of the referenced field regardless of whether the referenced source value is null or not.
 *
 * @author huangchengxing
 * @since 2.1.0
 */
public class OverwriteMappingStrategy implements PropertyMappingStrategy {

    public static final OverwriteMappingStrategy INSTANCE = new OverwriteMappingStrategy();

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
        mapping.accept(sourceValue);
    }
}

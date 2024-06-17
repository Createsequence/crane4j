package cn.crane4j.core.parser.handler.strategy;

import cn.crane4j.core.parser.PropertyMapping;
import cn.crane4j.core.parser.operation.AssembleOperation;
import cn.crane4j.core.support.NamedComponent;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Consumer;

/**
 * Property value mapping strategy.
 *
 * @author huangchengxing
 * @see OverwriteMappingStrategy
 * @see OverwriteNotNullMappingStrategy
 * @see ReferenceMappingStrategy
 * @see PropertyMappingStrategy
 * @since 2.1.0
 */
public interface PropertyMappingStrategy extends NamedComponent {

    String Coll_JOIN_AS_STRING = CollJoinAsStringMappingStrategy.class.getSimpleName();
    String OVERWRITE = OverwriteMappingStrategy.class.getSimpleName();
    String OVERWRITE_NOT_NULL = OverwriteNotNullMappingStrategy.class.getSimpleName();
    String REFERENCE = ReferenceMappingStrategy.class.getSimpleName();

    /**
     * Map {@code sourceValue} to reference fields in target.
     *
     * @param operation assemble operation
     * @param target target object
     * @param source source object
     * @param sourceValue source value
     * @param propertyMapping property mapping
     * @param mapping mapping action
     */
    void doMapping(
        AssembleOperation operation,
        Object target, Object source, @Nullable Object sourceValue, 
        PropertyMapping propertyMapping, Consumer<Object> mapping);
}

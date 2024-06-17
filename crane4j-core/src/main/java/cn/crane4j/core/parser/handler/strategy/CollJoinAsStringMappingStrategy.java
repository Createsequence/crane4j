package cn.crane4j.core.parser.handler.strategy;

import cn.crane4j.core.parser.PropertyMapping;
import cn.crane4j.core.parser.operation.AssembleOperation;
import cn.crane4j.core.util.StringUtils;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Join as string mapping strategy.
 *
 * @author huangchengxing
 * @see AssembleOperation#getKeyDescription()
 * @since 2.9.0
 */
public class CollJoinAsStringMappingStrategy implements PropertyMappingStrategy {

    public static final CollJoinAsStringMappingStrategy INSTANCE = new CollJoinAsStringMappingStrategy();
    private static final String DEFAULT_DELIMITER = ",";

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
        if (sourceValue instanceof Collection) {
            String delimiter = StringUtils.emptyToDefault(operation.getKeyDescription(), DEFAULT_DELIMITER);
            sourceValue = ((Collection<?>)sourceValue).stream()
                .map(String::valueOf)
                .collect(Collectors.joining(delimiter));
        }
        mapping.accept(sourceValue);
    }
}

package cn.crane4j.core.executor.handler.key;

import cn.crane4j.core.parser.operation.AssembleOperation;
import cn.crane4j.core.support.converter.ConverterManager;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.util.ClassUtils;
import cn.crane4j.core.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>Key resolver, which is used to resolve the key of the operation.
 *
 * @author huangchengxing
 */
@RequiredArgsConstructor
public class ReflectiveSeparablePropertyKeyResolver implements KeyResolver {

    private final PropertyOperator propertyOperator;
    private final ConverterManager converterManager;
    private final String separator;

    /**
     * Resolve the key of the operation.
     *
     * @param target    target
     * @param operation operation
     * @return key
     */
    @Override
    public Object resolve(Object target, AssembleOperation operation) {
        Object propertyValue = propertyOperator.readProperty(target.getClass(), target, operation.getKey());
        Collection<?> values = splitKey(propertyValue, separator);
        if (values.isEmpty()) {
            return Collections.emptyList();
        }
        Class<?> keyType = operation.getKeyType();
        if (Objects.isNull(keyType) || ClassUtils.isObjectOrVoid(keyType)) {
            return values;
        }
        return values.stream()
            .map(v -> converterManager.convert(v, keyType))
            .collect(Collectors.toList());
    }

    /**
     * Split the key value.
     *
     * @param propertyValue property value
     * @param keySplitter key splitter
     * @return split values
     */
    @NonNull
    @SuppressWarnings("unchecked")
    protected Collection<?> splitKey(@Nullable Object propertyValue, String keySplitter) {
        if (Objects.isNull(propertyValue)) {
            return Collections.emptyList();
        }
        if (propertyValue instanceof CharSequence) {
            return StringUtils.split((String)propertyValue, keySplitter);
        }
        if (propertyValue instanceof Collection) {
            return (Collection<Object>) propertyValue;
        }
        if (propertyValue.getClass().isArray()) {
            return Arrays.asList((Object[]) propertyValue);
        }
        return Collections.singletonList(propertyValue);
    }
}

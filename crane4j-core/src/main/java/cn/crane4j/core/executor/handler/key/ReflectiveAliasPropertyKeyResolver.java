package cn.crane4j.core.executor.handler.key;

import cn.crane4j.core.parser.operation.AssembleOperation;
import cn.crane4j.core.support.converter.ConverterManager;
import cn.crane4j.core.support.reflect.PropDesc;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.util.ClassUtils;
import cn.crane4j.core.util.StringUtils;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Reflective alias property key resolver.
 *
 * @author huangchengxing
 * @since 2.7.0
 */
@RequiredArgsConstructor
public class ReflectiveAliasPropertyKeyResolver implements KeyResolver, KeyResolver.OperationAware {

    private static final String DEFAULT_KEY_SEPARATOR = ",";
    private static final String[] EMPTY = new String[0];
    private final PropertyOperator propertyOperator;
    private final ConverterManager converterManager;
    private String[] keyProperties;

    /**
     * Resolve the key of the operation.
     *
     * @param target    target
     * @param operation operation
     * @return key
     */
    @Override
    public Object resolve(Object target, AssembleOperation operation) {
        PropDesc propDesc = propertyOperator.getPropertyDescriptor(target.getClass());
        Class<?> keyType = operation.getKeyType();
        boolean specifiedKeyType = Objects.nonNull(keyType) && !ClassUtils.isObjectOrVoid(keyType);
        return Stream.of(keyProperties)
            .map(p -> propDesc.readProperty(target, p))
            .filter(Objects::nonNull)
            .findFirst()
            .map(k -> specifiedKeyType ? converterManager.convert(k, keyType) : k)
            .orElse(null);
    }

    /**
     * Set assemble operation.
     *
     * @param operation operation
     */
    @Override
    public void setAssembleOperation(AssembleOperation operation) {
        String desc = operation.getKeyDescription();
        Collection<String> props = StringUtils.split(desc, DEFAULT_KEY_SEPARATOR);
        this.keyProperties = props.isEmpty() ?
            EMPTY : props.toArray(new String[0]);
    }
}

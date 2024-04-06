package cn.crane4j.core.executor.handler.key;

import cn.crane4j.core.parser.operation.AssembleOperation;
import cn.crane4j.core.support.converter.ConverterManager;
import cn.crane4j.core.support.reflect.PropertyOperator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

/**
 * <p>Key resolver, which is used to get key value from specified property.
 *
 * @author huangchengxing
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ReflectivePropertyKeyResolver implements KeyResolver {

    private final PropertyOperator propertyOperator;

    /**
     * Create {@link ReflectivePropertyKeyResolver} instance.
     *
     * @param propertyOperator propertyOperator
     * @return key resolver
     */
    public static KeyResolver create(PropertyOperator propertyOperator) {
        return new ReflectivePropertyKeyResolver(propertyOperator);
    }

    /**
     * Create {@link ReflectivePropertyKeyResolver} instance.
     *
     * @param propertyOperator propertyOperator
     * @param converterManager converterManager
     * @return key resolver
     */
    public static KeyResolver create(PropertyOperator propertyOperator, ConverterManager converterManager) {
        return new Convertible(propertyOperator, converterManager);
    }

    /**
     * Resolve the key of the operation.
     *
     * @param target    target
     * @param operation operation
     * @return key
     */
    @Override
    public Object resolve(Object target, AssembleOperation operation) {
        return propertyOperator.readProperty(target.getClass(), target, operation.getKey());
    }

    private static class Convertible extends ReflectivePropertyKeyResolver {
        private final ConverterManager converterManager;
        public Convertible(
            PropertyOperator propertyOperator, ConverterManager converterManager) {
            super(propertyOperator);
            this.converterManager = converterManager;
        }
        @Override
        public Object resolve(Object target, AssembleOperation operation) {
            Object key = super.resolve(target, operation);
            return Objects.isNull(key) ? null : converterManager.convert(key, operation.getKeyType());
        }
    }
}

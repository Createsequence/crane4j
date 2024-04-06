package cn.crane4j.core.executor.handler.key;

import cn.crane4j.core.parser.PropertyMapping;
import cn.crane4j.core.parser.operation.AssembleOperation;
import cn.crane4j.core.support.reflect.PropDesc;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.util.ClassUtils;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>Key resolver, which is used to resolve the key of the operation.
 *
 * @author huangchengxing
 */
@RequiredArgsConstructor
public class ReflectiveBeanKeyResolver implements KeyResolver {

    private final PropertyOperator propertyOperator;
    private final PropertyMapping[] propertyMappings;

    @NonNull
    private Object newInstance(Class<?> beanType) {
        return Map.class == beanType ?
            new LinkedHashMap<>(propertyMappings.length) : ClassUtils.newInstance(beanType);
    }

    /**
     * Resolve the key of the operation.
     *
     * @param target    target
     * @param operation operation
     * @return key
     */
    @SuppressWarnings("all")
    @Override
    public Object resolve(Object target, AssembleOperation operation) {
        Class<?> beanType = operation.getKeyType();
        Object bean = newInstance(beanType);
        // copy property values from target to bean
        Class<?> targetType = target.getClass();
        PropDesc targetPropDesc = propertyOperator.getPropertyDescriptor(targetType);
        PropDesc sourcePropDesc = propertyOperator.getPropertyDescriptor(beanType);
        for (PropertyMapping mapping : propertyMappings) {
            Object propertyValue = targetPropDesc.readProperty(target, mapping.getSource());
            sourcePropDesc.writeProperty(bean, mapping.getReference(), propertyValue);
        }
        return bean;
    }
}

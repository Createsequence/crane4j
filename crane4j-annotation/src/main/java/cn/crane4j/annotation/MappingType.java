package cn.crane4j.annotation;

import java.util.Map;

/**
 * The mapping relationship between the object returned by the method and the target object.
 *
 * @author huangchengxing
 * @see cn.crane4j.core.container.MethodInvokerContainer;
 * @see ContainerMethod#type()
 */
public enum MappingType {

    /**
     * <p>Instead of mapping by key,
     * combine key and value directly into a Map collection in sequence,
     * such as {@code Collections#zip}.
     *
     * <p>When using this type,
     * the return {@link ContainerMethod#resultKey()} and {@link ContainerMethod#resultType()} are <strong>ignored</strong>.
     *
     * @see 2.5.0
     */
    ORDER_OF_KEYS,

    /**
     * <p>The return value of the method is already a {@link Map} set grouped by the key value.
     * No further conversion is required according to the key value.
     *
     * <p>When using this type,
     * the return {@link ContainerMethod#resultKey()} and {@link ContainerMethod#resultType()} are <strong>ignored</strong>.
     *
     * @since 2.5.0
     */
    NO_MAPPING,

    /**
     * <p>One key corresponds to one data source object.<br/>
     * After obtaining the data source object,
     * it will be mapped to the key specified by {@link ContainerMethod#resultKey()}.
     *
     * <p>When using this type,
     * the type which specified by {@link ContainerMethod#resultType()} <strong>must be {@code Map} or java bean</strong>.
     */
    ONE_TO_ONE,

    /**
     * <p>One key corresponds to multiple data source objects.<br/>
     * After obtaining the data source object,
     * it will be grouped with the key specified by {@link ContainerMethod#resultKey()}.
     *
     * <p>When using this type,
     * the type which specified by {@link ContainerMethod#resultType()} <strong>must be {@code Map} or java bean</strong>.
     */
    ONE_TO_MANY
}

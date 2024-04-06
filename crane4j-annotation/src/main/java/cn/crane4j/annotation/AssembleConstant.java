package cn.crane4j.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declare an operation of assemble based on constant container.
 *
 * @author huangchengxing
 * @see cn.crane4j.core.parser.TypeHierarchyBeanOperationParser;
 * @see cn.crane4j.core.parser.handler.AssembleConstantAnnotationHandler;
 * @since 2.6.0
 */
@Repeatable(value = AssembleConstant.List.class)
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AssembleConstant {

    /**
     * Constant class.
     *
     * @return enum class
     */
    Class<?> type() default Object.class;

    /**
     * Full class name of constant class,
     * only valid when {@link #type()} is {@link Void} or {@link Object}.
     *
     * @return full class name
     */
    String typeName() default "";

    /**
     * Configuration of container.
     *
     * @return {@link ContainerConstant} annotation.
     */
    ContainerConstant constant() default @ContainerConstant;

    /**
     * <p>a quick set for reference field name, equivalent to {@code @Mapping(ref = "")}.<br />
     * when not empty, the value is jointly effective  with {@link #props()}.
     *
     * @return reference field name
     * @see #props()
     */
    String ref() default "";

    /**
     * If the {@link #type()} is annotated with {@link ContainerConstant},
     * the configuration defined by that annotation will be used first.
     *
     * @return boolean
     */
    boolean followTypeConfig() default true;

    // ================= common =================

    /**
     * Operation id.
     *
     * @return id
     */
    String id() default "";

    /**
     * <p>key field name.<br />
     * This field value will be used to obtain the associated
     * data source object from the data source container later.
     *
     * <p>When the annotation is on:
     * <ul>
     *     <li>
     *         field of this class,
     *         it will be forced to specify the name of the annotated attribute,
     *         the key value is the field value of current object;
     *     </li>
     *     <li>
     *         this class, and specify key,
     *         equivalent to directly annotating on a specified field;
     *     </li>
     *     <li>
     *         this class, and key is empty,
     *         the key value is the current object itself.
     *     </li>
     * </ul>
     *
     * @return key field name
     */
    String key() default "";

    /**
     * The name of key resolver to be used.
     *
     * @return namespace
     * @since 2.7.0
     */
    Class<?> keyResolver() default Object.class;

    /**
     * Some description of the key which
     * helps {@link #keyResolver() resolver} to resolve the key.
     *
     * @return description
     * @since 2.7.0
     */
    String keyDesc() default "";

    /**
     * <p>The type to which the key value of target should be converted
     * when fetching the data source from the data source.
     *
     * <p>For example, the data source obtained from the data source
     * is grouped according to the key of the {@link Long} type,
     * and the key value corresponding to the current operation is {@link Integer},
     * then the {@code keyType} needs to be {@link Long} at this time.<br />
     * When the actual operation is performed,
     * the key value is automatically converted from Integer to {@link Long} type.
     *
     * @return key type
     */
    Class<?> keyType() default Object.class;

    /**
     * Sort values.
     * The lower the value, the higher the priority.
     *
     * @return sort values
     */
    int sort() default Integer.MAX_VALUE;

    /**
     * The name of the handler to be used.
     *
     * @return name
     * @see cn.crane4j.core.executor.handler.AssembleOperationHandler;
     */
    String handler() default "";

    /**
     * The type of the handler to be used.
     *
     * @return name
     * @see cn.crane4j.core.executor.handler.AssembleOperationHandler;
     */
    Class<?> handlerType() default Object.class;

    /**
     * <p>Attributes that need to be mapped
     * between the data source object and the current object.<br/>
     * It equivalent to {@link #prop()}.
     *
     * @return attribute mappings
     * @see #propTemplates()
     */
    Mapping[] props() default { };

    /**
     * <p>Attributes that need to be mapped
     * between the data source object and the current object.<br/>
     * It equivalent to {@link #props()}.
     *
     * <p>the format is following:
     * <ul>
     *     <li>{@code 'a:b'}：equivalent to {@code @Mapping(src = 'a', ref = 'b')}；</li>
     *     <li>{@code 'a'}：equivalent to {@code @Mapping(src = 'a', ref = 'a')} or {@code @Mapping('a')}；</li>
     *     <li>{@code ':a'}：equivalent to {@code @Mapping(ref = 'a')}；</li>
     * </ul>
     *
     * @return attribute mappings
     * @since 2.7.0
     */
    String[] prop() default { };

    /**
     * <p>Mapping template classes.
     * specify a class, if {@link MappingTemplate} exists on the class,
     * it will scan and add {@link Mapping} to {@link #props()}。
     *
     * @return mapping templates
     */
    Class<?>[] propTemplates() default {};

    /**
     * The group to which the current operation belongs.
     *
     * @return groups
     */
    String[] groups() default {};

    /**
     * Get the name of property mapping strategy.
     *
     * @return strategy name
     * @see cn.crane4j.core.parser.handler.strategy.PropertyMappingStrategy
     */
    String propertyMappingStrategy() default "";

    /**
     * Batch operation.
     *
     * @author huangchengxing
     */
    @Documented
    @Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        AssembleConstant[] value() default {};
    }
}

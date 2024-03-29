package cn.crane4j.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Declare an operation of assemble。<br />
 * Specify the specific attribute of the current object as the key.
 * When the operation is executed,
 * the key value will be extracted and the data source object
 * obtained from the specified data source container, And map the attribute value
 * specified in the data source object to the corresponding attribute
 * of the current object according to the configuration。<br />
 * for example：
 * <pre class="code">{@code
 * public class Foo {
 *     @Assemble(
 *         namespace = "test",
 *         props = @Mapping(ref = "name", src = "value")
 *     )
 *     private Integer id;
 *     private String name;
 * }
 * }</pre>
 * The above example shows that the corresponding data source object
 * is obtained from the "test" container according to the id field value,
 * Then map the "value" field value of the data source object
 * to the "name" field of the current object.
 *
 * <h3>mapping configuration</h3>
 * Establish the mapping relationship between data source object
 * and target object attributes through {@link Mapping} annotation.<br />
 * For example：<br />
 * If the object to be processed is<i>T<i>, the corresponding data source object is<i>S<i>：
 * <pre class="code">{@code
 * public class T {
 *     @Assemble
 *     private String id;
 *     private String name;
 * }
 * }</pre>
 * then：
 * <table type="text">
 *     <tr><td>mapping configuration                       </td><td><td>source     </td><td>target</td></tr>
 *     <tr><td>{@code @Mapping(src = "name", ref = "name")}</td><td><td>S.name     </td><td>T.name</td></tr>
 *     <tr><td>{@code @Mapping("name")}                    </td><td><td>S.name     </td><td>T.name</td></tr>
 *     <tr><td>{@code @Mapping(ref = "name")}              </td><td><td>S.name     </td><td>T.id</td></tr>
 *     <tr><td>{@code @Mapping(src = "name")}              </td><td><td>S          </td><td>T.name</td></tr>
 *     <tr><td>{@code @Mapping}                            </td><td><td>S          </td><td>T.id</td></tr>
 * </table>
 *
 * <h3>mapping template</h3>
 * <p>When there are many configurations through {@link Mapping},
 * we can consider separating them into templates through {@link #propTemplates()}. <br />
 * For example：
 * <pre class="code">{@code
 * // detach to template class
 * @MappingTemplate(@Mapping(src = "id1", ref = "name1"))
 * private static class MappingTemp {}
 *
 * // after simplification
 * public class Foo {
 *     @Assemble(
 *         namespace = "test",
 *         propTemplates = MappingTemp.class // import template class
 *     )
 *     private Integer id;
 *     private String name;
 * }
 * }</pre>
 * After parsing, {@link Mapping} declared in {@link MappingTemplate}
 * is equivalent to that declared directly in {@link #props()}。
 *
 * @author huangchengxing
 * @see cn.crane4j.core.parser.TypeHierarchyBeanOperationParser;
 * @see cn.crane4j.core.parser.handler.AssembleAnnotationHandler;
 */
@Repeatable(value = Assemble.List.class)
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Assemble {

    /**
     * Operation id.
     *
     * @return id
     * @since 2.6.0
     */
    String id() default "";

    /**
     * Sort values.
     * The lower the value, the higher the priority.
     *
     * @return sort values
     */
    int sort() default Integer.MAX_VALUE;

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
     * @since 2.2.0
     */
    Class<?> keyType() default Object.class;

    /**
     * The name of key resolver to be used.
     *
     * @return namespace
     * @since 2.7.0
     */
    String keyResolver() default "";

    /**
     * Some description of the key which
     * helps {@link #keyResolver() resolver} to resolve the key.
     *
     * @return description
     * @since 2.7.0
     */
    String keyDesc() default "";
    
    /**
     * The namespace of the data source container to be used.
     *
     * @return namespace
     */
    String container() default "";

    /**
     * The name of the container provider to be used.
     *
     * @return container factory name
     */
    String containerProvider() default "";

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
     * @since 2.1.0
     */
    String propertyMappingStrategy() default "";

    /**
     * Batch operation.
     *
     * @author huangchengxing
     */
    @Documented
    @Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        Assemble[] value() default {};
    }
}

package cn.crane4j.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.Map;

/**
 * <p>Indicates that the annotation or the method pointed to by the annotation
 * can be converted to a method container of the specified type.
 * The annotated method needs to meet the following requirements：
 * <ul>
 *     <li>method must have a return value;</li>
 *     <li>If {@link #type()} is {@link MappingType#NO_MAPPING}, the return value type must be {@link Map};</li>
 *     <li>
 *         If {@link #type()} is not {@link MappingType#NO_MAPPING},
 *         the return value can be a single object, array or {@link Collection};
 *     </li>
 * </ul>
 *
 * <p>For example, the following example describes how to adapt the <i>requestFoo()</i> method to a data source container：<br />
 * The first way is to add annotations directly on the method：
 * <pre type="code">{@code
 * @ContainerMethod(
 *     namespace = "foo",
 *     resultType = Foo.class, resultKey = "id"
 * )
 * public List<Foo> requestFoo(Set<Integer> ids) { // do something }
 * }</pre>
 * The second way is to annotate the annotation on the class,
 * and then bind the method through {@link #bindMethod()} and {@link #bindMethodParamTypes()}:
 * <pre type="code">{@code
 * @ContainerMethod(
 *     namespace = "foo", resultType = Foo.class,
 *     bindMethod =  "requestFoo", bindMethodParamTypes = Set.class
 * )
 * public class Foo {
 *     public List<Foo> requestFoo(Set<Integer> ids) { // do something }
 * }
 * }</pre>
 * The generated container namespace is <i>"foo"</i>.
 * When the id set is entered into the container, the Foo set grouped by <i>"id"</i> will be returned.
 *
 * @author huangchengxing
 * @see cn.crane4j.core.support.container.DefaultMethodContainerFactory
 * @see cn.crane4j.core.support.container.ContainerMethodAnnotationProcessor
 */
@Repeatable(ContainerMethod.List.class)
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ContainerMethod {

    /**
     * Namespace of the data source container, use method name when empty.
     *
     * @return namespace
     */
    String namespace() default "";

    /**
     * The mapping relationship between the object returned by the method and the target object.
     *
     * @return mapping relationship
     */
    MappingType type() default MappingType.ONE_TO_ONE;

    /**
     * The strategy for handling duplicate keys.
     *
     * @return strategy
     * @since 2.2.0
     */
    DuplicateStrategy duplicateStrategy() default DuplicateStrategy.ALERT;

    /**
     * The key field of the data source object returned by the method.<br />
     * If {@link #type()} is {@link MappingType#NO_MAPPING} or {@link MappingType#ORDER_OF_KEYS},
     * this parameter is ignored.
     *
     * @return key field name
     */
    String resultKey() default "id";

    /**
     * Data source object type returned by method.<br />
     * If {@link #type()} is {@link MappingType#NO_MAPPING} or {@link MappingType#ORDER_OF_KEYS},
     * this parameter is ignored.
     *
     * @return type
     */
    Class<?> resultType() default Void.class;

    /**
     * The name of method which will be used to adapt the method container.<br/>
     * If annotation is annotated on the method, this parameter is ignored.
     *
     * @return method name, if empty, find method by {@link #namespace()}
     */
    String bindMethod() default "";

    /**
     * The parameter types of the method which will be used to adapt the method container.<br/>
     * If annotation is annotated on the method, this parameter is ignored.
     *
     * @return parameter types
     */
    Class<?>[] bindMethodParamTypes() default {};

    /**
     * <p>When the return value is a wrapper class,
     * we can specify to obtain the dataset to be processed
     * from the specific field of the wrapper class,
     * and then use it to be data source of the container.
     *
     * <p>For example:
     * <pre type="code">{@code
     * // general response
     * public static class Result<T> {
     *     private Integer code;
     *     private T data; // objects to be processed
     * }
     * // process general response
     * @ContainerMethod(resultType = Foo.class, on = "data")
     * public Result<List<Foo>> requestFoo() { // do something }
     * }</pre>
     * The return value of the method is<i>Result</i>, but the data is in <i>Result.data</i>,
     * obtain data from specific fields for <i>on</i>.
     *
     * @return field name
     * @since 2.8.0
     * @see AutoOperate#on()
     */
    String on() default "";

    /**
     * Batch operation.
     *
     * @author huangchengxing
     */
    @Documented
    @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        ContainerMethod[] value() default {};
    }
}

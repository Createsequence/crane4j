package cn.crane4j.extension.spring.annotation.compose;

import cn.crane4j.annotation.ArgAutoOperate;
import cn.crane4j.annotation.AutoOperate;
import cn.crane4j.core.executor.AsyncBeanOperationExecutor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to mark a method as an asynchronous auto operation.
 *
 * @author huangchengxing
 * @see AutoOperate
 * @since 2.8.0
 */
@AutoOperate(executorType = AsyncBeanOperationExecutor.class)
@Target({ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AsyncAutoOperate {

    /**
     * When used in {@link ArgAutoOperate}, it is used to bind the corresponding parameter name.
     *
     * @return parameter name
     */
    String value() default "";

    /**
     * <p>The object type to be processed in the return value of the method.<br />
     * It will be parsed by the parser specified in {@link AutoOperate#parser()} when it is first executed,
     * and then the operation configuration will be obtained.
     *
     * <p>If the object is generic or cannot be confirmed, the default value is {@link Object},
     * and the type will be automatically inferred.
     *
     * @return type to be processed
     * @see cn.crane4j.core.support.TypeResolver
     */
    Class<?> type() default Object.class;

    /**
     * <p>Whether to resolve the operations from the current element
     * when annotated on method.<br/>
     * When this option is true, {@link #type()} will be ignored.
     *
     * @return true if resolve from current element, otherwise false
     * @since 2.7.0
     */
    boolean resolveOperationsFromCurrentElement() default false;

    /**
     * <p>When the return value is a wrapper class,
     * we can specify to obtain the data set to be processed
     * from the specific field of the wrapper class, and then process it。<br />
     *
     * <p>This configuration is generally used to process the method
     * of returning the general response body in the Controller。<br />
     * For example:
     * <pre type="code">{@code
     * // general response
     * public static class Result<T> {
     *     private Integer code;
     *     private T data; // objects to be processed
     * }
     * // process general response
     * @AutoOperate(type = Foo.class, on = "data")
     * public Result<Foo> requestFoo() { // do something }
     * }</pre>
     * The return value of the method is<i>Result</i>, but the data to be filled is in <i>Result.data</i>,
     * obtain data from specific fields for filling by <i>on</i>.
     *
     * @return field name
     */
    String on() default "";

    /**
     * The group of operations to be performed. <br />
     * If it is blank, all operations will be performed by default.
     *
     * @return group names
     */
    String[] includes() default {};

    /**
     * <p>The group of operations not to be performed.<br />
     * The priority of this configuration is higher than {@link #includes()}
     *
     * @return group names
     */
    String[] excludes() default {};

    /**
     * <p>The expression of apply condition.
     * The operation will only be performed when the expression result is {@code true} or "true" string。
     *
     * <p>The following variables can be used by default in the expression：
     * <ul>
     *     <li>{@code @beanName}：beans in spring context；</li>
     *     <li>{@code #parameterName}: arguments of method；</li>
     *     <li>{@code #result}: return value of method；</li>
     * </ul>
     *
     * @return expression of apply condition
     */
    String condition() default "";
}

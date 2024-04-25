package cn.crane4j.core.support.aop;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.annotation.AutoOperate;
import cn.crane4j.annotation.Mapping;
import cn.crane4j.core.container.LambdaContainer;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.ParameterNameFinder;
import cn.crane4j.core.support.SimpleAnnotationFinder;
import cn.crane4j.core.support.SimpleCrane4jGlobalConfiguration;
import cn.crane4j.core.support.SimpleParameterNameFinder;
import cn.crane4j.core.support.auto.AutoOperateAnnotatedElementResolver;
import cn.crane4j.core.support.auto.MethodBasedAutoOperateAnnotatedElementResolver;
import cn.crane4j.core.support.expression.MethodBasedExpressionEvaluator;
import cn.crane4j.core.support.expression.OgnlExpressionContext;
import cn.crane4j.core.support.expression.OgnlExpressionEvaluator;
import cn.crane4j.core.util.ReflectUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * test for {@link MethodResultAutoOperateSupport}
 *
 * @author huangchengxing
 */
@SuppressWarnings("unused")
public class MethodResultAutoOperateSupportTest {

    private MethodResultAutoOperateSupport support;

    @Before
    public void init() {
        Crane4jGlobalConfiguration configuration = SimpleCrane4jGlobalConfiguration.builder().build();
        ParameterNameFinder parameterNameFinder = new SimpleParameterNameFinder();
        MethodBasedExpressionEvaluator expressionEvaluator = new MethodBasedExpressionEvaluator(
            parameterNameFinder, new OgnlExpressionEvaluator(), method -> new OgnlExpressionContext()
        );
        AutoOperateAnnotatedElementResolver resolver = new MethodBasedAutoOperateAnnotatedElementResolver(configuration, configuration.getTypeResolver());
        support = new MethodResultAutoOperateSupport(resolver, expressionEvaluator, SimpleAnnotationFinder.INSTANCE);

        configuration.registerContainer(LambdaContainer.<Integer>forLambda(
            "test", ids -> ids.stream().map(id -> new Foo(id, "name" + id))
                .collect(Collectors.toMap(Foo::getId, Function.identity()))
        ));
    }

    @Test
    public void beforeMethodInvoke() {
        Method method = ReflectUtils.getMethod(this.getClass(), "method", Collection.class);
        Assert.assertNotNull(method);
        Result<Foo> foo = new Result<>(new Foo(1));
        support.afterMethodInvoke(method, foo, new Object[]{ Arrays.asList(1, 2) });
        Assert.assertEquals("name1", foo.getData().getName());

        method = ReflectUtils.getMethod(this.getClass(), "notAnnotatedMethodWhenCannotResolveOperationsFromCurrentElement", Collection.class);
        Assert.assertNotNull(method);
        support.afterMethodInvoke(method, foo, new Object[]{ Arrays.asList(1, 2) });
    }

    @Test
    public void beforeMethodInvokeWhenResolveOperationsFromCurrentElement() {
        Method method = ReflectUtils.getMethod(this.getClass(), "methodWhenResolveOperationsFromCurrentElement", Collection.class);
        Assert.assertNotNull(method);
        Result<Foo2> foo = new Result<>(new Foo2(1));
        support.afterMethodInvoke(method, foo, new Object[]{ Arrays.asList(1, 2) });
        Assert.assertEquals("name1", foo.getData().getName());

        method = ReflectUtils.getMethod(this.getClass(), "notAnnotatedMethodWhenCannotResolveOperationsFromCurrentElement", Collection.class);
        Assert.assertNotNull(method);
        support.afterMethodInvoke(method, foo, new Object[]{ Arrays.asList(1, 2) });
    }

    @Test
    public void beforeMethodInvokeWhenCannotResolveOperationsFromCurrentElement() {
        Method method = ReflectUtils.getMethod(this.getClass(), "methodWhenCannotResolveOperationsFromCurrentElement", Collection.class);
        Assert.assertNotNull(method);
        Result<Foo2> foo = new Result<>(new Foo2(1));
        support.afterMethodInvoke(method, foo, new Object[]{ Arrays.asList(1, 2) });
        Assert.assertNull(foo.getData().getName());
    }

    @AutoOperate(on = "data", condition = "true")
    private Result<Collection<Foo>> method(Collection<Integer> ids) {
        return new Result<>(ids.stream().map(Foo::new).collect(Collectors.toList()));
    }

    @Assemble(key = "id", container = "test", props = @Mapping("name"))
    @AutoOperate(on = "data", condition = "true", resolveOperationsFromCurrentElement = true)
    private Result<Collection<Foo2>> methodWhenResolveOperationsFromCurrentElement(Collection<Integer> ids) {
        return new Result<>(ids.stream().map(Foo2::new).collect(Collectors.toList()));
    }

    @AutoOperate(on = "data", condition = "true", resolveOperationsFromCurrentElement = true)
    private Result<Collection<Foo2>> methodWhenCannotResolveOperationsFromCurrentElement(Collection<Integer> ids) {
        return new Result<>(ids.stream().map(Foo2::new).collect(Collectors.toList()));
    }
    private Result<Collection<Foo2>> notAnnotatedMethodWhenCannotResolveOperationsFromCurrentElement(Collection<Integer> ids) {
        return new Result<>(ids.stream().map(Foo2::new).collect(Collectors.toList()));
    }

    @Getter
    @RequiredArgsConstructor
    private static class Result<T> {
        private final T data;
    }

    @AllArgsConstructor
    @RequiredArgsConstructor
    @Data
    private static class Foo {
        @Assemble(container = "test", props = @Mapping("name"))
        private final Integer id;
        private String name;
    }

    @AllArgsConstructor
    @RequiredArgsConstructor
    @Data
    private static class Foo2 {
        private final Integer id;
        private String name;
    }
}

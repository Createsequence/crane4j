package cn.crane4j.core.parser.handler;

import cn.crane4j.annotation.AssembleMethod;
import cn.crane4j.annotation.ContainerCache;
import cn.crane4j.annotation.ContainerMethod;
import cn.crane4j.annotation.Mapping;
import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.support.SimpleCrane4jGlobalConfiguration;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * test for {@link AssembleMethodAnnotationHandler}
 *
 * @author huangchengxing
 */
public class AssembleMethodAnnotationHandlerTest {

    public static class Target {
        protected List<Foo> getItemsByInstantMethod() {
            return IntStream.range(0, 5)
                .mapToObj(idx -> new Foo(idx, idx)
                    .setValue1("value" + idx).setValue2("value" + idx).setValue3("value" + idx))
                .collect(Collectors.toList());
        }
    }

    public static List<Foo> getItemsByStaticMethod() {
        return IntStream.range(0, 5)
            .mapToObj(idx -> new Foo(idx, idx)
                .setName1("name" + idx).setName2("name" + idx).setName3("name" + idx))
            .collect(Collectors.toList());
    }

    @Test
    public void testStaticMethodOfType() {
        SimpleCrane4jGlobalConfiguration configuration = SimpleCrane4jGlobalConfiguration.create();
        BeanOperations beanOperations = configuration.getBeanOperationsParser(BeanOperationParser.class).parse(Foo.class);
        BeanOperationExecutor executor = configuration.getBeanOperationExecutor(BeanOperationExecutor.class);
        List<Foo> targets = IntStream
            .range(0, 5).mapToObj(idx -> new Foo(idx, idx))
            .collect(Collectors.toList());
        executor.execute(targets, beanOperations);
        for (int i = 0; i < targets.size(); i++) {
            Foo target = targets.get(i);
            String expectedName = "name" + i;
            Assert.assertEquals(expectedName, target.getName1());
            Assert.assertEquals(expectedName, target.getName2());
            Assert.assertEquals(expectedName, target.getName3());
        }
    }

    @Test
    public void testInstantMethodOfType() {
        SimpleCrane4jGlobalConfiguration configuration = SimpleCrane4jGlobalConfiguration.create();
        BeanOperations beanOperations = configuration.getBeanOperationsParser(BeanOperationParser.class).parse(Foo.class);
        BeanOperationExecutor executor = configuration.getBeanOperationExecutor(BeanOperationExecutor.class);
        List<Foo> targets = IntStream
            .range(0, 5).mapToObj(idx -> new Foo(idx, idx))
            .collect(Collectors.toList());
        executor.execute(targets, beanOperations);
        for (int i = 0; i < targets.size(); i++) {
            Foo target = targets.get(i);
            String expectedName = "value" + i;
            Assert.assertEquals(expectedName, target.getValue1());
            Assert.assertEquals(expectedName, target.getValue2());
            Assert.assertEquals(expectedName, target.getValue3());
        }
    }

    @Test
    public void testCache() {
        SimpleCrane4jGlobalConfiguration configuration = SimpleCrane4jGlobalConfiguration.create();
        BeanOperations beanOperations = configuration.getBeanOperationsParser(BeanOperationParser.class).parse(Foo.class);
        BeanOperationExecutor executor = configuration.getBeanOperationExecutor(BeanOperationExecutor.class);

        Foo foo1 = new Foo(1, 1);
        executor.execute(Collections.singleton(foo1), beanOperations);
        Foo foo2 = new Foo(1, 1);
        executor.execute(Collections.singleton(foo2), beanOperations);

        Assert.assertSame(foo1.target, foo2.target);
    }

    @Accessors(chain = true)
    @RequiredArgsConstructor
    @Data
    private static class Foo {

        @AssembleMethod(
            target = "cn.crane4j.core.parser.handler.AssembleMethodAnnotationHandlerTest",
            method = @ContainerMethod(bindMethod = "getItemsByStaticMethod", resultType = Foo.class),
            props = @Mapping(src = "name3", ref = "name3")
        )
        @AssembleMethod(
            targetType = AssembleMethodAnnotationHandlerTest.class,
            method = @ContainerMethod(bindMethod = "getItemsByStaticMethod", resultType = Foo.class),
            props = @Mapping(src = "name2", ref = "name2")
        )
        @AssembleMethod(
            targetType = AssembleMethodAnnotationHandlerTest.class,
            method = @ContainerMethod(bindMethod = "getItemsByStaticMethod", resultType = Foo.class),
            props = @Mapping(src = "name1", ref = "name1")
        )
        private final Integer id;
        private String name1;
        private String name2;
        private String name3;


        @AssembleMethod(
            target = "cn.crane4j.core.parser.handler.AssembleMethodAnnotationHandlerTest$Target",
            method = @ContainerMethod(bindMethod = "getItemsByInstantMethod", resultType = Foo.class),
            //props = @Mapping(src = "value3", ref = "value3")
            prop = "value3"
        )
        @AssembleMethod(
            targetType = AssembleMethodAnnotationHandlerTest.Target.class,
            method = @ContainerMethod(bindMethod = "getItemsByInstantMethod", resultType = Foo.class),
            //props = @Mapping(src = "value2", ref = "value2")
            prop = "value2"
        )
        @AssembleMethod(
            targetType = AssembleMethodAnnotationHandlerTest.Target.class,
            method = @ContainerMethod(bindMethod = "getItemsByInstantMethod", resultType = Foo.class),
            props = @Mapping(src = "value1", ref = "value1")
        )
        private final Integer code;
        private String value1;
        private String value2;
        private String value3;


        @AssembleMethod(
            target = "cn.crane4j.core.parser.handler.AssembleMethodAnnotationHandlerTest",
            method = @ContainerMethod(bindMethod = "getItemsByStaticMethod", resultType = Foo.class),
            //props = @Mapping(ref = "target"),
            prop = ":target",
            enableCache = true, cache = @ContainerCache
        )
        private Integer key;
        private Target target;
    }
}

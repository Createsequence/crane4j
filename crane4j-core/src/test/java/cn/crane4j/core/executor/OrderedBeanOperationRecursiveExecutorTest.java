package cn.crane4j.core.executor;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.annotation.Disassemble;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.Containers;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.support.Crane4jGlobalSorter;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * test for {@link OrderedBeanOperationRecursiveExecutor}
 *
 * @author huangchengxing
 */
public class OrderedBeanOperationRecursiveExecutorTest extends BaseExecutorTest {

    private OrderedBeanOperationRecursiveExecutor executor;

    @Before
    public void init() {
        executor = new OrderedBeanOperationRecursiveExecutor(
            configuration, Crane4jGlobalSorter.comparator());

        Map<Integer, Foo> sources = new HashMap<>();
        sources.put(1, new Foo(2, "foo2"));
        sources.put(2, new Foo(3, "foo3"));
        sources.put(3, new Foo(4, "foo4"));
        Container<Integer> container = Containers.forMap("test", sources);
        configuration.registerContainer(container);
    }

    @Test
    public void test() {
        BeanOperations beanOperations = parseOperations(Foo.class);
        Foo foo = new Foo(1, "foo1");
        executor.execute(Collections.singleton(foo), beanOperations);

        foo = foo.getNested();
        Assert.assertNotNull(foo);
        Assert.assertEquals("foo2", foo.getName());

        foo = foo.getNested();
        Assert.assertNotNull(foo);
        Assert.assertEquals("foo3", foo.getName());

        foo = foo.getNested();
        Assert.assertNotNull(foo);
        Assert.assertEquals("foo4", foo.getName());

        Assert.assertNull(foo.getNested());
    }

    @Assemble(prop = "id:key", sort = 1)
    @Data
    @RequiredArgsConstructor
    public static class Foo {
        private final Integer id;
        private final String name;
        @Assemble(container = "test", prop = ":nested", sort = 2)
        private Integer key;
        @Disassemble(type = Foo.class)
        private Foo nested;
    }
}

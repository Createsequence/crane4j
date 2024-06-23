package cn.crane4j.core.executor;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.annotation.Disassemble;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.Containers;
import cn.crane4j.core.parser.BeanOperations;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * test for {@link AsyncBeanOperationRecursiveExecutor}
 *
 * @author huangchengxing
 */
public class AsyncBeanOperationRecursiveExecutorTest extends BaseExecutorTest {

    private AsyncBeanOperationRecursiveExecutor operationExecutor;

    @Before
    public void init() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
            1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>()
        );
        operationExecutor = new AsyncBeanOperationRecursiveExecutor(configuration, executor);
        operationExecutor.setBatchSize(5);

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
        operationExecutor.execute(Collections.singleton(foo), beanOperations);

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

    @Data
    @RequiredArgsConstructor
    public static class Foo {
        @Assemble(container = "test", prop = ":nested")
        private final Integer id;
        private final String name;
        @Disassemble(type = Foo.class)
        private Foo nested;
    }
}

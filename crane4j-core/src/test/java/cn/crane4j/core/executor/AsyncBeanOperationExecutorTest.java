package cn.crane4j.core.executor;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.annotation.Disassemble;
import cn.crane4j.annotation.Mapping;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.Containers;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.SimpleBeanOperations;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * test for {@link AbstractBeanOperationFlattingExecutor}
 *
 * @author huangchengxing
 */
public class AsyncBeanOperationExecutorTest extends BaseExecutorTest {

    private ThreadPoolExecutor executor;
    private AsyncBeanOperationExecutor operationExecutor;

    @Before
    public void init() {
        executor = new ThreadPoolExecutor(
            1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>()
        );
        operationExecutor = new AsyncBeanOperationExecutor(configuration, executor);
        operationExecutor.setBatchSize(5);

        Map<Integer, Object> sources = new HashMap<>();
        sources.put(1, new Source(1, "one"));
        sources.put(2, new Source(2, "two"));
        Container<Integer> container = Containers.forMap("test", sources);
        configuration.registerContainer(container);
    }

    @SuppressWarnings("all")
    @Test
    public void execute() {
        // for not active bean
        BeanOperations notActiveBeanOperations = new SimpleBeanOperations(null);
        operationExecutor.setEnableExecuteNotActiveOperation(false);
        notActiveBeanOperations.setActive(false);
        operationExecutor.execute(Collections.singleton(new Bean()), notActiveBeanOperations);

        // for normal bean
        Bean bean1 = new Bean().setId(1).setNestedBean(new NestedBean().setType(2));
        Bean bean2 = new Bean().setId(2).setNestedBean(new NestedBean().setType(1));
        List<Bean> beanList = Arrays.asList(bean1, bean2);

        BeanOperations beanOperations = parseOperations(Bean.class);
        operationExecutor.execute(beanList, beanOperations);

        // waiting for shutdown
        executor.shutdown();
        while (!executor.isShutdown()) {
        }

        Assert.assertEquals("one", bean1.getName());
        Assert.assertEquals("two", bean1.getNestedBean().getTypeName());
        Assert.assertEquals("two", bean2.getName());
        Assert.assertEquals("one", bean2.getNestedBean().getTypeName());
    }

    @Getter
    @RequiredArgsConstructor
    private static class Source {
        private final Integer key;
        private final String value;
    }

    @Accessors(chain = true)
    @Data
    private static class Bean {
        @Assemble(container = "test", props = @Mapping(ref = "name", src = "value"))
        private Integer id;
        private String name;
        @Disassemble(type = NestedBean.class)
        private NestedBean nestedBean;
    }

    @Accessors(chain = true)
    @Data
    private static class NestedBean {
        @Assemble(container = "test", props = @Mapping(ref = "typeName", src = "value"))
        private Integer type;
        private String typeName;
    }
}

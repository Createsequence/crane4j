package cn.crane4j.core.executor;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.annotation.Disassemble;
import cn.crane4j.core.container.ContainerManager;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.operation.AssembleOperation;
import cn.crane4j.core.parser.operation.KeyTriggerOperation;
import cn.crane4j.core.util.CollectionUtils;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * test for {@link AbstractBeanOperationFlattingExecutor}
 *
 * @author huangchengxing
 */
public class AbstractBeanOperationFlattingExecutorTest extends BaseExecutorTest {

    private TestExecutor executor;

    @Before
    public void init() {
        executor = new TestExecutor(configuration);
    }

    @Test
    public void execute() {
        executor.execute(null, BeanOperations.empty());
        executor.execute(Collections.singleton(new Object()), null);

        List<AssembleExecution> executions = getExecutions((beans, beanOperations) -> executor.execute(beans, beanOperations));

        AssembleExecution executionForId = CollectionUtils.get(executions, 0);
        checkAssembleOperation(executionForId, Bean.class, 3, "id");
        AssembleExecution executionForKey = CollectionUtils.get(executions, 1);
        checkAssembleOperation(executionForKey, Bean.class, 3, "key");
        AssembleExecution executionForNested = CollectionUtils.get(executions, 2);
        checkAssembleOperation(executionForNested, NestedBean.class, 2, "type");
    }

    @Test
    public void executeWhenHasFilter() {
        // 筛选不为“key”的操作
        Predicate<? super KeyTriggerOperation> filter = op -> op.isBelong("key");
        List<AssembleExecution> executions = getExecutions(
            (beans, beanOperations) -> executor.execute(beans, beanOperations, filter.negate())
        );
        Assert.assertEquals(2, executions.size());
    }

    private static void checkAssembleOperation(
        AssembleExecution execution, Class<?> targetType, int targetSize, String key) {
        Assert.assertNotNull(execution);
        AssembleOperation operationForId = execution.getOperation();
        Assert.assertEquals(key, operationForId.getKey());
        Assert.assertEquals(targetType, execution.getSource());
        Assert.assertEquals(targetSize, execution.getTargets().size());
    }

    private List<AssembleExecution> getExecutions(BiConsumer<List<Bean>, BeanOperations> consumer) {
        // bean * 3, nestBean * 2
        Bean bean1 = new Bean().setNestedBean(new NestedBean());
        Bean bean2 = new Bean().setNestedBean(new NestedBean());
        Bean bean3 = new Bean();
        List<Bean> beans = Arrays.asList(bean1, bean2, bean3);

        BeanOperations beanOperations = parseOperations(Bean.class);
        consumer.accept(beans, beanOperations);
        return executor.getExecutions();
    }

    @Getter
    private static class TestExecutor extends AbstractBeanOperationFlattingExecutor {
        private List<AssembleExecution> executions;
        public TestExecutor(ContainerManager containerManager) {
            super(containerManager);
        }

        @Override
        protected void doExecuteAssembleOperations(List<AssembleExecution> executions, Options options) {
            this.executions = executions;
        }
    }

    @Accessors(chain = true)
    @Data
    private static class Bean {
        @Assemble(groups = {"op", "id"}, sort = 0)
        private Integer id;
        private String name;

        @Assemble(groups = {"op", "key"}, sort = 1)
        private Integer key;
        private String value;

        @Disassemble(type = NestedBean.class, groups = "op")
        private NestedBean nestedBean;
    }

    @Data
    private static class NestedBean {
        @Assemble(groups = {"op", "id"})
        private Integer type;
        private String typeName;
    }
}

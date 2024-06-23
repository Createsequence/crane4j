package cn.crane4j.core.executor;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.annotation.Disassemble;
import cn.crane4j.core.container.ContainerManager;
import cn.crane4j.core.container.lifecycle.SmartOperationAwareBean;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * test for {@link AbstractOperationAwareBeanOperationExecutor}
 *
 * @author huangchengxing
 */
public class AbstractOperationAwareBeanOperationExecutorTest extends BaseExecutorTest {

    private TestExecutor executor;
    private static Map<String, Integer> callbackCountOfBean;
    private static Map<String, Integer> callbackCountOfNestedBean;

    @Before
    public void init() {
        executor = new TestExecutor(configuration);
        callbackCountOfBean = new HashMap<>();
        callbackCountOfNestedBean = new HashMap<>();
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

        // bean * 3
        Assert.assertEquals(6, callbackCountOfBean.get("beforeAssembleOperation").intValue());
        Assert.assertEquals(6, callbackCountOfBean.get("afterOperationsCompletion").intValue());
        Assert.assertEquals(18, callbackCountOfBean.get("supportOperation").intValue());
        // nestBean * 2
        Assert.assertEquals(4, callbackCountOfNestedBean.get("beforeAssembleOperation").intValue());
        Assert.assertEquals(4, callbackCountOfNestedBean.get("afterOperationsCompletion").intValue());
        Assert.assertEquals(4, callbackCountOfNestedBean.get("supportOperation").intValue());
    }

    @Test
    public void executeNoneCallback() {
        executor.execute(Collections.singleton(new NoneCallbackBean()), parseOperations(NoneCallbackBean.class));
        Assert.assertTrue(callbackCountOfBean.isEmpty());
        Assert.assertTrue(callbackCountOfNestedBean.isEmpty());
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
        AssembleExecution executionForId, Class<?> targetType, int targetSize, String key) {
        Assert.assertNotNull(executionForId);
        Assert.assertEquals(targetType, executionForId.getSource());
        Assert.assertEquals(targetSize, executionForId.getTargets().size());
        AssembleOperation operationForId = executionForId.getOperation();
        Assert.assertEquals(key, operationForId.getKey());
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
    private static class Bean implements SmartOperationAwareBean {
        @Assemble(groups = {"op", "id"})
        private Integer id;
        private String name;

        @Assemble(groups = {"op", "key"})
        private Integer key;
        private String value;

        @Disassemble(type = NestedBean.class, groups = "op")
        private NestedBean nestedBean;

        @Override
        public boolean supportOperation(String key) {
            callbackCountOfBean.put("supportOperation", callbackCountOfBean.getOrDefault("supportOperation", 0) + 1);
            return true;
        }
        @Override
        public boolean supportOperation(KeyTriggerOperation operation) {
            callbackCountOfBean.put("supportOperation", callbackCountOfBean.getOrDefault("supportOperation", 0) + 1);
            return true;
        }
        @Override
        public void afterOperationsCompletion() {
            callbackCountOfBean.put("afterOperationsCompletion", callbackCountOfBean.getOrDefault("afterOperationsCompletion", 0) + 1);
        }

        @Override
        public void beforeAssembleOperation() {
            callbackCountOfBean.put("beforeAssembleOperation", callbackCountOfBean.getOrDefault("beforeAssembleOperation", 0) + 1);
        }
        @Override
        public void beforeAssembleOperation(BeanOperations operations) {
            callbackCountOfBean.put("beforeAssembleOperation", callbackCountOfBean.getOrDefault("beforeAssembleOperation", 0) + 1);
        }
        @Override
        public void afterOperationsCompletion(BeanOperations operations) {
            callbackCountOfBean.put("afterOperationsCompletion", callbackCountOfBean.getOrDefault("afterOperationsCompletion", 0) + 1);
        }
    }

    @Data
    private static class NestedBean implements SmartOperationAwareBean {
        @Assemble(groups = {"op", "id"})
        private Integer type;
        private String typeName;

        @Override
        public boolean supportOperation(String key) {
            callbackCountOfNestedBean.put("supportOperation", callbackCountOfNestedBean.getOrDefault("supportOperation", 0) + 1);
            return true;
        }
        @Override
        public void beforeAssembleOperation() {
            callbackCountOfNestedBean.put("beforeAssembleOperation", callbackCountOfNestedBean.getOrDefault("beforeAssembleOperation", 0) + 1);
        }
        @Override
        public void beforeAssembleOperation(BeanOperations operations) {
            callbackCountOfNestedBean.put("beforeAssembleOperation", callbackCountOfNestedBean.getOrDefault("beforeAssembleOperation", 0) + 1);
        }
        @Override
        public void afterOperationsCompletion() {
            callbackCountOfNestedBean.put("afterOperationsCompletion", callbackCountOfNestedBean.getOrDefault("afterOperationsCompletion", 0) + 1);
        }
        @Override
        public boolean supportOperation(KeyTriggerOperation operation) {
            callbackCountOfNestedBean.put("supportOperation", callbackCountOfNestedBean.getOrDefault("supportOperation", 0) + 1);
            return true;
        }
        @Override
        public void afterOperationsCompletion(BeanOperations operations) {
            callbackCountOfNestedBean.put("afterOperationsCompletion", callbackCountOfNestedBean.getOrDefault("afterOperationsCompletion", 0) + 1);
        }
    }

    private static class NoneCallbackBean implements SmartOperationAwareBean {
        @SuppressWarnings("unused")
        @Assemble(groups = {"op", "id"})
        private Integer type;
    }
}

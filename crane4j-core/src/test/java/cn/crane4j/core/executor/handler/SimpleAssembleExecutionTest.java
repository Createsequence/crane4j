package cn.crane4j.core.executor.handler;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.executor.AssembleExecution;
import cn.crane4j.core.executor.handler.key.KeyResolver;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.SimpleBeanOperations;
import cn.crane4j.core.parser.operation.AssembleOperation;
import cn.crane4j.core.parser.operation.SimpleAssembleOperation;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

/**
 * test for {@link AssembleExecution.SimpleAssembleExecution}
 *
 * @author huangchengxing
 */
public class SimpleAssembleExecutionTest {

    private static final AssembleOperationHandler ASSEMBLE_OPERATION_HANDLER = new AssembleOperationHandler() {
        @Override
        public KeyResolver determineKeyResolver(AssembleOperation operation) {
            return null;
        }
        @Override
        public void process(Container<?> container, Collection<AssembleExecution> executions) {
            // do nothing
        }
    };
    private static final BeanOperations BEAN_OPERATIONS = new SimpleBeanOperations(Void.TYPE);
    private static final AssembleOperation ASSEMBLE_OPERATION = SimpleAssembleOperation.builder()
        .key("key")
        .propertyMappings(Collections.emptySet())
        .container(Container.EMPTY_CONTAINER_NAMESPACE)
        .assembleOperationHandler(ASSEMBLE_OPERATION_HANDLER)
        .build();

    private static final AssembleExecution EXECUTION = new AssembleExecution.SimpleAssembleExecution(
        BEAN_OPERATIONS, ASSEMBLE_OPERATION, Container.empty(), Collections.emptyList()
    );

    @Test
    public void getBeanOperations() {
        Assert.assertSame(BEAN_OPERATIONS, EXECUTION.getBeanOperations());
    }

    @Test
    public void getTargetType() {
        Assert.assertEquals(BEAN_OPERATIONS.getSource(), EXECUTION.getSource());
    }

    @Test
    public void getOperation() {
        Assert.assertSame(ASSEMBLE_OPERATION, EXECUTION.getOperation());
    }

    @Test
    public void getContainer() {
        Assert.assertEquals(Container.empty(), EXECUTION.getContainer());
    }

    @Test
    public void getHandler() {
        Assert.assertSame(ASSEMBLE_OPERATION_HANDLER, EXECUTION.getHandler());
    }

    @Test
    public void getTargets() {
        Assert.assertEquals(Collections.emptyList(), EXECUTION.getTargets());
    }
}

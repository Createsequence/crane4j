package cn.crane4j.core.executor;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerManager;
import cn.crane4j.core.exception.OperationExecuteException;
import cn.crane4j.core.parser.operation.AssembleOperation;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * <p>Synchronization implementation of {@link BeanOperationExecutor}.<br />
 * During execution, the execution order of {@link AssembleOperation} will be guaranteed,
 * but it cannot be guaranteed that {@link Container} will only be called at least once.
 *
 * @author huangchengxing
 */
public class OrderedBeanOperationExecutor extends AbstractFlatDisassembleBeanOperationExecutor {

    /**
     * comparator
     */
    private final Comparator<AssembleOperation> comparator;

    /**
     * Create a new {@link OrderedBeanOperationExecutor} instance.
     *
     * @param containerManager container manager
     * @param comparator       comparator
     */
    public OrderedBeanOperationExecutor(ContainerManager containerManager, Comparator<AssembleOperation> comparator) {
        super(containerManager);
        this.comparator = comparator;
    }

    /**
     * <p>Complete the assembly operation.<br />
     * All operations of input parameters ensure their orderliness in the same class.
     * For example, if there are ordered operations <i>a<i> and <i>b<i> in {@code A.class},
     * the order of <i>a<i> and <i>b<i> is still guaranteed when
     * the corresponding {@link AssembleExecution} is obtained.
     *
     * @param executions assembly operations to be completed
     * @param options options for execution
     * @throws OperationExecuteException thrown when operation execution exception
     * @implNote
     * <ul>
     *     <li>If necessary, you need to ensure the execution order of {@link AssembleExecution};</li>
     *     <li>
     *         If the network request and other time-consuming operations are required to obtain the data source,
     *         the number of requests for the data source should be reduced as much as possible;
     *     </li>
     * </ul>
     */
    @Override
    protected void doExecuteAssembleOperations(List<AssembleExecution> executions, Options options) throws OperationExecuteException {
        executions.stream()
            .sorted(Comparator.comparing(AssembleExecution::getOperation, comparator))
            .forEach(e -> tryExecuteAssembleExecution(e.getHandler(), e.getContainer(), Collections.singletonList(e)));
    }
}

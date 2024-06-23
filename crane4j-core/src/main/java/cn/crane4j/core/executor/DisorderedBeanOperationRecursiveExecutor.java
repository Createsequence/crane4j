package cn.crane4j.core.executor;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerManager;
import cn.crane4j.core.exception.OperationExecuteException;
import cn.crane4j.core.executor.handler.AssembleOperationHandler;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Synchronization implementation of {@link AbstractBeanOperationRecursiveExecutor}.<br />
 * During execution, the number of calls to {@link Container} will be reduced as much as possible,
 * but the order of operation execution cannot be guaranteed.
 *
 * @author huangchengxing
 * @since 2.9.0
 */
public class DisorderedBeanOperationRecursiveExecutor extends AbstractBeanOperationRecursiveExecutor {

    public DisorderedBeanOperationRecursiveExecutor(ContainerManager containerManager) {
        super(containerManager);
    }

    /**
     * <p>Complete the assembly operation.<br />
     * All operations of input parameters ensure their orderliness in the same class.
     * For example, if there are ordered operations <i>a<i> and <i>b<i> in {@code A.class},
     * the order of <i>a<i> and <i>b<i> is still guaranteed when
     * the corresponding {@link AssembleExecution} is obtained.
     *
     * @param executions assembly operations to be completed
     * @param options    options for execution
     * @throws OperationExecuteException thrown when operation execution exception
     * @implNote <ul>
     * <li>If necessary, you need to ensure the execution order of {@link AssembleExecution};</li>
     * <li>
     * If the network request and other time-consuming operations are required to obtain the data source,
     * the number of requests for the data source should be reduced as much as possible;
     * </li>
     * </ul>
     */
    @Override
    protected void doExecuteAssembleOperations(
        List<AssembleExecution> executions, Options options) throws OperationExecuteException {
        Map<Container<?>, Map<AssembleOperationHandler, List<AssembleExecution>>> operations = new LinkedHashMap<>();
        executions.forEach(e -> {
            List<AssembleExecution> es = getAssembleExecutions(e, operations);
            es.add(e);
        });
        try {operations.forEach((container, he) ->
            he.forEach((handler, execs) -> tryExecuteAssembleExecution(handler, container, execs))
        );
        } catch (Exception e) {
            throw new OperationExecuteException(e);
        }
    }

    @NonNull
    private static List<AssembleExecution> getAssembleExecutions(AssembleExecution e, Map<Container<?>, Map<AssembleOperationHandler, List<AssembleExecution>>> operations) {
        Container<?> container = e.getContainer();
        Map<AssembleOperationHandler, List<AssembleExecution>> he = operations.computeIfAbsent(container, c -> new HashMap<>(8));
        return he.computeIfAbsent(e.getHandler(), h -> new ArrayList<>());
    }
}

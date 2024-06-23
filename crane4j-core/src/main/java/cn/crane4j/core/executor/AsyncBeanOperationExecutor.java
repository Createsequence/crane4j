package cn.crane4j.core.executor;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerManager;
import cn.crane4j.core.executor.handler.AssembleOperationHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * <p>The asynchronous implementation of {@link BeanOperationExecutor}.<br />
 * It will group the operations to be executed according to the data source container,
 * then submit them to the executor in turn, and finally complete them asynchronously.
 *
 * @author huangchengxing
 */
@Slf4j
public class AsyncBeanOperationExecutor extends DisorderedBeanOperationExecutor {

    /**
     * thread pool used to perform operations.
     */
    private final Executor executor;

    /**
     * Create an instance of {@link AsyncBeanOperationExecutor}.
     *
     * @param containerManager container manager
     * @param executor thread pool used to perform operations
     */
    public AsyncBeanOperationExecutor(
        ContainerManager containerManager, Executor executor) {
        super(containerManager);
        this.executor = executor;
    }

    /**
     * <p>Execute the assembly operation.
     *
     * @param executionGroups grouped assembly operations
     */
    @Override
    protected void doExecuteOperations(Map<Container<?>, Map<AssembleOperationHandler, List<AssembleExecution>>> executionGroups) {
        List<Runnable> tasks = new ArrayList<>(executionGroups.size());
        executionGroups.forEach((c, he) ->
            he.forEach((h, es) -> tasks.add(() -> h.process(c, es))));
        tasks.stream()
            .map(t -> CompletableFuture.runAsync(t, executor))
            .collect(Collectors.collectingAndThen(
                Collectors.toList(), ts -> CompletableFuture.allOf(ts.toArray(new CompletableFuture[0]))
            ))
            .join();
    }
}

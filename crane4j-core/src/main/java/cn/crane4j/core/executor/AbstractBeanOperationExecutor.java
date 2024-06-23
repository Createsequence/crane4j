package cn.crane4j.core.executor;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerManager;
import cn.crane4j.core.exception.OperationExecuteException;
import cn.crane4j.core.executor.handler.AssembleOperationHandler;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.operation.AssembleOperation;
import cn.crane4j.core.parser.operation.KeyTriggerOperation;
import cn.crane4j.core.util.Asserts;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.core.util.MultiMap;
import cn.crane4j.core.util.TimerUtil;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
/**
 *
 * <p>This class serves as a template class and provides a basic skeleton implementation
 * for most of the {@link BeanOperationExecutor}.
 *
 * <p>This class only guarantees the sequential execution of {@link cn.crane4j.core.parser.operation.DisassembleOperation} operations,
 * while the sequential execution of {@link AssembleOperation} operations depends on
 * the implementation logic of {@link #doExecuteAssembleOperations}.<br />
 * For performance reasons, when implementing the {@link #doExecuteAssembleOperations} method,
 * it is recommended to minimize the number of accesses to the {@link Container}.
 *
 * @author huangchengxing
 * @see #doExecute 
 * @see #executeAssembleOperations
 * @see #executeDisassembleOperations
 * @since 2.5.0
 */
@SuppressWarnings("java:S125")
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractBeanOperationExecutor implements BeanOperationExecutor {

    /**
     * Container manager.
     */
    protected final ContainerManager containerManager;

    /**
     * <p>process target num of each batch when executing an operation.<br />
     * for example, if we have 1000 targets and batch size is 100,
     * and each target has 3 operations, so we will get 3000 executions.<br />
     * it's maybe useful when using asynchronous executor to process a large number of targets.
     *
     * @since 2.5.0
     */
    @Setter
    protected int batchSize = -1;

    /**
     * Whether to enable the execution of operations that are not active.
     */
    @Setter
    protected boolean enableExecuteNotActiveOperation = false;

    /**
     * Complete operations on all objects in {@code targets} according to the specified {@link BeanOperations} and {@link Options}.
     *
     * @param targets targets
     * @param operations operations to be performed
     * @param options options for execution
     * @see #beforeDisassembleOperation
     * @see #beforeAssembleOperation
     * @see #afterOperationsCompletion
     */
    @Override
    public void execute(Collection<?> targets, BeanOperations operations, Options options) {
        if (CollectionUtils.isEmpty(targets) || Objects.isNull(operations)) {
            return;
        }

        // When the following all conditions are met, the operation will be abandoned:
        // 1. The operation is not active;
        // 2. The operation is still not active after waiting for a period of time;
        // 3. The execution of non-active operations is not enabled.
        if (!operations.isActive() && !enableExecuteNotActiveOperation) {
            log.warn("bean operation of [{}] is still not ready, abort execution of the operation", operations.getSource());
            return;
        }

        doExecute(targets, operations, options);
    }

    /**
     * Complete operations on all objects in {@code targets} according to the specified {@link BeanOperations} and {@link Options}.
     *
     * @param targets targets
     * @param operations operations to be performed
     * @param options options for execution
     * @implSpec call {@link #afterOperationsCompletion} finally
     * @since 2.9.0
     */
    public abstract void doExecute(@NonNull Collection<?> targets, @NonNull BeanOperations operations, Options options);

    // region ====== disassemble ops ======

    /**
     * Complete assemble operations for {@code targets}
     * and call {@link #beforeDisassembleOperation} hook method.
     *
     * @param operations ops of targets
     * @param options options
     * @param targetWithOps targetWithOps
     * @return MultiMap<BeanOperations, Object>
     * @see #beforeDisassembleOperation
     * @since 2.9.0
     */
    protected final MultiMap<BeanOperations, Object> executeDisassembleOperations(
        BeanOperations operations, BeanOperationExecutor.Options options, MultiMap<BeanOperations, Object> targetWithOps) {
        targetWithOps.asMap().forEach((ops, targets) ->
            beforeDisassembleOperation(targets, operations, options));
        Predicate<? super KeyTriggerOperation> filter = options.getFilter();
        return TimerUtil.getExecutionTime(
            log.isDebugEnabled(),
            time -> log.debug("disassemble operations completed in {} ms", time),
            () -> doExecuteDisassembleOperations(filter, targetWithOps)
        );
    }

    /**
     * Complete disassemble for {@code targetWithOps}
     *
     * @param filter filter
     * @param targetWithOps targetWithOps
     * @return processed targets
     * @since 2.9.0
     */
    protected abstract MultiMap<BeanOperations, Object> doExecuteDisassembleOperations(
        Predicate<? super KeyTriggerOperation> filter, MultiMap<BeanOperations, Object> targetWithOps);

    // endregion

    // region ====== assemble ops ======

    /**
     * Complete assemble operations for {@code targetWithOps}
     * and call {@link #doExecuteAssembleOperations} hook method.
     *
     * @param options options
     * @param targetWithOps target with ops
     * @see #doExecuteAssembleOperations
     * @since 2.9.0
     */
    protected final void executeAssembleOperations(
        BeanOperationExecutor.Options options, MultiMap<BeanOperations, Object> targetWithOps) {
        // execute assemble operations
        beforeAssembleOperation(targetWithOps);
        List<AssembleExecution> executions = new ArrayList<>();
        Predicate<? super KeyTriggerOperation> filter = options.getFilter();
        targetWithOps.asMap().forEach((op, ts) -> {
            List<AssembleExecution> executionsOfOp = combineExecutions(options, filter, op, ts);
            if (CollectionUtils.isNotEmpty(executionsOfOp)) {
                executions.addAll(executionsOfOp);
            }
        });
        TimerUtil.getExecutionTime(
            log.isDebugEnabled(),
            time -> log.debug("assemble operations completed in {} ms", time),
            () -> doExecuteAssembleOperations(executions, options)
        );
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
    protected abstract void doExecuteAssembleOperations(
        List<AssembleExecution> executions, BeanOperationExecutor.Options options) throws OperationExecuteException;

    @NonNull
    private List<AssembleExecution> combineExecutions(
        BeanOperationExecutor.Options options, Predicate<? super KeyTriggerOperation> filter, BeanOperations beanOperations, Collection<Object> targets) {
        List<Collection<Object>> batches = batchSize > 1 ?
            CollectionUtils.split(targets, batchSize) : Collections.singletonList(targets);
        return batches.stream()
            .map(batch -> doCombineExecutions(options, filter, beanOperations, batch))
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    /**
     * Combine the {@link AssembleExecution} objects according to the specified {@link BeanOperations} and {@link BeanOperationExecutor.Options}.
     *
     * @param options options for execution
     * @param filter filter
     * @param beanOperations bean operations
     * @param targets targets
     * @return {@link AssembleExecution} objects
     */
    @NonNull
    protected List<AssembleExecution> doCombineExecutions(
        BeanOperationExecutor.Options options, Predicate<? super KeyTriggerOperation> filter, BeanOperations beanOperations, Collection<Object> targets) {
        return beanOperations.getAssembleOperations()
            .stream()
            .filter(filter)
            .map(p -> createAssembleExecution(beanOperations, p, targets, options))
            .collect(Collectors.toList());
    }

    /**
     * Create a {@link AssembleExecution}.
     *
     * @param beanOperations bean operations
     * @param operation operation
     * @param targets targets
     * @param options options for execution
     * @return {@link AssembleExecution}
     */
    private AssembleExecution createAssembleExecution(
        BeanOperations beanOperations, AssembleOperation operation, Collection<Object> targets, BeanOperationExecutor.Options options) {
        targets = filterTargetsForSupportedOperation(targets, operation);
        String namespace = operation.getContainer();
        Container<?> container = options.getContainer(containerManager, namespace);
        Asserts.isNotNull(container, "container [{}] not found", namespace);
        return AssembleExecution.create(beanOperations, operation, container, targets);
    }

    // endregion

    // region ====== hook methods ======

    /**
     * Do something before the assembly operation begin.
     *
     * @param targetWithOperations target with operations
     */
    protected void beforeAssembleOperation(MultiMap<BeanOperations, Object> targetWithOperations) {
        // do nothing
    }

    /**
     * Do something before the disassemble operations begin.
     *
     * @param targets targets
     * @param operations operations
     * @param options options for execution
     * @since 2.5.0
     */
    @SuppressWarnings("unused")
    protected void beforeDisassembleOperation(
        Collection<?> targets, BeanOperations operations, BeanOperationExecutor.Options options) {
        // do nothing
    }

    /**
     * Do something after all operations completed.
     *
     * @param targetWithOperations target with operations
     * @since 2.5.0
     */
    protected void afterOperationsCompletion(MultiMap<BeanOperations, Object> targetWithOperations) {
        // do nothing
    }

    /**
     * Filter the targets that do not support the operation.
     *
     * @param targets targets
     * @param operation operation
     * @return filtered targets
     * @since 2.5.0
     */
    @NonNull
    protected <T> Collection<T> filterTargetsForSupportedOperation(
        Collection<T> targets, KeyTriggerOperation operation) {
        return targets;
    }

    // endregion

    /**
     * <p>Try to execute the operation.<br />
     * If necessary, output the log when throwing an exception.
     *
     * @param handler handler
     * @param executions executions
     * @param container container
     */
    protected static void tryExecuteAssembleExecution(
        AssembleOperationHandler handler, Container<?> container, Collection<AssembleExecution> executions) {
        try {
            handler.process(container, executions);
        } catch(Exception ex) {
            log.warn("execute operation fail: {}", ex.getMessage(), ex);
        }
    }
}

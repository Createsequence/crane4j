package cn.crane4j.core.executor;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerManager;
import cn.crane4j.core.executor.handler.DisassembleOperationHandler;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.operation.AssembleOperation;
import cn.crane4j.core.parser.operation.DisassembleOperation;
import cn.crane4j.core.parser.operation.KeyTriggerOperation;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.core.util.MultiMap;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;
import java.util.function.Predicate;

/**
 *
 * <p>This class serves as a template class and provides a basic skeleton implementation
 * for most of the {@link BeanOperationExecutor},
 * particularly shielding the complexity of parsing {@link DisassembleOperation} operations.
 * Once the {@link #doExecuteAssembleOperations} method is implemented in a subclass,
 * it can be used as a {@link BeanOperationExecutor}.
 *
 * <p>According to the instructions specified in {@link BeanOperations},
 * the following steps are performed when executing operations through the {@link BeanOperationExecutor#execute} method:
 * <ul>
 *     <li>
 *         If there are any {@link DisassembleOperation} operations to be executed,
 *         recursively extract and flatten the objects that need to be processed from
 *         the {@code target} object (if it is a collection or an array, iterate over each element);
 *     </li>
 *     <li>
 *         Group all the objects to be processed based on their corresponding {@link BeanOperations},
 *         and wrap each group into an {@link AssembleExecution} object.
 *     </li>
 *     <li>
 *         Invoke the {@link #doExecuteAssembleOperations} method implemented in the subclass to
 *         actually perform the operations within each {@link AssembleExecution}.
 *     </li>
 * </ul>
 * 
 * <p>This class only guarantees the sequential execution of {@link DisassembleOperation} operations,
 * while the sequential execution of {@link AssembleOperation} operations depends on
 * the implementation logic of {@link #doExecuteAssembleOperations}.<br />
 * For performance reasons, when implementing the {@link #doExecuteAssembleOperations} method,
 * it is recommended to minimize the number of accesses to the {@link Container}.
 *
 * @author huangchengxing
 * @see AsyncBeanOperationExecutor
 * @see DisorderedBeanOperationExecutor
 * @see OrderedBeanOperationExecutor
 * @since 2.9.0
 */
@Slf4j
public abstract class AbstractBeanOperationFlattingExecutor
    extends AbstractOperationAwareBeanOperationExecutor {

    protected AbstractBeanOperationFlattingExecutor(ContainerManager containerManager) {
        super(containerManager);
    }

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
    public void doExecute(
        @NonNull Collection<?> targets, @NonNull BeanOperations operations, Options options) {
        MultiMap<BeanOperations, Object> targetWithOperations = MultiMap.linkedListMultimap();
        targetWithOperations.putAll(operations, targets);
        // disassembly
        targetWithOperations = executeDisassembleOperations(
            operations, options, targetWithOperations);
        // assembly
        executeAssembleOperations(options, targetWithOperations);
        // complete assembly operation
        afterOperationsCompletion(targetWithOperations);
    }

    /**
     * Complete disassemble for {@code targetWithOps}
     *
     * @param filter        filter
     * @param targetWithOps targetWithOps
     * @return processed targets
     */
    @Override
    protected MultiMap<BeanOperations, Object> doExecuteDisassembleOperations(
        Predicate<? super KeyTriggerOperation> filter,
        MultiMap<BeanOperations, Object> targetWithOps) {
        MultiMap<BeanOperations, Object> results = MultiMap.linkedListMultimap();
        results.putAll(targetWithOps);
        targetWithOps.asMap().forEach((ops, targets) ->
            disassembleIfNecessary(targets, ops, filter, results));
        return results;
    }

    private <T> void disassembleIfNecessary(
        Collection<T> targets, BeanOperations operations,
        Predicate<? super KeyTriggerOperation> filter, MultiMap<BeanOperations, Object> collectedTargets) {
        Collection<DisassembleOperation> internalOperations = operations.getDisassembleOperations();
        if (CollectionUtils.isEmpty(internalOperations)) {
            return;
        }
        internalOperations.stream()
            .filter(filter)
            .forEach(internal -> doDisassembleAndCollect(targets, internal, filter, collectedTargets));
    }

    private <T> void doDisassembleAndCollect(
        Collection<T> targets, DisassembleOperation disassembleOperation, Predicate<? super KeyTriggerOperation> filter, MultiMap<BeanOperations, Object> collector) {
        DisassembleOperationHandler handler = disassembleOperation.getDisassembleOperationHandler();
        targets = filterTargetsForSupportedOperation(targets, disassembleOperation);
        Collection<?> internalTargets = handler.process(disassembleOperation, targets);
        if (CollectionUtils.isEmpty(internalTargets)) {
            return;
        }
        BeanOperations internalOperations = disassembleOperation.getInternalBeanOperations(internalTargets);
        collector.putAll(internalOperations, internalTargets);
        // recurse process if still have nested objects
        disassembleIfNecessary(internalTargets, internalOperations, filter, collector);
    }
}

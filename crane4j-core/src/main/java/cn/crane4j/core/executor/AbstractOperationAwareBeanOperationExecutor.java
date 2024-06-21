package cn.crane4j.core.executor;

import cn.crane4j.annotation.OperationAwareBean;
import cn.crane4j.core.condition.Condition;
import cn.crane4j.core.container.ContainerManager;
import cn.crane4j.core.container.lifecycle.SmartOperationAwareBean;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.operation.KeyTriggerOperation;
import cn.crane4j.core.util.MultiMap;
import lombok.NonNull;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * An abstract implementation of {@link AbstractFlatDisassembleBeanOperationExecutor}
 * that supports the operation aware.
 *
 * @author huangchengxing
 * @see OperationAwareBean
 * @see SmartOperationAwareBean
 * @since 2.5.0
 */
public abstract class AbstractOperationAwareBeanOperationExecutor
    extends AbstractBeanOperationExecutor {

    protected AbstractOperationAwareBeanOperationExecutor(ContainerManager containerManager) {
        super(containerManager);
    }

    /**
     * Do something before the assembly operation begin.
     *
     * @param targetWithOperations target with operations
     */
    @Override
    protected void beforeAssembleOperation(MultiMap<BeanOperations, Object> targetWithOperations) {
        targetWithOperations.forEach((operations, target) -> {
            if (target instanceof OperationAwareBean) {
                ((OperationAwareBean)target).beforeAssembleOperation();
                if (target instanceof SmartOperationAwareBean) {
                    ((SmartOperationAwareBean)target).beforeAssembleOperation(operations);
                }
            }
        });
    }

    /**
     * Trigger the {@link OperationAwareBean#afterOperationsCompletion} method of the target object.
     *
     * @param targetWithOperations target with operations
     * @since 2.5.0
     */
    @Override
    protected void afterOperationsCompletion(MultiMap<BeanOperations, Object> targetWithOperations) {
        targetWithOperations.forEach((operations, target) -> {
            if (target instanceof OperationAwareBean) {
                ((OperationAwareBean)target).afterOperationsCompletion();
                if (target instanceof SmartOperationAwareBean) {
                    ((SmartOperationAwareBean)target).afterOperationsCompletion(operations);
                }
            }
        });
    }

    /**
     * Filter the targets that support the specified operation
     * by the {@link OperationAwareBean#supportOperation} method.
     *
     * @param targets targets
     * @param operation operation
     * @return filtered targets
     * @since 2.5.0
     */
    @Override
    @NonNull
    protected <T> Collection<T> filterTargetsForSupportedOperation(
        Collection<T> targets, KeyTriggerOperation operation) {
        Predicate<T> filter = t -> filterBySupportOperation(t, operation);
        Condition condition = operation.getCondition();
        if (Objects.nonNull(condition)) {
            filter = filter.and(t -> filterByCondition(t, operation, condition));
        }
        return targets.stream()
            .filter(filter)
            .collect(Collectors.toList());
    }

    private boolean filterByCondition(
        Object target, KeyTriggerOperation operation, Condition condition) {
        return condition.test(target, operation);
    }

    private boolean filterBySupportOperation(Object target, KeyTriggerOperation operation) {
        if (target instanceof OperationAwareBean) {
            boolean support = ((OperationAwareBean)target).supportOperation(operation.getKey());
            if (support && target instanceof SmartOperationAwareBean) {
                return ((SmartOperationAwareBean)target).supportOperation(operation);
            }
            return support;
        }
        return true;
    }
}
